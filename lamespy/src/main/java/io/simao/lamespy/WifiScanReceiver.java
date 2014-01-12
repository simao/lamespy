package io.simao.lamespy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import java.util.ArrayList;

public class WifiScanReceiver extends BroadcastReceiver {

    public static final String NEW_SCAN_INTENT = "io.simao.lamespy.new_scan";

    public enum IDS {
        NEW_SCAN_RECEIVED
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            Intent resultsIntent = new Intent();
            resultsIntent.setAction(NEW_SCAN_INTENT);
            c.sendBroadcast(resultsIntent);
        }
    }
}

