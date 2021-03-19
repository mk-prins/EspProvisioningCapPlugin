import Foundation
import Capacitor
import ESPProvision

public class ProvisioningManager: NSObject {
    
    private var rootViewController: UIViewController
    private var deviceList:[ESPDevice] = []
    
    init(viewController: UIViewController) {
        rootViewController = viewController
    }
    
    private func createDeviceID(espDevice: ESPDevice) -> Int {
        let deviceID = self.deviceList.count
        self.deviceList.insert(espDevice, at: deviceID)
        
        return deviceID
    }
    
    private func validateDeviceID(deviceID: Int?) -> Bool {
        if (deviceID == nil || (deviceID != nil && !self.deviceList.indices.contains(deviceID!))) {
            return false
        }
        return true
    }
    
    private func formatDeviceList(deviceList: [ESPDevice]) -> [[String:Any]] {
        var serialisedDevices = [[String:Any]]();
        for device in deviceList {
            serialisedDevices.append([
                "id": createDeviceID(espDevice: device),
                "device": Formatter.serialiseEspDevice(device: device)
            ])
        }
        return serialisedDevices
    }

    public func createESPDevice(call: CAPPluginCall) {
        let name = call.getString("name")!
        let tpType = Formatter.espTransportStringToEnum(str: call.getString("transport")) ?? .ble
        let secType = Formatter.espSecurityStringToEnum(str: call.getString("security")) ?? .unsecure
        let pop = call.getString("pop")!
        
        ESPProvision.ESPProvisionManager.shared.createESPDevice(deviceName: name, transport: tpType, security: secType, proofOfPossession: pop){ espDevice, error in
            if (espDevice?.name == nil) {
                call.reject("Device could not be created")
                return
            }
            call.resolve([
                "id": self.createDeviceID(espDevice: espDevice!),
                "device": Formatter.serialiseEspDevice(device: espDevice!)
            ])
        }
    }
    
    public func searchBleEspDevices(call: CAPPluginCall) {
        let prefix = call.getString("prefix") ?? "";
        let tpType = Formatter.espTransportStringToEnum(str: call.getString("transport")) ?? .ble;
        let secType = Formatter.espSecurityStringToEnum(str: call.getString("security")) ?? .unsecure;
        
        ESPProvision.ESPProvisionManager.shared.searchESPDevices(devicePrefix: prefix, transport:tpType, security:secType) { deviceList, _ in
            if (deviceList == nil || (deviceList != nil && !(deviceList!.count > 0))) {
                call.reject("No devices found")
                return
            }
            call.resolve([
                "count": deviceList!.count,
                "devices": self.formatDeviceList(deviceList: deviceList!)
            ])
        }
    }
    
    public func scanQRCode(call: CAPPluginCall) {
        DispatchQueue.main.async {
            let qrScanViewController = QRScanViewController()
            self.rootViewController.present(qrScanViewController, animated: true, completion: nil)
            
            ESPProvisionManager.shared.scanQRCode(scanView: qrScanViewController.CameraView) { espDevice, error in
                if (espDevice?.name == nil) {
                    call.reject("Device could not be found");
                    return
                }
                call.success([
                    "id": self.createDeviceID(espDevice: espDevice!),
                    "device": Formatter.serialiseEspDevice(device: espDevice!)
                ])
            }
        }
    }
    
    public func connectToDevice(call: CAPPluginCall) {
        let deviceID = call.getInt("device")
        guard self.validateDeviceID(deviceID: deviceID) else {
            call.reject("Invalid Device ID provided")
            return
        }
        
        let device = self.deviceList[deviceID!]
        device.connect() { status in
            if case ESPSessionStatus.connected = status {
                call.success([
                    "status": "connected"
                ])
                return
            }
            call.reject("Unable to connect to device")
        }
    }
    
    public func scanWifiList(call: CAPPluginCall) {
        let deviceID = call.getInt("device")
        guard self.validateDeviceID(deviceID: deviceID) else {
            call.reject("Invalid Device ID provided")
            return
        }
        
        let device = self.deviceList[deviceID!]
        device.scanWifiList { wifiList, _ in
            if (wifiList != nil && !(wifiList!.count > 0)) {
                call.reject("Unable to find any Wifi Networks.")
            }
            call.success([
                "count": wifiList!.count,
                "networks": Formatter.serialiseWifiList(wifiNetworkList: wifiList!)
            ])
        }
    }
    
    public func provision(call: CAPPluginCall) {
        let deviceID = call.getInt("device")
        let ssid = call.getString("ssid")
        let passphrase = call.getString("passphrase") ?? ""
        
        guard self.validateDeviceID(deviceID: deviceID) else {
            call.reject("Invalid Device ID provided")
            return
        }
        guard ssid != nil else {
            call.reject("Invalid SSID provided.")
            return
        }
        
        let device = self.deviceList[deviceID!]
        device.provision(ssid: ssid!, passPhrase: passphrase) { status in
            if case ESPProvisionStatus.failure = status {
                call.reject("Failed to provision device.")
            }
            call.success([
                "status": "success"
            ])
        }
    }

}
