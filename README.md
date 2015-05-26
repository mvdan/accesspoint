# accesspoint

Manage wireless access points on Android 2.2 or later.

### Quick start

```java
WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
WifiApControl apControl = WifiApControl.getInstance(context);

boolean enabled = apControl.isEnabled();
int state = apControl.getState();

WifiConfiguration config = apControl.getConfiguration();
Inet4Address addr4 = apControl.getInet4Address();
Inet6Address addr6 = apControl.getInet6Address();

List<WifiApControl.Client> clients = apControl.getClients()

wifiManager.setWifiEnabled(false);
apControl.enable();

apControl.disable();
wifiManager.setWifiEnabled(true);
```

### Features

 * Enabling and disabling your AP
 * Configuring your AP via `WifiConfiguration`
 * Getting your device's IP address in your AP network
 * Getting the list of clients connected to your AP

### Permissions

You will need the following:

```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET"/>
```

### How does it work?

Enabling, disabling and configuring of wireless Access Points are all
unaccessible in the SDK behind hidden methods in `WifiManager`. Reflection is
used to get access to those methods.

Getting your own IP address is done by getting the IP address that is
associated with the wireless network interface.

Getting the list of clients consists of parsing `/proc/net/arp` and parsing
each line to see what devices are neighbours in our wireless network.

An extra method is available to get the list of reachable clients, since the
arp table is cached for up to a few minutes. The method asynchronously tests
the reachability of each client.

### License

Published under the Apache2 license.
