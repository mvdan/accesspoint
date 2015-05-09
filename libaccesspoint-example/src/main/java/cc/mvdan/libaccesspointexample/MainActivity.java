package cc.mvdan.libaccesspointexample;

import android.app.Activity;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.widget.TextView;
import android.content.Context;

import cc.mvdan.libaccesspoint.WifiApControl;

public class MainActivity extends Activity {

	private WifiApControl apControl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		apControl = WifiApControl.getApControl(wm);
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshStatus();
	}

	private static String describeWifiApState(int state) {
		switch (state) {
			case WifiApControl.STATE_FAILED:
				return "Failed state: could not enable or disable";
			case WifiApControl.STATE_DISABLED:
				return "Disabled state: AP is currently off";
			case WifiApControl.STATE_DISABLING:
				return "Disabling state: AP is currently being turned off";
			case WifiApControl.STATE_ENABLED:
				return "Enabled state: AP is currently on";
			case WifiApControl.STATE_ENABLING:
				return "Enabling state: AP is currently being turned on";
			default:
				return "Unknown state!";
		}
	}

	private void refreshStatus() {
		TextView tv = (TextView) findViewById(R.id.text1);
		if (!WifiApControl.isSupported()) {
			tv.setText("AP mode not supported!");
		} else {
			int state = apControl.getWifiApState();
			final String desc = describeWifiApState(state);
			tv.setText(desc);
		}
	}
}
