package io.simao.lamespy;

import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static fj.data.Array.array;

public class SavedLocationsStore {

    private final SharedPreferences sharedPrefs;

    public static final String preference_file_key = "io.simao.lamespy.PREFERENCE_FILE_KEY";

    public enum KEYS {
        SAVED_LOCATIONS
    }

    public SavedLocationsStore(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    // TODO If location exists, update ssids on that location
    public void saveLocation(String name, List<ScanResult> currentScan) {
        try {
            JSONObject location = new JSONObject();

            location.put("name", name);

            JSONArray scanResults = new JSONArray();

            for(ScanResult r: currentScan) {
                scanResults.put(r.SSID + r.BSSID);
            }

            location.put("wifi_networks", scanResults);

            String locationsRepr = sharedPrefs.getString(KEYS.SAVED_LOCATIONS.toString(), "[]");

            JSONArray knownLocations = new JSONArray(locationsRepr);

            knownLocations.put(location);

            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(KEYS.SAVED_LOCATIONS.toString(), knownLocations.toString());
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Location> getSavedLocationsList() {
        return new LinkedList<Location>(getSavedLocations().values());
    }

    public Map<String, Location> getSavedLocations() {
        Map<String, Location> results = new TreeMap<String, Location>();

        String locationsRepr = sharedPrefs.getString(KEYS.SAVED_LOCATIONS.toString(), "[]");
        try {
            JSONArray locations = new JSONArray(locationsRepr);

            for (int i = 0; i < locations.length(); i++) {
                JSONObject location = new JSONObject(locations.getString(i));

                String name = location.getString("name");
                List<String> wifiNetworks = new LinkedList<String>();

                JSONArray wifiNetworksRepr = location.getJSONArray("wifi_networks");

                for(int j = 0; j < wifiNetworksRepr.length(); j++) {
                    wifiNetworks.add(wifiNetworksRepr.getString(j));
                }

                results.put(name, new Location(name, wifiNetworks));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }
}
