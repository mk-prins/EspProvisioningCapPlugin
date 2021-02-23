package com.myapp.plugins.espprovisioning;

import android.content.Context;

import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin
public class EspProvisioning extends Plugin {

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }

    @PluginMethod
    public void foo(PluginCall call) {
        ESPConstants.TransportType transportType = ESPConstants.TransportType.TRANSPORT_SOFTAP;
        ESPConstants.SecurityType securityType = ESPConstants.SecurityType.SECURITY_1;

        ESPDevice espDevice = ESPProvisionManager.getInstance(this.getContext()).createESPDevice(transportType, securityType);

        String name = espDevice.getDeviceName();
        String secType = espDevice.getSecurityType().toString();

        String value = "[Native]: foo";

        JSObject ret = new JSObject();
        ret.put("value", value);
        ret.put("name", name);
        ret.put("secType", secType);
        call.success(ret);
    }
}
