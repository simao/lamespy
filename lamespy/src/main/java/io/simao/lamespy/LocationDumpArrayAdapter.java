package io.simao.lamespy;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fj.data.Option;
import io.simao.lamespy.db.Location;
import io.simao.lamespy.db.LocationEvent;

import java.util.List;

public class LocationDumpArrayAdapter extends ArrayAdapter<LocationEvent> {

    private final List<LocationEvent> objects;
    private LayoutInflater mInflater;

    public LocationDumpArrayAdapter(Context context, int resource, List<LocationEvent> objects) {
        super(context, resource, objects);
        this.objects = objects;
        this.mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationEvent event = objects.get(position);

        TextView text = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        text.setText(eventToText(event));

        return text;
    }

    private Spanned eventToText(LocationEvent event) {
        Location l = event.getLocation().orSome(Location.UNKNOWN);

        String html = "<strong>" + l.getName() + "</strong> at " + event.getTimeStamp();

        return Html.fromHtml(html);
    }
}
