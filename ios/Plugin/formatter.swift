import Foundation
import ESPProvision

public class Formatter {
    
    public static func espSecurityStringToEnum(str: String?) -> ESPSecurity? {
        switch str?.lowercased() {
            case "secure":
                return ESPSecurity.secure
            case "unsecure":
                return ESPSecurity.unsecure
            default:
                return nil
        }
    }
    
    public static func espTransportStringToEnum(str: String?) -> ESPTransport? {
        switch str?.lowercased() {
            case "ble":
                return ESPTransport.ble
            case "softap":
                return ESPTransport.softap
            default:
                return nil
        }
    }
    
    public static func espSecurityEnumToString(enumVal: ESPSecurity) -> String {
        switch enumVal {
            case .secure:
                return "secure"
            case .unsecure:
                return "unsecure"
        }
    }
    
    public static func espTransportEnumToString(enumVal: ESPTransport) -> String {
        switch enumVal {
            case .ble:
                return "ble"
            case .softap:
                return "softap"
        }
    }
    
    public static func serialiseEspDevice(device: ESPDevice) -> [String: String] {
        return [
            "name": device.name,
            "transport": Formatter.espTransportEnumToString(enumVal: device.transport),
            "security": Formatter.espSecurityEnumToString(enumVal: device.security)
        ]
    }
    
    public static func serialiseWifiList(wifiNetworkList: [ESPWifiNetwork]) -> [[String: String]] {
        var serialisedList = [[String:String]]()
        for wifiNetwork in wifiNetworkList {
            serialisedList.append([
                "ssid": wifiNetwork.ssid,
                "channel": String(wifiNetwork.channel),
                "rssi": String(wifiNetwork.rssi)
//                public var bssid: Data = SwiftProtobuf.Internal.emptyData
//                public var auth: Espressif_WifiAuthMode = .open
//                public var unknownFields = SwiftProtobuf.UnknownStorage()
            ])
        }
        return serialisedList
    }

}
