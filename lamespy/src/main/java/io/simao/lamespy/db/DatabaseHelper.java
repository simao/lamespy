package io.simao.lamespy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import fj.P1;
import fj.data.Option;

import java.text.SimpleDateFormat;
import java.util.*;

import static fj.data.Option.none;
import static fj.data.Option.some;

// TODO Lots of duplicated code around this class

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lamespy.db";

    private static final int DATABASE_VERSION = 8;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private Option<Location> UNKNOWN_LOCATION = none();

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(LocationEvent.DATABASE_CREATE);
        database.execSQL(Location.DATABASE_CREATE);

        database.execSQL("CREATE INDEX idx_location_event_time ON " +
                LocationEvent.TABLE_NAME + "(timestamp)");

        database.execSQL("CREATE INDEX idx_location_name ON " +
                Location.TABLE_NAME + "(name)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
//        // TODO Upgrade
//        database.execSQL("DROP TABLE " + LocationEvent.TABLE_NAME);
//        database.execSQL("DROP TABLE " + Location.TABLE_NAME);
//
//        database.execSQL(LocationEvent.DATABASE_CREATE);
//        database.execSQL(Location.DATABASE_CREATE);

//        database.execSQL("CREATE INDEX idx_location_name ON " +
//                Location.TABLE_NAME + "(name)");

//        database.execSQL("CREATE INDEX idx_location_name ON " +
//                Location.TABLE_NAME + "(name)");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    // TODO This can be optimized to findByBssid or something
    public List<Location> getAllLocations() {
        String sql = "SELECT * from " + Location.TABLE_NAME;
        List<Location> results = new LinkedList<Location>();

        Cursor c = getReadableDatabase().rawQuery(sql, null);

        try {
            if (c.moveToFirst()) {
                do {
                    long id = c.getLong(c.getColumnIndexOrThrow("id"));
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                    String networks = c.getString(c.getColumnIndexOrThrow("networks"));

                    Location l = new Location(some(id), name, networks);
                    results.add(l);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return results;
    }


    public List<LocationEvent> getAllLocationEvents() {
        String sql = "SELECT l.id, l.name, l.networks, le.id as location_event_id, le.timestamp  from "
                + LocationEvent.TABLE_NAME + " AS le INNER JOIN " +
                Location.TABLE_NAME + " AS l ON l.id = le.location_id";

        return getLocationEvents(sql);
    }

    private List<LocationEvent> getLocationEvents(String sql) {
        List<LocationEvent> results = new LinkedList<LocationEvent>();
        Cursor c = getReadableDatabase().rawQuery(sql, null);

        try {
            if (c.moveToFirst()) {
                do {
                    LocationEvent event = buildEvent(c);
                    results.add(event);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return results;
    }


    public void addLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Location.NAME, location.getName());
        values.put(Location.NETWORKS, location.getWifiNetworksStr());

        if (location.getId().isNone()) {
            values.put(Location.ID, 0);
        }

        db.insert(Location.TABLE_NAME, null, values);
        db.close();
    }

    public long addLocationEvent(Location eventLocation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(LocationEvent.LOCATION_ID, eventLocation.getIdOrZero());
        values.put(LocationEvent.TIMESTAMP, getCurrentDateTime());

        long id = db.insert(LocationEvent.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public Option<Location> getLocationByName(String name) {
        String sql = "SELECT l.id, l.name, l.networks FROM " + Location.TABLE_NAME +
                " l WHERE l.name = '" + name + "' ORDER BY id ASC LIMIT 1";

        Cursor c = getReadableDatabase().rawQuery(sql, null);

        try {
            if (c.moveToFirst()) {
                return some(buildLocation(c));
            } else {
                return none();
            }
        } finally {
            c.close();
        }
    }

    public void updateLocation(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(Location.NAME, location.getName());
        cv.put(Location.NETWORKS, location.getWifiNetworksStr());

        if (location.getId().isNone()) {
            throw new RuntimeException("Cannot update a location with id = None");
        }

        getWritableDatabase().update(Location.TABLE_NAME, cv,
                "id="+location.getIdOrZero(), null);
    }

    public void updateLocationEventTimestamp(LocationEvent locationEvent) {
        ContentValues cv = new ContentValues();
        cv.put("timestamp", getCurrentDateTime());

        getWritableDatabase().update(LocationEvent.TABLE_NAME, cv,
                "id=" + locationEvent.getId(), null);
    }

    public List<LocationEvent> getLastLocationEvents(int count) {
        String sql = "SELECT l.id, l.name, l.networks, le.id as location_event_id, le.timestamp  from "
                + LocationEvent.TABLE_NAME + " AS le INNER JOIN " +
                Location.TABLE_NAME + " AS l ON l.id = le.location_id " +
                "ORDER BY le.timestamp DESC LIMIT " + count
                ;

        return getLocationEvents(sql);
    }

    private Location buildLocation(Cursor record) {
        long lid = record.getLong(record.getColumnIndexOrThrow("id"));
        String name = record.getString(record.getColumnIndexOrThrow("name"));
        String networks = record.getString(record.getColumnIndexOrThrow("networks"));

        return new Location(some(lid), name, networks);
    }

    private LocationEvent buildEvent(Cursor record) {
        Location l = buildLocation(record);
        long lid = record.getLong(record.getColumnIndexOrThrow("id"));

        int leId = record.getInt(record.getColumnIndexOrThrow("location_event_id"));

        String timeStamp = record.getString(record.getColumnIndexOrThrow("timestamp"));

        LocationEvent event = new LocationEvent(leId, lid, timeStamp);

        event.setLocation(l);

        return event;
    }


    private String getCurrentDateTime() {
        return DATE_FORMAT.format(new Date());
    }

    public Location getUnknownLocation() {
        return UNKNOWN_LOCATION.orSome(new P1<Location>() {
            @Override
            public Location _1() {
                Option<Location> existentLocation = getLocationByName(Location.UNKNOWN_NAME);

                if (existentLocation.isSome()) {
                    UNKNOWN_LOCATION = existentLocation;
                    return existentLocation.some();
                } else {
                    long id = addLocationEvent(new Location(some(0l), Location.UNKNOWN_NAME, "[]"));
                    Location l = new Location(some(id), Location.UNKNOWN_NAME, "[]");
                    UNKNOWN_LOCATION = some(l);
                    return l;
                }
            }
        });
    }
}
