declare module '@capacitor/core' {
    interface PluginRegistry {
        EspProvisioning: EspProvisioningPlugin;
    }
}
declare enum TransportType {
    TRANSPORT_BLE = 0,
    TRANSPORT_SOFTAP = 1
}
declare enum SecurityType {
    SECURITY_0 = 0,
    SECURITY_1 = 1
}
interface ESPDevice {
    name: string;
    transport_type: TransportType;
    security_type: SecurityType;
    proof_of_possesion: string;
    primary_service_uuid: string;
    capabilities: unknown;
    version: string;
}
export interface EspProvisioningPlugin {
    createEspDevice(data: {
        transportType: TransportType;
        securityType: SecurityType;
    }): Promise<ESPDevice>;
    getEspDevice(): Promise<ESPDevice>;
    scanQRCode(data: unknown): Promise<unknown>;
    searchBleEspDevices(data?: {
        prefix: string;
    }): Promise<unknown>;
    stopBleScane(): Promise<void>;
    searchWifiEspDevices(data?: {
        prefix: string;
    }): Promise<unknown>;
}
export {};
