package io.simao.lamespy;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainFragment extends Fragment {
    protected View rootView;
    protected MainFragmentEventsListener listener;

    public MainFragment() {
    }

    public interface MainFragmentEventsListener {
        public void scanButtonClicked();
        public void addLocationClicked();
        public void fragmentViewCreated();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        getAddLocationsButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addLocationClicked();
            }
        });

        getUpdateButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.scanButtonClicked();
            }
        });

        listener.fragmentViewCreated();

        return rootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (MainFragmentEventsListener) activity;
    }

    public TextView getCurrentLocationText() {
        return (TextView)rootView.findViewById(R.id.current_location);
    }

    public Button getAddLocationsButton() {
        return (Button)rootView.findViewById(R.id.b_add_location);
    }

    public Button getUpdateButton() {
        return (Button)rootView.findViewById(R.id.b_update_status);
    }

    public TextView getLocationAvailableText() {
        return (TextView)rootView.findViewById(R.id.location_available);
    }
}