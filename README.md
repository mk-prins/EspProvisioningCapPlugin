# Espressif SDK Capacitor Plugin

## Table of contents
* [General info](#general-info)
* [Prerequisites](#prerequisites)
* [Deploying](#deploying)
* [Developing](#developing) 
* [Features](#features)
* [Status](#status)
* [License](#license)
* [Credits](#credits)

## General info
This is a [capacitor](https://capacitorjs.com/docs) plugin for using the [Espressif SDK for Unified Provisioning](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/provisioning/provisioning.html). It contains support for both Android and iOS.

## Prerequisites
 - [Node](https://nodejs.org/en/) installed
 - [Yarn](https://yarnpkg.com/) installed
 - (Ionic) Capacitor installed globally with yarn or npm
 - Xcode and Cocoapods installed for iOS
 - Android Studio installed for Android

## Deploying
To use this package in any project you'll need to do the following.

### Adding the dependency
Run the following inside the project you wish to add this plugin into.
```bash
yarn add https://github.com/energietransitie/esp-provisioning-capacitor-plugin#<release_version>
# e.g. yarn add https://github.com/energietransitie/esp-provisioning-capacitor-plugin#v1.0.0
```

### Configure Android
To able to use the plugin you need to add it to your project by updating it's `MainActivity.java`.

Make sure to import the plugin at the top of the file.
```
import com.myapp.plugins.espprovisioning.EspProvisioning;
```

Then add the plugin inside the `init()` method.
```java
// Initializes the Bridge
this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
    // Additional plugins you've installed go here
    add(EspProvisioning.class);
}});
```

Finally you need to make capacitor inside your project aware of the added plugin.
```bash
cap sync android
# -OR-
ionic cap sync android
```

### Configure iOS
You need to make capacitor aware of the added plugin using the sync script. Capacitor should take care of the Podfiles and the installment of the defined dependencies.
```bash
cap sync ios
# -OR-
ionic cap sync ios
```

#### Permissions
The following three permissions need to be added to your project's `Info.plist`. Update the description strings to match your application.
```xml
<key>NSBluetoothAlwaysUsageDescription</key>
<string>Your bluetooth is required for xyz benefits for you...</string>
<key>NSLocationWhenInUseUsageDescription</key>
<string>Your location is required for xyz benefits for you...</string>
<key>NSLocalNetworkUsageDescription</key>
<string>Your local network usage info is required for xyz benefits for you...</string>
```

### Usage
If succesfully configured for the desired platform(s), you'll be able to simply import the plugin through capacitor using;
```js
import { Plugins } from '@capacitor/core';

const { EspProvisioning } = Plugins;
```

## Developing
This project uses `yarn` as the node package manager. Make sure to install all the depencies.
```bash
yarn
```

Whenever you've made any changes you need to (re)build to apply them.
```bash
yarn build
```

### Local development
To make local development significantly easier, you can use the `link` feature off `npm` or `yarn`. This will allow you to directly debug any changes without any of the hassle.

```bash
# Inside this project
yarn link

# Inside of your own project
yarn link esp-provisioning-plugin
```

**Note!** Once done, don't forget to `unlink`.
```bash
# Inside of your own project
yarn unlink esp-provisioning-plugin

# Inside this project
yarn unlink
```

## Features
List of features ready and TODOs for future development. Ready:

| Method | Params | Returns | Note |
| ------ | ------ | ------- | ---- |
| requestLocationPermissions | - | `Promise<unknown>` | Android Only |
| checkLocationPermissions | - | `Promise<{ permissionStatus: string}>` | Android Only |
| createESPDevice | `{ name: string, transportType: TransportType, securityType: SecurityType, pop: string }` | `Promise<ESPDevice>` | - |
| searchBleEspDevices | `{ prefix?: string }` | `Promise<{ count: number; devices: ESPDevice[]}>` | - |
| searchWifiEspDevices | `{ prefix?: string }` | `Promise<{ count: number; devices: ESPDevice[]}>` | Android Only |
| stopBleScan | - | `Promise<void>` | - |
| connectToDevice | `{ device: number }` | `Promise<Record<string, string>>` | - |
| scanWifiList | `{ device: number }` | `Promise<{ count: number; networks: WifiNetwork[]}>` | - |
| provision | `{ device: number, ssid: string, passphrase: string }` | `Promise<Record<string, string>>` | - |

To-do:
 * Automated version management
 * Improved typings and exporting type declaration.

## Status
Project is: _in progress_

This is currently still in development. The current version is usable and stable, but could use some internal cleanup and improvements.

## License
This software is available under the [Apache 2.0 license](./LICENSE.md), Copyright 2021 [Research group Energy Transition, Windesheim University of Applied Sciences](https://windesheim.nl/energietransitie) 

## Credits
This software is a collaborative effort of the following students:
* Wietske Veneberg  ·  [@WVeneberg](https://github.com/WVeneberg)
* Amicia Smit  ·  [@AmiciaSmit](https://github.com/AmiciaSmit)
* Marco Prins  ·  [@mk-prins](https://github.com/mk-prins)

Product owner:
* Henri ter Hofte  ·  [@henriterhofte](https://github.com/henriterhofte)

We use and gratefully acknowledge the efforts of the makers of the following source code and libraries:

* [Capacitor](https://github.com/ionic-team/capacitor), by Drifty Co., licensed under [MIT](https://github.com/ionic-team/capacitor/blob/main/LICENSE)
* [esp-idf-provisioning-ios](https://github.com/espressif/esp-idf-provisioning-ios), by Espressif, licensed under [Apache 2.0](https://github.com/espressif/esp-idf-provisioning-ios/blob/master/LICENSE)
* [esp-idf-provisioning-android](https://github.com/espressif/esp-idf-provisioning-android), by Espressif, licensed under [Apache 2.0](https://github.com/espressif/esp-idf-provisioning-android/blob/master/LICENSE)