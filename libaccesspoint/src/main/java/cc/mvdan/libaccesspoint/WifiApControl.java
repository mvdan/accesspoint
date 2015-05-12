/**
 * Copyright 2015 Daniel Martí
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.mvdan.libaccesspoint;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class WifiApControl {

	private static final String TAG = "WifiApControl";

	private static Method getWifiApConfiguration;
	private static Method getWifiApState;
	private static Method isWifiApEnabled;
	private static Method setWifiApEnabled;

	static {
		for (Method method : WifiManager.class.getDeclaredMethods()) {
			String name = method.getName();
			if (name.equals("getWifiApConfiguration")) {
				getWifiApConfiguration = method;
			} else if (name.equals("getWifiApState")) {
				getWifiApState = method;
			} else if (name.equals("isWifiApEnabled")) {
				isWifiApEnabled = method;
			} else if (name.equals("setWifiApEnabled")) {
				setWifiApEnabled = method;
			}
		}
	}

	public static final int WIFI_AP_STATE_DISABLING = 10;
	public static final int WIFI_AP_STATE_DISABLED  = 11;
	public static final int WIFI_AP_STATE_ENABLING  = 12;
	public static final int WIFI_AP_STATE_ENABLED   = 13;
	public static final int WIFI_AP_STATE_FAILED    = 14;

	public static final int STATE_DISABLING = WIFI_AP_STATE_DISABLING;
	public static final int STATE_DISABLED  = WIFI_AP_STATE_DISABLED;
	public static final int STATE_ENABLING  = WIFI_AP_STATE_ENABLING;
	public static final int STATE_ENABLED   = WIFI_AP_STATE_ENABLED;
	public static final int STATE_FAILED    = WIFI_AP_STATE_FAILED;

	private static boolean isSoftwareSupported() {
		return (getWifiApState != null
				&& isWifiApEnabled != null
				&& setWifiApEnabled != null
				&& getWifiApConfiguration != null);
	}

	private static boolean isHardwareSupported() {
		// TODO: implement via native code
		return true;
	}

	public static boolean isSupported() {
		return isSoftwareSupported() && isHardwareSupported();
	}

	private static final String fallbackWifiDevice = "wlan0";

	private final WifiManager wm;
	private final String wifiDevice;

	private WifiApControl(WifiManager wm, String wifiDevice) {
		this.wm = wm;
		this.wifiDevice = wifiDevice;
	}

	public static WifiApControl getApControl(WifiManager wm) {
		if (!isSupported()) {
			return null;
		}

		final String wifiDevice = getWifiDeviceName(wm);
		return new WifiApControl(wm, wifiDevice);
	}

	private static String getWifiDeviceName(final WifiManager wifiManager) {
		final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		final String wifiMacString = wifiInfo.getMacAddress();
		final byte[] wifiMacBytes = macAddressToByteArray(wifiMacString);
		final BigInteger wifiMac = new BigInteger(wifiMacBytes);

		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();

				final byte[] hardwareAddress = iface.getHardwareAddress();
				if (hardwareAddress == null) {
					continue;
				}

				final BigInteger currentMac = new BigInteger(hardwareAddress);
				if (currentMac.equals(wifiMac)) {
					return iface.getName();
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, "", e);
		}

		Log.w(TAG, "Falling back to the default wifi device name: " + fallbackWifiDevice);
		return fallbackWifiDevice;
	}

	static private byte[] macAddressToByteArray(final String macString) {
		final String[] mac = macString.split("[:\\s-]");
		final byte[] macAddress = new byte[6];
		for (int i = 0; i < mac.length; i++) {
			macAddress[i] = Integer.decode("0x" + mac[i]).byteValue();
		}
		return macAddress;
	}

	public boolean isWifiApEnabled() {
		try {
			return (Boolean) isWifiApEnabled.invoke(wm);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return false;
	}

	public boolean isEnabled() {
		return isWifiApEnabled();
	}

	public static int newStateNumber(int state) {
		// WifiManager's state constants were changed around Android 4.0
		if (state < 10) {
			return state + 10;
		}
		return state;
	}

	public int getWifiApState() {
		try {
			return newStateNumber((Integer) getWifiApState.invoke(wm));
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return -1;
	}

	public int getState() {
		return getWifiApState();
	}

	public WifiConfiguration getWifiApConfiguration() {
		try {
			return (WifiConfiguration) getWifiApConfiguration.invoke(wm);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return null;
	}

	public WifiConfiguration getConfiguration() {
		return getWifiApConfiguration();
	}

	public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
		try {
			return (Boolean) setWifiApEnabled.invoke(wm, config, enabled);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		return false;
	}

	public boolean setEnabled(WifiConfiguration config, boolean enabled) {
		return setWifiApEnabled(config, enabled);
	}

	public boolean enable() {
		return setWifiApEnabled(getWifiApConfiguration(), true);
	}

	public boolean disable() {
		return setWifiApEnabled(null, false);
	}

	public InetAddress getInetAddress() {
		if (!isEnabled()) {
			return null;
		}

		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();

				Enumeration<InetAddress> addrs = iface.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();

					if (addr.isLoopbackAddress()) {
					       continue;
					}

					final String ifaceName = iface.getDisplayName();
					if (ifaceName.contains(wifiDevice)) {
						return addr;
					}
				}
			}

		} catch (SocketException e) {
			Log.e(TAG, "", e);
		}
		return null;
	}

	public static class Client {
		public String IPAddr;
		public String HWAddr;

		public Client(String IPAddr, String HWAddr) {
			this.IPAddr = IPAddr;
			this.HWAddr = HWAddr;
		}
	}

	public List<Client> getClients() {
		if (!isEnabled()) {
			return null;
		}
		final List<Client> result = new ArrayList<Client>();

		// Basic sanity checks
		final Pattern macPattern = Pattern.compile("..:..:..:..:..:..");

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				final String[] parts = line.split(" +");
				if (parts == null || parts.length < 6) {
					continue;
				}

				final String IPAddr = parts[0];
				final String HWAddr = parts[3];
				final String device = parts[5];

				if (!device.equals(wifiDevice)) {
					continue;
				}

				if (!macPattern.matcher(parts[3]).find()) {
					continue;
				}

				result.add(new Client(IPAddr, HWAddr));
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
		}

		return result;
	}
}
