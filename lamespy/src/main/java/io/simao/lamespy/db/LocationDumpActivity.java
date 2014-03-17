package io.simao.lamespy.db;

import android.app.*;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import io.simao.lamespy.LocationDumpArrayAdapter;
import io.simao.lamespy.LocationExporter;
import io.simao.lamespy.R;

import java.util.Collections;
import java.util.List;

public class LocationDumpActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_dump);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<LocationEvent> events = new LocationExporter(databaseHelper).consolidatedDump();

        ArrayAdapter<LocationEvent> adapter = new LocationDumpArrayAdapter(this, android.R.layout.simple_list_item_1,
               events);

        setListAdapter(adapter);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO: Show dialog with event detail
        LocationEvent event = (LocationEvent) getListAdapter().getItem(position);
        Location location = event.getLocation().orSome(Location.UNKNOWN);

        StringBuilder sb = new StringBuilder();

        sb.append("<strong>");
        sb.append(location.getName()).append("</strong></br>");
        sb.append(location.getWifiNetworks().size());
        sb.append(" networks available: <br/>");

        for (Location.Network n : location.getWifiNetworks()) {
            sb.append("- ").append(n.getName())
              .append("<br/>");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Spanned html = Html.fromHtml(sb.toString());

        builder.setMessage(html)
               .setTitle(event.getTimeStamp())
               .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location_dump, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
