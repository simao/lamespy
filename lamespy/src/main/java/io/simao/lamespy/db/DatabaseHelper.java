package io.simao.lamespy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.format.Time;
import android.util.Log;
import android.util.Pair;
import fj.data.Option;
import io.simao.lamespy.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static fj.data.Option.some;

// TODO Lots of duplicated code around this class

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lamespy.db";

    private static final int DATABASE_VERSION = 6;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(LocationEvent.DATABASE_CREATE);
        database.execSQL(Location.DATABASE_CREATE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        // TODO Upgrade
        database.execSQL("DROP TABLE " + LocationEvent.TABLE_NAME);
        database.execSQL("DROP TABLE " + Location.TABLE_NAME);

        database.execSQL(LocationEvent.DATABASE_CREATE);
        database.execSQL(Location.DATABASE_CREATE);
    }

    // TODO This can be optimized to findByBssid or something
    public List<Location> getAllLocations() {
        String sql = "SELECT * from " + Location.TABLE_NAME;
        List<Location> results = new LinkedList<Location>();

        Cursor c = getReadableDatabase().rawQuery(sql, null);


        try {
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
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

    // TODO Can I somehow just return a cursor here?
    // I think I need to return the database cursor and handle the mapping to POJOs
    // as needed...
    public List<LocationEvent> getAllLocationEvents() {
        String sql = "SELECT l.id, l.name, l.networks, le.id as location_event_id, le.timestamp  from "
                + LocationEvent.TABLE_NAME + " AS le INNER JOIN " +
                Location.TABLE_NAME + " AS l ON l.id = le.location_id";
        List<LocationEvent> results = new ArrayList<LocationEvent>();

        Cursor c = getReadableDatabase().rawQuery(sql, null);

        try {
            if (c.moveToFirst()) {
                do {
                    int lid = c.getInt(c.getColumnIndexOrThrow("id"));
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                    String networks = c.getString(c.getColumnIndexOrThrow("networks"));

                    Location l = new Location(some(lid), name, networks);

                    int leId = c.getInt(c.getColumnIndexOrThrow("location_event_id"));

                    String timeStamp = c.getString(c.getColumnIndexOrThrow("timestamp"));

                    LocationEvent event = new LocationEvent(leId, lid, timeStamp);

                    event.setLocation(l);

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

        db.insert(Location.TABLE_NAME, null, values);
        db.close();
    }

    public void addLocationEvent(Location eventLocation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(LocationEvent.LOCATION_ID, eventLocation.getIdOrZero());
        values.put(LocationEvent.TIMESTAMP, getCurrentDateTime());

        db.insert(LocationEvent.TABLE_NAME, null, values);
        db.close();
    }

    private String getCurrentDateTime() {
        return DATE_FORMAT.format(new Date());
    }
}
