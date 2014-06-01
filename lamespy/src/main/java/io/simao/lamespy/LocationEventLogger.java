package io.simao.lamespy;

import android.net.wifi.ScanResult;
import android.util.Log;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationEvent;

import java.util.List;

public class LocationEventLogger {
    private static final String TAG = LocationEventLogger.class.getName();

    private DatabaseHelper dbHelper;
    private LocationMatcher locationMatcher = new LocationMatcher();

    public LocationEventLogger(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void log(List<ScanResult> scanResults) {
        SavedLocationsStore store = new SavedLocationsStore(dbHelper);
        Option<Location> location = locationMatcher.findCurrentFromSavedLocations(store, scanResults);

        if (location.isSome()) {
            logEventToDatabase(dbHelper, location.some());
        } else {
            logEventToDatabase(dbHelper, dbHelper.getUnknownLocation());
            Log.d(TAG, "Could not determine current location");
        }
    }

    private void logEventToDatabase(DatabaseHelper db, Location location) {
        List<LocationEvent> lastEvents = db.getLastLocationEvents(2);

        if (lastEvents.size() <= 1) {
            db.addLocationEvent(location);
            db.addLocationEvent(location);
        } else {
            Location last = lastEvents.get(0).getLocation();
            Location previousToLast = lastEvents.get(1).getLocation();

            if (last.equals(previousToLast) && last.equals(location)) {
                db.updateLocationEventTimestamp(lastEvents.get(0));
            } else if (!last.equals(location)) {
                db.addLocationEvent(location);
                db.addLocationEvent(location);
            } else {
                db.addLocationEvent(location);
            }
        }
    }
}
