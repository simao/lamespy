package io.simao.lamespy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import fj.F;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationDataListener extends BroadcastReceiver {

    public static final String LOCATION_UPDATE = "io.simao.lamespy.location_update";
    public static final String EXTRA_LAST_SCAN_RESULTS = "last_scan_results";
    public static final String LOCATION = "io.simao.lamespy.location";

    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            ArrayList<ScanResult> results = new ArrayList<ScanResult>(wifiManager.getScanResults());

            Intent resultsIntent = new Intent()
                    .putParcelableArrayListExtra(EXTRA_LAST_SCAN_RESULTS, results)
                    .setAction(LOCATION_UPDATE);

            Option<Location> l = locationFromResults(new DatabaseHelper(c), results);
            if (l.isSome()) {
                resultsIntent.putExtra(LOCATION, l.some());
            }

            c.sendBroadcast(resultsIntent);
        }
    }

    private Option<Location> locationFromResults(DatabaseHelper dbHelper, List<ScanResult> scanResults) {
        LocationMatcher locationMatcher = new LocationMatcher();
        SavedLocationsStore store = new SavedLocationsStore(dbHelper);
        return locationMatcher.findCurrentFromSavedLocations(store, scanResults);
    }
}

