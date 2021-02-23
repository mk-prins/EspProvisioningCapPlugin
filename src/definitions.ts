declare module '@capacitor/core' {
  interface PluginRegistry {
    EspProvisioning: EspProvisioningPlugin;
  }
}

export interface EspProvisioningPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  foo(data: unknown): Promise<unknown>;
}
