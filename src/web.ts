import { WebPlugin } from '@capacitor/core';
import { EspProvisioningPlugin } from './definitions';

enum TransportType {
  TRANSPORT_BLE,
  TRANSPORT_SOFTAP
}

enum SecurityType {
  SECURITY_0,
  SECURITY_1
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

export class EspProvisioningWeb extends WebPlugin implements EspProvisioningPlugin {
  constructor() {
    super({
      name: 'EspProvisioning',
      platforms: ['web'],
    });
  }

  async createEspDevice(data: { transportType: TransportType, securityType: SecurityType }): Promise<ESPDevice> {
    console.log('[Web]: createEspDevice', data);
    return data as {} as ESPDevice;
  }

  async getEspDevice(): Promise<ESPDevice> {
    console.log('[Web]: getEspDevice');
    return Promise.resolve({} as ESPDevice);
  }

  async scanQRCode(data: unknown): Promise<unknown> {
    console.log('[Web]: scanQRCode', data);
    return data;
  }

  async searchBleEspDevices(data?: { prefix: string }): Promise<unknown> {
    console.log('[Web]: searchBleEspDevices', data);
    return data;
  }

  async stopBleScane(): Promise<void> {
    console.log('[Web]: stopBleScane');
    return Promise.resolve();
  }

  async searchWifiEspDevices(data?: { prefix: string }): Promise<unknown> {
    console.log('[Web]: searchWifiEspDevices', data);
    return data;
  }

}

const EspProvisioning = new EspProvisioningWeb();

export { EspProvisioning };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(EspProvisioning);
