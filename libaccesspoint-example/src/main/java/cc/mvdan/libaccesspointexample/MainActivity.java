package cc.mvdan.libaccesspointexample;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.List;

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

		new Thread() {
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
		}.start();
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
		final WifiConfiguration config = apControl.getConfiguration();
		sb.append("WifiConfiguration:");
		if (config == null) {
			sb.append(" null\n");
		} else {
			sb.append("\n");
			sb.append("   SSID: \"").append(config.SSID).append("\"\n");
			sb.append("   preSharedKey: \"").append(config.preSharedKey).append("\"\n");
		}
		final InetAddress addr = apControl.getInetAddress();
		sb.append("InetAddress: ");
		if (addr == null) {
			sb.append("null\n");
		} else {
			sb.append(addr.toString()).append('\n');
		}
		final List<WifiApControl.Client> clients = apControl.getClients();
		sb.append("Clients: ");
		if (clients == null) {
			sb.append("null\n");
		} else if (clients.size() == 0) {
			sb.append("none\n");
		} else {
			sb.append('\n');
			for (final WifiApControl.Client c : clients) {
				sb.append("   ").append(c.IPAddr);
				sb.append(" ").append(c.HWAddr).append('\n');
			}
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		tv.setText(sb.toString());
	}

	public void refresh(final View view) {
		refresh();
	}

	public void enable() {
		wifiManager.setWifiEnabled(false);
		apControl.enable();
		refresh();
	}

	public void enable(final View view) {
		final Button button = (Button) view;
		button.setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							button.setEnabled(true);
						}
					});
				} catch (InterruptedException e) {
				}
			}
		}.start();
		enable();
	}

	public void disable() {
		apControl.disable();
		wifiManager.setWifiEnabled(true);
		refresh();
	}

	public void disable(final View view) {
		final Button button = (Button) view;
		button.setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							button.setEnabled(true);
						}
					});
				} catch (InterruptedException e) {
				}
			}
		}.start();
		disable();
	}
}
