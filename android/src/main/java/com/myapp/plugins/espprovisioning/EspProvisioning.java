package com.myapp.plugins.espprovisioning;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.util.Log;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Hashtable;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CHANGE_WIFI_STATE;

@NativePlugin(
        permissions={
                BLUETOOTH,
                BLUETOOTH_ADMIN,
                ACCESS_FINE_LOCATION,
                ACCESS_WIFI_STATE,
                CHANGE_WIFI_STATE,
                ACCESS_NETWORK_STATE
        },
        requestCodes = {EspProvisioning.REQUEST_ACCESS_FINE_LOCATION}
)
public class EspProvisioning extends Plugin {

    private ESPProvisionManager espProvisionManager;
    private Hashtable<String,ScanResult> scanResults;
    private Hashtable<Integer,ESPDevice> espDevices = new Hashtable<>();
    static final int REQUEST_ACCESS_FINE_LOCATION = 8000;

    private Integer createDeviceID(ESPDevice espDevice){
        Integer deviceID = espDevices.size();
        espDevices.put(deviceID,espDevice);
        return deviceID;
    }

    private boolean validateDeviceID(Integer deviceID){
        if(espDevices.containsKey(deviceID)){
            return true;
        }
        return false;
    }

    public void load() {
        espProvisionManager = ESPProvisionManager.getInstance(getContext().getApplicationContext());
    }

