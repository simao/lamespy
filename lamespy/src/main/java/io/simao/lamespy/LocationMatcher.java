package io.simao.lamespy;

import android.net.wifi.ScanResult;
import fj.F;
import fj.data.Option;
import io.simao.lamespy.db.Location;

import java.util.List;

public class LocationMatcher {

    public Option<Location> findCurrentFromSavedLocations(SavedLocationsStore locationStore, List<ScanResult> lastScan) {
        return findCurrentLocation(locationStore.getSavedLocationsList(), lastScan);
    }

    public Option<Location> findCurrentLocation(List<Location> savedLocations, List<ScanResult> lastScan) {

        for (ScanResult scan : lastScan) {
            for (Location savedLocation : savedLocations) {
                if (savedLocation.includesBssid(scan.BSSID)) {
                    return Option.some(savedLocation);
                }
            }
        }

        return Option.none();
    }

    public Option<String> findCurrentLocationName(List<Location> savedLocations, List<ScanResult> lastScan) {
        return findCurrentLocation(savedLocations, lastScan).map(new F<Location, String>() {
            @Override
            public String f(Location l) {
                return l.getName();
            }
        });
    }
}
