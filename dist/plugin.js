var capacitorPlugin = (function (exports, core) {
    'use strict';

    exports.TransportType = void 0;
    (function (TransportType) {
        TransportType[TransportType["TRANSPORT_BLE"] = 0] = "TRANSPORT_BLE";
        TransportType[TransportType["TRANSPORT_SOFTAP"] = 1] = "TRANSPORT_SOFTAP";
    })(exports.TransportType || (exports.TransportType = {}));
    exports.SecurityType = void 0;
    (function (SecurityType) {
        SecurityType[SecurityType["SECURITY_0"] = 0] = "SECURITY_0";
        SecurityType[SecurityType["SECURITY_1"] = 1] = "SECURITY_1";
    })(exports.SecurityType || (exports.SecurityType = {}));

    var __awaiter = (undefined && undefined.__awaiter) || function (thisArg, _arguments, P, generator) {
        function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
        return new (P || (P = Promise))(function (resolve, reject) {
            function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
            function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
            function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
            step((generator = generator.apply(thisArg, _arguments || [])).next());
        });
    };
    var TransportType;
    (function (TransportType) {
        TransportType[TransportType["TRANSPORT_BLE"] = 0] = "TRANSPORT_BLE";
        TransportType[TransportType["TRANSPORT_SOFTAP"] = 1] = "TRANSPORT_SOFTAP";
    })(TransportType || (TransportType = {}));
    var SecurityType;
    (function (SecurityType) {
        SecurityType[SecurityType["SECURITY_0"] = 0] = "SECURITY_0";
        SecurityType[SecurityType["SECURITY_1"] = 1] = "SECURITY_1";
    })(SecurityType || (SecurityType = {}));
    class EspProvisioningWeb extends core.WebPlugin {
        constructor() {
            super({
                name: 'EspProvisioning',
                platforms: ['web'],
            });
        }
        createESPDevice(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: createEspDevice', data);
                return data;
            });
        }
        scanQRCode(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: scanQRCode', data);
                return data;
            });
        }
        searchBleEspDevices(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: searchBleEspDevices', data);
                return data;
            });
        }
        stopBleScan() {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: stopBleScane');
                return Promise.resolve();
            });
        }
        searchWifiEspDevices(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: searchWifiEspDevices', data);
                return data;
            });
        }
        connectToDevice(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: connectToDevice', data);
                return data;
            });
        }
        scanWifiList(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: scanWifiList', data);
                return data;
            });
        }
        provision(data) {
            return __awaiter(this, void 0, void 0, function* () {
                console.log('[Web]: provision', data);
                return data;
            });
        }
    }
    const EspProvisioning = new EspProvisioningWeb();
    core.registerWebPlugin(EspProvisioning);

    exports.EspProvisioning = EspProvisioning;
    exports.EspProvisioningWeb = EspProvisioningWeb;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

}({}, capacitorExports));
//# sourceMappingURL=plugin.js.map
