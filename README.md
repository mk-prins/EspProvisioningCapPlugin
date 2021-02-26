# Espressif SDK Capacitor Plugin
This is a [capacitor](https://capacitorjs.com/docs) plugin for using the [Espressif SDK for Unified Provisioning](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/provisioning/provisioning.html). It contains support for both Android and iOS.

 - [Installing](#installing)
   - [Configure Android](#configure-android)
   - [Configure iOS](#configure-ios)
 - [Developing](#developing)
   - [Local development](#local-development)
 - [Additional resources](#additional-resources)

## Installing
Depending on wether you're using `yarn` or `npm` run the following inside the project you wish to add this plugin into.
```
yarn add https://github.com/mk-prins/EspProvisioningCapPlugin
-OR-
npm install --save https://github.com/mk-prins/EspProvisioningCapPlugin
```

### Configure Android
To able to use the plugin you need to add it to your project by updating it's `MainActivity.java`.

Make sure to import the plugin at the top of the file.
```
import com.myapp.plugins.espprovisioning.EspProvisioning;
```

Then add the plugin inside the `init()` method.
```
// Initializes the Bridge
this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
    // Additional plugins you've installed go here
    add(EspProvisioning.class);
}});
```

Finally you need to make capacitor inside your project aware of the added plugin.
```
cap sync
-OR-
ionic cap sync
```

### Configure iOS
_Work in progress_

## Developing
This project uses `yarn` as the node package manager. Make sure to install all the depencies.
```
yarn
```

Whenever you've made any changes you need to (re)build to apply them.
```
yarn build
```

### Local development
To make local development significantly easier, you can use the `link` feature off `npm` or `yarn`. This will allow you to directly debug any changes without any of the hassle.

```
/* Inside this project */
yarn link
-OR-
npm link

/* Inside of your own project */
yarn link esp-provisioning-plugin
-OR-
npm link esp-provisioning-plugin
```

**Note!** Once done, don't forget to `unlink`.
```
/* Inside of your own project */
yarn unlink esp-provisioning-plugin
-OR-
npm unlink esp-provisioning-plugin

/* Inside this project */
yarn unlink
-OR-
npm unlink
```

## Additional resources
For more info on the packages used checkout their official documentation.
 - [Capacitor](https://capacitorjs.com/docs)
 - [Espressif Unified Provisioning](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/provisioning/provisioning.html)
   - [SDK for Android](https://github.com/espressif/esp-idf-provisioning-android)
   - [SDK for iOS](https://github.com/espressif/esp-idf-provisioning-ios)