package io.simao.lamespy;

import android.util.Log;
import fj.F;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationEvent;

import java.util.LinkedList;
import java.util.List;

import static fj.data.List.iterableList;

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

    public List<LocationEvent> consolidatedDump() {
        fj.data.List<LocationEvent> dump = iterableList(dump());

        return new LinkedList<LocationEvent>(dump.filter(new F<LocationEvent, Boolean>() {
            @Override
            public Boolean f(LocationEvent locationEvent) {
                return !locationEvent.getLocation().equals(databaseHelper.getUnknownLocation());
            }
        }).reverse().toCollection());
    }

    public List<LocationEvent> dump() {
        return databaseHelper.getAllLocationEvents();
    }
}
