package com.myapp.plugins.espprovisioning;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

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

import java.util.ArrayList;

@NativePlugin
public class EspProvisioning extends Plugin {

    ESPProvisionManager espProvisionManager;

    public void load() {
        espProvisionManager = ESPProvisionManager.getInstance(this.getContext());
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
    public void scanQRCode (PluginCall call) {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    @PluginMethod
    public void searchBleEspDevices (final PluginCall call) {
        BleScanListener bleScanListener = new BleScanListener() {

            ArrayList<BluetoothDevice> devices;
            ArrayList<ScanResult> scanResults;

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
                ret.put("devices", devices);
                ret.put("scan_results", scanResults);
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
    }

    // @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})
    @PluginMethod
    public void stopBleScan (PluginCall call) {
        espProvisionManager.stopBleScan();
        call.success();
    }

    // @RequiresPermission(allOf = {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE})
    @PluginMethod
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
