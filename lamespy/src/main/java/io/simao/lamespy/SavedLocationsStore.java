package io.simao.lamespy;

import android.net.wifi.ScanResult;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SavedLocationsStore {

    private final DatabaseHelper db;

    public SavedLocationsStore(DatabaseHelper db) {
        this.db = db;
    }

    // TODO If location exists, update ssids on that location
    public void saveLocation(String name, List<ScanResult> currentScan) {
        List<Location.Network> networks = new LinkedList<Location.Network>();

        for(ScanResult r: currentScan) {
            networks.add(new Location.Network(r.SSID, r.BSSID));
        }

        Location l = new Location(name, networks);

        db.addLocation(l);
    }

    public List<Location> getSavedLocationsList() {
        return new LinkedList<Location>(getSavedLocations().values());
    }

    public Map<String, Location> getSavedLocations() {
        Map<String, Location> r = new TreeMap<String, Location>();

        for (Location l : db.getAllLocations()) {
            r.put(l.getName(), l);
        }

        return r;
    }
}
