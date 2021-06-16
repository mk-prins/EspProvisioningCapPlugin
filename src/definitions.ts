declare module '@capacitor/core' {
    interface PluginRegistry {
        EspProvisioning: EspProvisioningPlugin;
    }
}

export enum TransportType {
    TRANSPORT_BLE,
    TRANSPORT_SOFTAP
}

export enum SecurityType {
    SECURITY_0,
    SECURITY_1
}
export interface ESPDevice {
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

export interface WifiNetwork {
    ssid: string;
    channel: string;
    rssi: string;
    security: boolean;
}

export interface EspProvisioningPlugin {
    requestLocationPermissions(): Promise<unknown>;
    checkLocationPermissions(data: unknown): Promise<{ permissionStatus: string}>;
    createESPDevice(data: { name: string, transportType: TransportType, securityType: SecurityType, pop: string }): Promise<ESPDevice>;
    searchBleEspDevices(data?: { prefix: string }): Promise<{ count: number; devices: ESPDevice[]}>;
    searchWifiEspDevices(data?: { prefix: string }): Promise<{ count: number; devices: ESPDevice[]}>;
    stopBleScan(): Promise<void>;
    connectToDevice(data: { device: number }): Promise<Record<string, string>>;
    scanWifiList(data: { device: number }): Promise<{ count: number; networks: WifiNetwork[]}>;
    provision(data: { device: number, ssid: string, passphrase: string }): Promise<Record<string, string>>;
}
