declare module '@capacitor/core' {
    interface PluginRegistry {
        EspProvisioning: EspProvisioningPlugin;
    }
}

enum TransportType {
    TRANSPORT_BLE,
    TRANSPORT_SOFTAP
}

enum SecurityType {
    SECURITY_0,
    SECURITY_1
}

interface ESPDevice {
    id: number;
    device: {
        name: string;
        transport_type: TransportType;
        security_type: SecurityType;
        proof_of_possesion: string;
        primary_service_uuid: string;
        capabilities: unknown;
        version: string;
    }
}

interface WifiNetwork {
    ssid: string;
    channel: string;
    rssi: string;
}

export interface EspProvisioningPlugin {
    createEspDevice(data: { transportType: TransportType, securityType: SecurityType }): Promise<ESPDevice>;
    scanQRCode(data: unknown): Promise<ESPDevice>;
    searchBleEspDevices(data?: { prefix: string }): Promise<{ count: number; devices: ESPDevice[]}>;
    searchWifiEspDevices(data?: { prefix: string }): Promise<{ count: number; devices: ESPDevice[]}>;
    stopBleScane(): Promise<void>;
    connectToDevice(data: { device: number }): Promise<Record<string, string>>;
    scanWifiList(data: { device: number }): Promise<{ count: number; networks: WifiNetwork[]}>;
    provision(data: { device: number }): Promise<Record<string, string>>;
}
