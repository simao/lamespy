package io.simao.lamespy.db;

import android.os.Parcelable;
import fj.data.Option;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static fj.data.Option.some;

public class LocationEvent {
    private int id;
    private long location_id;
    private String timeStamp;
    private Option<Location> location;

    public static final String TABLE_NAME = "location_events";
    public static final String LOCATION_ID = "location_id";

    public static final String TIMESTAMP = "timestamp";
    public static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + " ("
            + "id integer primary key autoincrement, "
            + LOCATION_ID + " int not null, "
            + TIMESTAMP + " timestamp text not null"
            + ");";

    public LocationEvent(int id, long location_id, String timeStamp) {
        this.id = id;
        this.location_id = location_id;
        this.timeStamp = timeStamp;
    }

    public int getId() {
        return id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setLocation(Location location) {
        this.location = some(location);
    }

    public Location getLocation() {
        if (this.location.isSome()) {
            return this.location.some();
        } else {
            throw new RuntimeException("getLocation called before setLocation");
        }
    }
}
