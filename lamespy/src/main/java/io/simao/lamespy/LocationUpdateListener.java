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

// This logic is kind of duplicated in the activity
// But the activity only needs to know that the location changed or that the locations is known,
// So maybe it just needs that new intent containing the new location and doesnt need
// the wifi results, so it doesnt need to run all this code.

// But the main activity also wants to know how to save stuff and if it's possible to
// save stuff so it also needs the wifi scan results

// Mas os resultados podem vir quando a activity nao tem  o listener de wifi e
// depois quando abrimos o programa queremos ver e guardar resultados com o ultimo scan
public class LocationUpdateListener extends BroadcastReceiver {
    private static final String TAG = LocationUpdateListener.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiScanReceiver.NEW_SCAN_INTENT)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            DatabaseHelper db = new DatabaseHelper(context);
            SavedLocationsStore store = new SavedLocationsStore(db);
            LocationMatcher matcher = new LocationMatcher();
            Option<Location> location = matcher.findCurrentFromSavedLocations(store, wifiManager.getScanResults());

            if (location.isSome()) {
                logEvent(db, location.some());
            } else {
                Log.i(TAG, "Could not determine current location");
            }
        }
    }

    private void logEvent(DatabaseHelper db, Location location) {
        db.addLocationEvent(location);
    }
}
