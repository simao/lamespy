package io.simao.lamespy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Parcelable;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;

import java.util.List;

public class LocationUpdateListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(LocationDataListener.LOCATION_UPDATE)) {
            DatabaseHelper db = new DatabaseHelper(context);
            LocationEventLogger locationEventLogger = new LocationEventLogger(db);

            Location intentLocation = intent.getParcelableExtra(LocationDataListener.LOCATION);

            locationEventLogger.log(Option.fromNull(intentLocation));
        }
    }
}
