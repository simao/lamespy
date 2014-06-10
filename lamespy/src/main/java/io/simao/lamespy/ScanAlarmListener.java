package io.simao.lamespy;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ScanAlarmListener extends BroadcastReceiver {
    public static String ALARM_RECEIVED_INTENT = "io.simao.lamespy.alarm_received";
    public static String TAG = ScanAlarmListener.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ALARM_RECEIVED_INTENT)) {
              Log.d(TAG, "Received alarm to start new wifi scan");

              WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
              mWifiManager.startScan();
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            setupAlarm(context);
        }
        // TODO Maybe also ACTION_PACKAGE_REPLACED ?
        // Is there an action for "this app was started"
    }

    public void setupAlarm(Context c) {
        Intent intent = new Intent();
        intent.setAction(ALARM_RECEIVED_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 100,
                AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
    }
}
