import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(EspProvisioning)
public class EspProvisioning: CAPPlugin {
    
    private var provisioningManager: ProvisioningManager!
    
    @objc override public func load() {
        self.provisioningManager = ProvisioningManager()
    }

    @objc func createESPDevice(_ call: CAPPluginCall) {
        self.provisioningManager.createESPDevice(call: call)
    }
    
    @objc func searchBleEspDevices(_ call: CAPPluginCall) {
        self.provisioningManager.searchBleEspDevices(call: call)
    }
    
    @objc func searchWifiEspDevices(_ call: CAPPluginCall) {
        call.reject("SoftAP search is not supported in iOS currently.")
    }
    
    @objc func connectToDevice(_ call: CAPPluginCall) {
        self.provisioningManager.connectToDevice(call: call)
    }
    
    @objc func scanWifiList(_ call: CAPPluginCall) {
        self.provisioningManager.scanWifiList(call: call)
    }
    
    @objc func provision(_ call: CAPPluginCall) {
        self.provisioningManager.provision(call: call)
    }

}
