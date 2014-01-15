package io.simao.lamespy.db;

public class LocationEvent {
    private int id;
    private int location_id;
    private String timeStamp;

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

    public LocationEvent(int id, int location_id, String timeStamp) {
        this.id = id;
        this.location_id = location_id;
        this.timeStamp = timeStamp;
    }
}
