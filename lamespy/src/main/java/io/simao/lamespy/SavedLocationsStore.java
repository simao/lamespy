package io.simao.lamespy;

import android.net.wifi.ScanResult;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;

import java.util.*;

public class SavedLocationsStore {

    private final DatabaseHelper db;

    public SavedLocationsStore(DatabaseHelper db) {
        this.db = db;
    }

    public void saveLocation(String name, List<ScanResult> currentScan) {
        List<Location.Network> networks = networksFromScan(currentScan);
        Location l = new Location(name, networks);

        db.addLocation(l);
    }

    public void addScanToLocation(String locationName, List<ScanResult> scan) {
        Location l = getSavedLocations().get(locationName);

        if (l == null) {
            throw new RuntimeException("Cannot edit an unexistent location");
        } else {
            List<Location.Network> networks = networksFromScan(scan);
            db.updateLocation(l.extendWithNetworks(networks));
        }
    }

    public List<Location> getSavedLocationsList() {
        return new LinkedList<Location>(getSavedLocations().values());
    }

    public Map<String, Location> getSavedLocations() {
        Map<String, Location> r = new TreeMap<String, Location>(String.CASE_INSENSITIVE_ORDER);

        for (Location l : db.getAllLocations()) {
            r.put(l.getName(), l);
        }

        return r;
    }

    public Boolean locationExists(String name) {
        return getSavedLocations().containsKey(name);
    }

    private List<Location.Network> networksFromScan(List<ScanResult> scan) {
        List<Location.Network> networks = new LinkedList<Location.Network>();

        for(ScanResult r: scan) {
            networks.add(new Location.Network(r.SSID, r.BSSID));
        }

        return networks;
    }
}
