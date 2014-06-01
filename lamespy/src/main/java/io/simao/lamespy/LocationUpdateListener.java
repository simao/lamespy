package io.simao.lamespy;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.text.format.Time;
import android.util.Log;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationEvent;

import java.util.List;

// This logic is duplicated in the activity
// But the activity only needs to know that the location changed or that the locations is known,
// So maybe it just needs that new intent containing the new location and doesnt need
// the wifi results, so it doesnt need to run all this code.

// But the main activity also wants to know how to save stuff and if it's possible to
// save stuff so it also needs the wifi scan results
//
// But it can receive just an intent with the results and current location!
public class LocationUpdateListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiScanReceiver.NEW_SCAN_INTENT)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            DatabaseHelper db = new DatabaseHelper(context);

            LocationEventLogger locationEventLogger = new LocationEventLogger(db);

            locationEventLogger.log(wifiManager.getScanResults());
        }
    }
}
