package com.myapp.plugins.espprovisioning;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

@NativePlugin
public class EspProvisioning extends Plugin {

    private ESPProvisionManager espProvisionManager;
    private Hashtable<String,BluetoothDevice> bleDevices;
    private ESPDevice espDevice;

    public void load() {
        espProvisionManager = ESPProvisionManager.getInstance(getContext().getApplicationContext());
    }

    @PluginMethod
    public void requestPermissions(PluginCall call){
        String[] permissions = {ACCESS_FINE_LOCATION,CAMERA};
        ActivityCompat.requestPermissions(getActivity(),permissions,1);
        call.success();
    }

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void createESPDevice(PluginCall call) {
        String tpType = call.getString("transportType", ESPConstants.TransportType.TRANSPORT_BLE.toString());
        String secType = call.getString("securityType", ESPConstants.SecurityType.SECURITY_1.toString());

        ESPConstants.TransportType transportType = ESPConstants.TransportType.valueOf(tpType);
        ESPConstants.SecurityType securityType = ESPConstants.SecurityType.valueOf(secType);

        ESPDevice espDevice = espProvisionManager.createESPDevice(transportType, securityType);
        espDevice.setDeviceName("PROV_XXX");
        espDevice.setProofOfPossession("abcd1234");
        espDevice.setBluetoothDevice(bleDevices.get("PROV_XXX"));
//        espDevice.connectToDevice();

        JSObject ret = new JSObject();
        ret.put("name", espDevice.getDeviceName());
        ret.put("transport_type", espDevice.getTransportType().toString());
        ret.put("security_type", espDevice.getSecurityType().toString());
        ret.put("proof_of_possession", espDevice.getProofOfPossession());
        ret.put("primary_service_uuid", espDevice.getPrimaryServiceUuid());
        ret.put("capabilities", espDevice.getDeviceCapabilities());
        ret.put("version", espDevice.getVersionInfo());
        call.success(ret);
    }

    @PluginMethod
    public void getEspDevice(PluginCall call) {
        ESPDevice espDevice = espProvisionManager.getEspDevice();

        JSObject ret = new JSObject();
        ret.put("name", espDevice.getDeviceName());
        ret.put("transport_type", espDevice.getTransportType().toString());
        ret.put("security_type", espDevice.getSecurityType().toString());
        ret.put("proof_of_possession", espDevice.getProofOfPossession());
        ret.put("primary_service_uuid", espDevice.getPrimaryServiceUuid());
        ret.put("capabilities", espDevice.getDeviceCapabilities());
        ret.put("version", espDevice.getVersionInfo());
        call.success(ret);
    }

    @PluginMethod
    @RequiresPermission(CAMERA)
    public void scanQRCode(PluginCall call) {
        final JSObject ret = new JSObject();
        ProvisionListener provisionListener = new ProvisionListener() {
            @Override
            public void createSessionFailed(Exception e) {
                ret.put("createSessionFailed", e.getMessage());
            }

            @Override
            public void wifiConfigSent() {
                ret.put("wifiConfigSent", "success");
            }

            @Override
            public void wifiConfigFailed(Exception e) {
                ret.put("wifiConfigFailed", e.getMessage());
            }

            @Override
            public void wifiConfigApplied() {
                ret.put("wifiConfigApplied", "success");
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {
                ret.put("wifiConfigApplyFailed", e.getMessage());
            }

            @Override
            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                ret.put("provisioningFailedFromDevice", failureReason.toString());
            }

            @Override
            public void deviceProvisioningSuccess() {
                ret.put("deviceProvisioning", "success");
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                ret.put("onProvisioningFailed", e.getMessage());
            }
        };
        espDevice.provision("","",provisionListener);
        call.success(ret);
    }

    @PluginMethod
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public void searchBleEspDevices(final PluginCall call) {
        if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bleDevices = new Hashtable<>();
            BleScanListener bleScanListener = new BleScanListener() {
                Hashtable<String,ScanResult> scanResults = new Hashtable<>();

                @Override
                public void scanStartFailed() {
                    call.error("ScanStartFailed");
                }

                @Override
                public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                    if(!bleDevices.containsKey(device.getName())){
                        bleDevices.put(device.getName(),device);
                    }
                    if(!scanResults.containsKey(scanResult.getDevice().getName())){
                        scanResults.put(scanResult.getDevice().getName(),scanResult);
                    }
                }

                @Override
                public void scanCompleted() {
                    final JSObject ret = new JSObject();
                    for (BluetoothDevice bd : bleDevices.values()) {
                        ret.put(bd.getName(), bd);
                    }
                    espDevice = espProvisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_1);
                    if(bleDevices.containsKey("PROV_XXX")) {
                        espDevice.setBluetoothDevice();
                        espDevice.setProofOfPossession("abcd1234");
                        espDevice.setDeviceName("PROV_XXX");
                        List<ParcelUuid> uuids = scanResults.get("PROV_XXX").getScanRecord().getServiceUuids();
                        ParcelUuid uuid = uuids.get(0);
                        espDevice.setPrimaryServiceUuid(scanResults.get("PROV_XXX").getScanRecord().getServiceUuids().get(0).getUuid().toString());
                        espDevice.connectToDevice();
                        call.success(ret);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    call.error("Failure", e);
                }
            };
            if (call.hasOption("prefix")) {
                String prefix = call.getString("prefix");
                ESPProvisionManager.getInstance(getContext().getApplicationContext()).searchBleEspDevices(prefix, bleScanListener);
            } else {
                espProvisionManager.searchBleEspDevices(bleScanListener);
            }
        } else {
            String[] permissions = {ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(getActivity(),permissions,1);
            call.error("Requires Permissions");
        }
    }

    @PluginMethod
    @RequiresPermission(allOf = {BLUETOOTH_ADMIN, BLUETOOTH, ACCESS_FINE_LOCATION})
    public void stopBleScan (PluginCall call) {
        espProvisionManager.stopBleScan();
        call.success();
    }

    @PluginMethod
    @RequiresPermission(allOf = {CHANGE_WIFI_STATE, ACCESS_WIFI_STATE})
    public void searchWifiEspDevices (final PluginCall call) {
        WiFiScanListener wiFiScanListener = new WiFiScanListener() {
            @Override
            public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                JSObject ret = new JSObject();
                for (WiFiAccessPoint accessPoint : wifiList) {
                    ret.put(accessPoint.getWifiName(), accessPoint);
                }
                call.success(ret);
            }

            @Override
            public void onWiFiScanFailed(Exception e) {
                call.error("WiFi Scan failed", e);
            }
        };

        if (call.hasOption("prefix")) {
            String prefix = call.getString("prefix");
            espProvisionManager.searchWiFiEspDevices(prefix, wiFiScanListener);
        } else {
            espProvisionManager.searchWiFiEspDevices(wiFiScanListener);
        }
    }
}
