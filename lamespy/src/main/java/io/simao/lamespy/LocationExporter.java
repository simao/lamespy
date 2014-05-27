package io.simao.lamespy;

import android.util.Log;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static fj.data.Option.none;
import static fj.data.Option.some;

public class LocationExporter {
    private static final String TAG = Location.class.getName();

    protected DatabaseHelper databaseHelper;

    public LocationExporter(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public boolean exportToConsole() {
        for (LocationEvent e : dump()) {
            Location l = e.getLocation();
            Log.i(TAG, "Location: " + l.getName() + " at " + e.getTimeStamp());
        }

        return true;
    }

    // TODO: This should no longer be needed since we just keep first and last checkin?
    // remove duplicates, keeping just first and last checkin

    // This should not be needed anymore since we consolidate on checkin
    public List<LocationEvent> consolidatedDump() {
        List<LocationEvent> results = new LinkedList<LocationEvent>();
        List<LocationEvent> dump = dump();

        Option<LocationEvent> last = none();
        for (int i = 0; i < dump.size(); i++) {
            Option<LocationEvent> next = none();
            if (i + 1 < dump.size()) {
                next = Option.fromNull(dump.get(i + 1));
            }

            LocationEvent current = dump.get(i);

            if ((last.isNone()) ||
                    (last.some().getLocationId() != current.getLocationId()) ||
                    (next.isNone()) ||
                    (next.some().getLocationId() != current.getLocationId())) {
                results.add(current);
                last = some(current);
            }
        }

        Collections.reverse(results);

        return results;
    }

    public List<LocationEvent> dump() {
        return databaseHelper.getAllLocationEvents();
    }
}
