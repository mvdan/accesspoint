# libaccesspoint

Manage wireless access points in Android.

Works on any device that supports them. Requires Android 2.2 since the WiFi
hotspot functionality was added in that version.

### Features

 * Enabling and disabling your AP
 * Configuring your AP via `WifiConfiguration`
 * Getting your device's IP address in your AP network
 * Getting the list of clients connected to your AP

### Permissions

You will need the following:

	<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
	<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
	<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
	<uses-permission android:name="android.permission.INTERNET"/>

### How does it work?

Enabling, disabling and configuring of wireless Access Points are all
unaccessible in the SDK behind hidden methods in `WifiManager`. Reflection is
used to get access to those methods.

Getting your own IP address is done by getting the IP address that is
associated with the wireless network interface.

Getting the list of clients consists of parsing `/proc/net/arp` and parsing
each line to see what devices are neighbours in our wireless network.

### License

Published under the Apache2 license.
