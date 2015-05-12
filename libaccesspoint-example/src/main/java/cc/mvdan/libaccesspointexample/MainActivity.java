package cc.mvdan.libaccesspointexample;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.lang.StringBuilder;
import java.net.InetAddress;

import cc.mvdan.libaccesspoint.WifiApControl;

public class MainActivity extends Activity {

	private WifiApControl apControl;
	private WifiManager wifiManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		apControl = WifiApControl.getApControl(wifiManager);

		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(1000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								refresh();
							}
						});
					}
				} catch (InterruptedException e) {
				}
			}
		};
		t.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	private static String stateString(final int state) {
		switch (state) {
			case WifiApControl.STATE_FAILED:
				return "FAILED";
			case WifiApControl.STATE_DISABLED:
				return "DISABLED";
			case WifiApControl.STATE_DISABLING:
				return "DISABLING";
			case WifiApControl.STATE_ENABLED:
				return "ENABLED";
			case WifiApControl.STATE_ENABLING:
				return "ENABLING";
			default:
				return "UNKNOWN!";
		}
	}

	private void refresh() {
		final TextView tv = (TextView) findViewById(R.id.text1);
		final StringBuilder sb = new StringBuilder();
		if (!WifiApControl.isSupported()) {
			sb.append("Warning: Wifi AP mode not supported!\n");
			sb.append("You should get unknown or zero values below.\n");
			sb.append("If you don't, isSupported() is probably buggy!\n");
		}
		final int state = apControl.getState();
		sb.append("State: ").append(stateString(state)).append('\n');
		sb.append("Enabled: ");
		if (apControl.isEnabled()) {
			sb.append("YES\n");
		} else {
			sb.append("NO\n");
		}
		final WifiConfiguration wifiConfig = apControl.getConfiguration();
		sb.append("WifiConfiguration:");
		if (wifiConfig == null) {
			sb.append(" null\n");
		} else {
			sb.append("\n");
			sb.append("   SSID: \"").append(wifiConfig.SSID).append("\"\n");
			sb.append("   preSharedKey: \"").append(wifiConfig.preSharedKey).append("\"\n");
		}
		final InetAddress addr = WifiApControl.getInetAddress();
		sb.append("InetAddress: ");
		if (addr == null) {
			sb.append("null");
		} else {
			sb.append(addr.toString());
		}
		tv.setText(sb.toString());
	}

	public void refresh(View view) {
		refresh();
	}

	public void enable() {
		wifiManager.setWifiEnabled(false);
		apControl.enable();
		refresh();
	}

	public void enable(View view) {
		enable();
	}

	public void disable() {
		apControl.disable();
		wifiManager.setWifiEnabled(true);
		refresh();
	}

	public void disable(View view) {
		enable();
	}
}
