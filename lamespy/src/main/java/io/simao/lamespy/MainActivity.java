package io.simao.lamespy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationDumpActivity;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity implements MainFragment.MainFragmentEventsListener {
    private static final String TAG = MainActivity.class.getName();

    protected WifiManager mWifiManager;
    protected MainFragment mainFragment;
    protected SavedLocationsStore savedLocationsStore;
    protected List<ScanResult> lastResult = new LinkedList<ScanResult>();
    protected LocationMatcher locationMatcher = new LocationMatcher();
    protected DatabaseHelper mDatabaseHelper;

    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiScanReceiver.NEW_SCAN_INTENT)) {
                onNewScanResults(mWifiManager.getScanResults());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mainFragment = new MainFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mainFragment)
                    .commit();
        }

        mDatabaseHelper = new DatabaseHelper(this);
        savedLocationsStore = new SavedLocationsStore(mDatabaseHelper);

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiScanReceiver.NEW_SCAN_INTENT));

        setupScanAlarm();
    }

    private void setupScanAlarm() {
        new ScanAlarmListener().setupAlarm(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiScanReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiScanReceiver.NEW_SCAN_INTENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_dump_log_console:
                return new LocationExporter(mDatabaseHelper).exportToConsole();
            case R.id.action_dump_log_share:
                return shareJsonDump();
            case R.id.action_show_dump:
                Intent launchNewIntent = new Intent(this, LocationDumpActivity.class);
                startActivity(launchNewIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean shareJsonDump() {
        LocationExporter exporter = new LocationExporter(mDatabaseHelper);

        File outputDir = getCacheDir();
        File outputFile = null;
        try {
            outputFile = File.createTempFile("lamespy-", ".json", outputDir);
            exporter.writeJsonToTmpFile(outputFile);
        } catch (IOException e) {
            Log.e(TAG, "Could not export", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, "Could not export", e);
            e.printStackTrace();
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outputFile));
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        try {
//            // TODO What happens if its too big?j
//            sendIntent.putExtra(Intent.EXTRA_TEXT, exporter.jsonDump().toString(4));
//        } catch (JSONException e) {
//            sendIntent.putExtra(Intent.EXTRA_TEXT, "[]");
//            e.printStackTrace();
//        }
//        sendIntent.setType("text/plain");
//        startActivity(sendIntent);
        return true;
    }

    @Override
    public void scanButtonClicked() {
        mWifiManager.startScan();

        Log.d(TAG, "Saved locations => " + savedLocationsStore.getSavedLocations());
    }

    public void addLocationClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add location: Name");
        final Context toastContext = this;
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                saveLocation(toastContext, name);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void saveLocation(Context context, String name) {
        final String fName = name;
        final Context fContext = context;

        if (savedLocationsStore.locationExists(name)) {
            String msg = String.format("Location with name %s already exists.\nDo you wish to merge the existent location with the new one?", fName);

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Location already exists")
                    .setMessage(msg);


            builder.setPositiveButton("Merge", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    savedLocationsStore.addScanToLocation(fName, lastResult);

                    Toast.makeText(fContext,
                            "Location " + fName + " merged",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            savedLocationsStore.saveLocation(name, lastResult);

            Toast.makeText(context,
                    "Location " + name + " added",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void fragmentViewCreated() {
    }

    public void onNewScanResults(List<ScanResult> results) {
        this.lastResult = results;

        if (this.lastResult.size() > 0)
            mainFragment.getLocationAvailableText().setText("Yes");
        else
            mainFragment.getLocationAvailableText().setText("No");

        updateCurrentLocation(results);
    }

    public void updateCurrentLocation(List<ScanResult> currentScan) {
        List<Location> savedLocations = savedLocationsStore.getSavedLocationsList();
        Option<String> locationName = locationMatcher.findCurrentLocationName(savedLocations, currentScan);
        String locationStr = locationName.orSome("Unknown");

        mainFragment.getCurrentLocationText().setText(locationStr);
    }
}
