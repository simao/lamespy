package io.simao.lamespy.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import fj.F;
import fj.data.Option;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static fj.data.List.iterableList;
import static fj.data.Option.none;
import static fj.data.Option.some;

public class Location implements Comparable<Location>, Parcelable {
    private static final String TAG = Location.class.getName();

    public static final String TABLE_NAME = "locations";
    public static final String NAME = "name";
    public static final String NETWORKS = "networks";
    public static final String ID = "id";


    private Option<Long> id = none();
    private String name;
    private Option<String> wifiNetworksStr = none();

    public static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + " ("
            + "id integer primary key autoincrement, "
            + "name text not null, "
            + "networks text not null"
            + ");";

    public static String UNKNOWN_NAME = "Unknown";

    public Location(Option<Long> id, String name, String wifiNetworksStr) {
        this.id = id;
        this.name = name;
        this.wifiNetworksStr = Option.fromNull(wifiNetworksStr);
    }

    public Location(String name, List<Network> networks) {
        this.name = name;
        JSONArray a = networkListToJson(networks);
        this.wifiNetworksStr = some(a.toString());
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

    public Location extendWithNetworks(List<Network> networks) {
        LinkedList<Network> newNetworks = new LinkedList<Network>();
        newNetworks.addAll(getWifiNetworks());

        Set<String> existentBssids = new TreeSet<String>(iterableList(newNetworks).map(new F<Network, String>() {
            @Override
            public String f(Network network) {
                return network.bssid;
            }
        }).toCollection());

        for (Network n : networks) {
            if (!existentBssids.contains(n.bssid)) {
                newNetworks.add(n);
                existentBssids.add(n.bssid);
            }
        }

        Location l = new Location(name, newNetworks);
        l.id = this.id;

        return l;
    }

    public boolean equals(Location l) {
        return l.getIdOrZero() == this.getIdOrZero();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "location: {name: " + name + ", locations: " +
                wifiNetworksStr.orSome("[]") + "}";
    }

    public Option<Long> getId() {
        return id;
    }

    @Override
    public int compareTo(Location another) {
        return this.getName().compareTo(another.getName());
    }

    public String getWifiNetworksStr() {
        return this.wifiNetworksStr.orSome("[]");
    }

    public long getIdOrZero() {
        return id.orSome(0l);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.getIdOrZero());
        dest.writeString(this.getName());
        dest.writeString(this.getWifiNetworksStr());
    }

    public static final Parcelable.Creator<Location> CREATOR
            = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
                Option<Long> id = Option.some(in.readLong());
                String name = in.readString();
                String wifiNetworksStr = in.readString();
            return new Location(id, name, wifiNetworksStr);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

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

        public String getName() {
            return name;
        }

        public JSONObject toJSON() {
            return new JSONObject(toMap());
        }
    }
}
