package com.myapp.plugins.espprovisioning;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;

import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.BleScanListener;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

@NativePlugin
public class EspProvisioning extends Plugin {

    ESPProvisionManager espProvisionManager;

    public void load() {
        espProvisionManager = ESPProvisionManager.getInstance(getContext().getApplicationContext());
    }

    @PluginMethod
    public void requestPermissions(PluginCall call){
        String[] permissions = {ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(getActivity(),permissions,1);
        call.success();
    }

    @PluginMethod
    public void createESPDevice(PluginCall call) {
        String tpType = call.getString("transportType", ESPConstants.TransportType.TRANSPORT_SOFTAP.toString());
        String secType = call.getString("securityType", ESPConstants.SecurityType.SECURITY_1.toString());

        ESPConstants.TransportType transportType = ESPConstants.TransportType.valueOf(tpType);
        ESPConstants.SecurityType securityType = ESPConstants.SecurityType.valueOf(secType);

        ESPDevice espDevice = espProvisionManager.createESPDevice(transportType, securityType);

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
    public void scanQRCode(PluginCall call) {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    @PluginMethod
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public void searchBleEspDevices(final PluginCall call) {
        BleScanListener bleScanListener = new BleScanListener() {

            ArrayList<BluetoothDevice> devices = new ArrayList<>();
            ArrayList<ScanResult> scanResults = new ArrayList<>();

            @Override
            public void scanStartFailed() {
                call.error("ScanStartFailed");
            }

            @Override
            public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                devices.add(device);
                scanResults.add(scanResult);
            }

            @Override
            public void scanCompleted() {
                JSObject ret = new JSObject();
                for (BluetoothDevice bd : devices) {
                    ret.put(bd.getName(), bd);
                }
                call.success(ret);
            }

            @Override
            public void onFailure(Exception e) {
                call.error("Failure", e);
            }
        };
        if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (call.hasOption("prefix")) {
                String prefix = call.getString("prefix");
                ESPProvisionManager.getInstance(getContext().getApplicationContext()).searchBleEspDevices(prefix, bleScanListener);
            } else {
                ESPProvisionManager.getInstance(getContext().getApplicationContext()).searchBleEspDevices(bleScanListener);
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
