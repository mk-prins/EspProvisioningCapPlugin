import { WebPlugin } from '@capacitor/core';
import { EspProvisioningPlugin } from './definitions';

export class EspProvisioningWeb extends WebPlugin implements EspProvisioningPlugin {
  constructor() {
    super({
      name: 'EspProvisioning',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('[Web]: ECHO', options);
    return options;
  }

  async foo(data: unknown): Promise<unknown> {
    console.log('[Web]: foo', data);
    return data;
  }
}

const EspProvisioning = new EspProvisioningWeb();

export { EspProvisioning };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(EspProvisioning);
