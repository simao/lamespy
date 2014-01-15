package io.simao.lamespy.db;

import android.util.Log;
import fj.data.Option;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Location implements Comparable<Location> {
    private static final String TAG = Location.class.getName();

    public static final String TABLE_NAME = "locations";
    public static final String NAME = "name";
    public static final String NETWORKS = "networks";

    private Option<Integer> id = Option.none();
    private String name;
    private Option<String> wifiNetworksStr = Option.none();

    public static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + " ("
            + "id integer primary key autoincrement, "
            + "name text not null, "
            + "networks text not null"
            + ");";

    public Location(Option<Integer> id, String name, String wifiNetworksStr) {
        this.id = id;
        this.name = name;
        this.wifiNetworksStr = Option.fromNull(wifiNetworksStr);
    }

    public Location(String name, List<Network> networks) {
        this.name = name;
        JSONArray a = networkListToJson(networks);
        this.wifiNetworksStr = Option.some(a.toString());
    }

    private JSONArray networkListToJson(List<Network> networks) {
        JSONArray res = new JSONArray();

        for (Network net : networks) {
            res.put(net.toJSON());
        }

        return res;
    }

    public boolean includesBssid(String bssid) {
        for (Network net : getWifiNetworks()) {
            if (net.bssid.contains(bssid)) {
                return true;
            }
        }

        return false;
    }

    public List<Network> getWifiNetworks() {
        List<Network> results = new LinkedList<Network>();

        try {
            String repr = this.wifiNetworksStr.orSome("[]");
            JSONArray networks = new JSONArray(repr);

            for (int i = 0; i < networks.length(); i++) {
                results.add(new Network(networks.getString(i)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing json", e);
        }

        return results;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "location: {name: " + name + ", locations: " +
                wifiNetworksStr.orSome("[]") + "}";
    }

    @Override
    public int compareTo(Location another) {
        return this.getName().compareTo(another.getName());
    }

    public String getWifiNetworksStr() {
        return this.wifiNetworksStr.orSome("[]");
    }

    public int getIdOrZero() {
        return id.orSome(0);
    }

    public static class Network {
        private String name;
        private String bssid;

        public Network(String name, String bssid) {
            this.name = name;
            this.bssid = bssid;
        }

        public Network(String jsonRepr) throws JSONException {
            JSONObject location = new JSONObject(jsonRepr);
            this.name = location.getString("name");
            this.bssid = location.getString("bssid");
        }

        public Map<String, String> toMap() {
            Map<String, String> r = new TreeMap<String, String>();
            r.put("name", name);
            r.put("bssid", bssid);
            return r;
        }

        public JSONObject toJSON() {
            return new JSONObject(toMap());
        }
    }
}