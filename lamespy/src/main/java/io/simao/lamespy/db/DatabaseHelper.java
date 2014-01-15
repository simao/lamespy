package io.simao.lamespy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import fj.data.Option;
import io.simao.lamespy.MainActivity;

import java.util.LinkedList;
import java.util.List;

import static fj.data.Option.some;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lamespy.db";

    private static final int DATABASE_VERSION = 5;

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

        Cursor c = getReadableDatabase().rawQuery(sql, null);

        List<Location> results = new LinkedList<Location>();

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                String networks = c.getString(c.getColumnIndexOrThrow("networks"));

                Location l = new Location(some(id), name, networks);
                results.add(l);
            } while (c.moveToNext());
        }

        return results;
    }

    public Option<Location> addLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Location.NAME, location.getName());
        values.put(Location.NETWORKS, location.getWifiNetworksStr());

        db.insert(Location.TABLE_NAME, null, values);
        db.close();

        return some(location);
    }
}

