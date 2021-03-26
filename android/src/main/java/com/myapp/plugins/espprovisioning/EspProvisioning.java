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
import com.getcapacitor.JSArray;
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
    private Hashtable<String,ScanResult> scanResults;
    private Hashtable<Integer,ESPDevice> espDevices = new Hashtable<>();

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
    public void requestPermissions(PluginCall call){
        String[] permissions = {ACCESS_FINE_LOCATION,CAMERA};
        ActivityCompat.requestPermissions(getActivity(),permissions,1);
        call.success();
    }

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void createESPDevice(final PluginCall call) {
        if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                        ret.put("id",deviceID);
                        ret.put("name",espDevice.getDeviceName());
                        ret.put("pop",espDevice.getProofOfPossession());
                        ret.put("transportType",espDevice.getTransportType());
                        ret.put("securityType",espDevice.getSecurityType());
                        ret.put("primaryServiceUuid",espDevice.getPrimaryServiceUuid());
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
        if (ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    @PluginMethod
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void connectToDevice(PluginCall call){
        Integer deviceID = call.getInt("device");
        if(validateDeviceID(deviceID)){
            ESPDevice device = espDevices.get(deviceID);
            device.connectToDevice();
            //TO-DO check if device is connected
            JSObject ret = new JSObject();
            ret.put("Return Message", "Device Connected... I hope");
            call.success(ret);
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
            final JSObject ret = new JSObject();
            ESPDevice device = espDevices.get(deviceID);
            ProvisionListener provisionListener = new ProvisionListener() {
                @Override
                public void createSessionFailed(Exception e) {
                    call.error("Create Session Failed",e);
                }

                @Override
                public void wifiConfigSent() {
                    ret.put("wifiConfigSent", "success");
                }

                @Override
                public void wifiConfigFailed(Exception e) {
                    call.error("WiFi Config Failed",e);
                }

                @Override
                public void wifiConfigApplied() {
                    ret.put("wifiConfigApplied", "success");
                }

                @Override
                public void wifiConfigApplyFailed(Exception e) {
                    call.error("WiFi Config Apply Failed",e);
                }

                @Override
                public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                    call.reject(failureReason.toString());
                }

                @Override
                public void deviceProvisioningSuccess() {
                    ret.put("deviceProvisioning", "success");
                    call.success(ret);
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
}