    @PluginMethod
    public void requestLocationPermissions(PluginCall call){
        if(ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            call.success();
        } else {
            saveCall(call);
            pluginRequestPermission(ACCESS_FINE_LOCATION,REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void createESPDevice(final PluginCall call) {
        if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            scanResults = new Hashtable<>();

            String tpType = call.getString("transportType", ESPConstants.TransportType.TRANSPORT_BLE.toString());
            String secType = call.getString("securityType", ESPConstants.SecurityType.SECURITY_1.toString());

            final String name = call.getString("name");
            final String pop = call.getString("pop");
            final ESPConstants.TransportType transportType = ESPConstants.TransportType.valueOf(tpType);
            final ESPConstants.SecurityType securityType = ESPConstants.SecurityType.valueOf(secType);

            BleScanListener bleScanListener = new BleScanListener() {

                @Override
                public void scanStartFailed() {
                    call.error("ScanStartFailed");
                }

                @Override
                public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                    if (!scanResults.containsKey(scanResult.getDevice().getName())) {
                        scanResults.put(scanResult.getDevice().getName(), scanResult);
                    }
                }

                @Override
                public void scanCompleted() {
                    if (scanResults.containsKey(name)) {
                        ESPDevice espDevice = espProvisionManager.createESPDevice(transportType, securityType);
                        espDevice.setDeviceName(name);
                        espDevice.setProofOfPossession(pop);
                        espDevice.setBluetoothDevice(scanResults.get(name).getDevice());
                        espDevice.setPrimaryServiceUuid(scanResults.get(name).getScanRecord().getServiceUuids().get(0).getUuid().toString());
                        int deviceID = createDeviceID(espDevice);
                        JSObject ret = new JSObject();
                        JSObject device = new JSObject();
                        ret.put("id",deviceID);
                        device.put("name",espDevice.getDeviceName());
                        device.put("transport_type",espDevice.getTransportType().toString());
                        device.put("security_type",espDevice.getSecurityType().toString());
                        device.put("proof_of_possesion",espDevice.getProofOfPossession());
                        device.put("primary_service_uuid",espDevice.getPrimaryServiceUuid());
                        ret.put("device",device);
                        call.success(ret);
                    } else {
                        call.error("Device not found");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    call.error("Failure", e);
                }
            };
            espProvisionManager.searchBleEspDevices(name, bleScanListener);
        } else {
            call.error("Requires Permission: Location");
        }
    }

    @PluginMethod
    @RequiresPermission(CAMERA)
    public void scanQRCode(PluginCall call) {
        call.reject("Not yet implemented");
    }

    @PluginMethod
    @RequiresPermission(ACCESS_FINE_LOCATION)
    public void searchBleEspDevices(final PluginCall call) {
        if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            scanResults = new Hashtable<>();
            BleScanListener bleScanListener = new BleScanListener() {

                @Override
                public void scanStartFailed() {
                    call.error("ScanStartFailed");
                }

                @Override
                public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                    if(!scanResults.containsKey(scanResult.getDevice().getName())){
                        scanResults.put(scanResult.getDevice().getName(),scanResult);
                    }
                }

                @Override
                public void scanCompleted() {
                    final JSObject ret = new JSObject();
                    for (ScanResult sr : scanResults.values()) {
                        ret.put(sr.getDevice().getName(), sr);
                    }
                    call.success(ret);
                }

                @Override
                public void onFailure(Exception e) {
                    call.error("Failure", e);
                }
            };
            if (call.hasOption("prefix")) {
                String prefix = call.getString("prefix");
                espProvisionManager.searchBleEspDevices(prefix, bleScanListener);
            } else {
                espProvisionManager.searchBleEspDevices(bleScanListener);
            }
        } else {
            call.error("Requires Permission: Location");
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

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void connectToDevice(PluginCall call) throws InterruptedException{
        Integer deviceID = call.getInt("device");
        if(validateDeviceID(deviceID)){
            ESPDevice device = espDevices.get(deviceID);
            saveCall(call);
            EventBus.getDefault().register(this);
            device.connectToDevice();
        } else {
            call.reject("Invalid Device ID provided");
        }
    }

    @PluginMethod
    public void scanWifiList(final PluginCall call){
        Integer deviceID = call.getInt("device");
        if(validateDeviceID(deviceID)){
            ESPDevice device = espDevices.get(deviceID);
            WiFiScanListener wifiScanListener = new WiFiScanListener() {
                @Override
                public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    JSObject ret = new JSObject();
                    ret.put("count",wifiList.size());
                    JSArray networks = new JSArray();
                    JSObject network;
                    for(WiFiAccessPoint ap : wifiList){
                        network = new JSObject();
                        network.put("ssid",ap.getWifiName());
                        network.put("rssi",ap.getRssi());
                        Log.d(ap.getWifiName(),String.valueOf(ap.getSecurity()));
                        if(ap.getSecurity() == 0){
                            network.put("security",false);
                        } else {
                            network.put("security",true);
                        }
                        networks.put(network);
                    }
                    ret.put("networks",networks);
                    call.success(ret);
                }

                @Override
                public void onWiFiScanFailed(Exception e) {
                    call.error("WiFi Scan Failed",e);
                }
            };
            device.scanNetworks(wifiScanListener);
        } else {
            call.reject("Invalid Device ID provided");
        }
    }

    @PluginMethod
    public void provision(final PluginCall call){
        Integer deviceID = call.getInt("device");
        String ssid = call.getString("ssid");
        String passphrase = call.getString("passphrase");
        if(validateDeviceID(deviceID)){
            ESPDevice device = espDevices.get(deviceID);
            ProvisionListener provisionListener = new ProvisionListener() {
                @Override
                public void createSessionFailed(Exception e) {
                    call.error("Create Session Failed",e);
                }

                @Override
                public void wifiConfigSent() {
                }

                @Override
                public void wifiConfigFailed(Exception e) {
                    call.error("WiFi Config Failed",e);
                }

                @Override
                public void wifiConfigApplied() {
                }

                @Override
                public void wifiConfigApplyFailed(Exception e) {
                    call.error("WiFi Config Apply Failed",e);
                }

                @Override
                public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                    call.error(failureReason.toString());
                }

                @Override
                public void deviceProvisioningSuccess() {
                    call.success();
                }

                @Override
                public void onProvisioningFailed(Exception e) {
                    call.error("Provisioning Failed",e);
                }
            };
            device.provision(ssid,passphrase,provisionListener);
        } else {
            call.reject("Invalid Device ID provided");
        }
    }

    @Subscribe
    public void onDeviceConnectionEvent(DeviceConnectionEvent deviceConnectionEvent){
        EventBus.getDefault().unregister(this);
        PluginCall savedCall = getSavedCall();
        if(deviceConnectionEvent.getEventType() == ESPConstants.EVENT_DEVICE_CONNECTED){
            savedCall.success();
        } else {
            savedCall.error("Couldn't connect to device");
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.handleRequestPermissionsResult(requestCode,permissions,grantResults);
        PluginCall savedCall = getSavedCall();
        if(savedCall == null){
            return;
        }
        for(int result: grantResults){
            if(result == PackageManager.PERMISSION_DENIED){
                savedCall.error("Permission Denied by User");
                return;
            }
        }
        if(requestCode == REQUEST_ACCESS_FINE_LOCATION){
            savedCall.success();
        }
    }
}
