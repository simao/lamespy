package io.simao.lamespy;

import android.util.Log;
import fj.F;
import fj.F2;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static fj.data.List.iterableList;

public class LocationExporter {
    private static final String TAG = LocationExporter.class.getName();

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

    public void writeJsonToTmpFile(File file) throws JSONException, IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(jsonDump().toString(4));
        writer.close();
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

    public JSONArray jsonDump() {
        fj.data.List<LocationEvent> dump = iterableList(dump());

        return dump.foldLeft(new F2<JSONArray, LocationEvent, JSONArray>() {
            @Override
            public JSONArray f(JSONArray jsonArray, LocationEvent locationEvent) {
                if (locationEvent.getLocation().equals(databaseHelper.getUnknownLocation())) {
                    return jsonArray;
                } else {
                    JSONObject o = new JSONObject();

                    try {
                        o.put(Location.NAME, locationEvent.getLocation().getName());
                        o.put(LocationEvent.TIMESTAMP, locationEvent.getTimeStamp());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    jsonArray.put(o);
                    return jsonArray;
                }
            }
        }, new JSONArray());
    }

    public List<LocationEvent> dump() {
        return databaseHelper.getAllLocationEvents();
    }
}
