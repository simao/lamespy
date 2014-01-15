package io.simao.lamespy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import fj.data.Option;
import io.simao.lamespy.db.DatabaseHelper;
import io.simao.lamespy.db.Location;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity implements MainFragment.MainFragmentEventsListener {
    private static final String TAG = MainActivity.class.getName();

    protected WifiManager mWifiManager;
    protected MainFragment mainFragment;
    protected SavedLocationsStore savedLocationsStore;
    protected List<ScanResult> lastResult = new LinkedList<ScanResult>();
    protected LocationMatcher locationMatcher = new LocationMatcher();

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

        savedLocationsStore = new SavedLocationsStore(new DatabaseHelper(this));

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiScanReceiver.NEW_SCAN_INTENT));
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void scanButtonClicked() {
        mWifiManager.startScan();

        Log.d(TAG, "Saved locations => " + savedLocationsStore.getSavedLocations());
    }

    @Override
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
                savedLocationsStore.saveLocation(name, lastResult);

                Toast.makeText(toastContext,
                        "Location " + name + " added",
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
