package fi.hk.sunko;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ForecastFragment extends Fragment {
    ImageView bg;

    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static ForecastFragment newInstance(int page, String title) {
        ForecastFragment forecastFragment = new ForecastFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        forecastFragment.setArguments(args);
        return forecastFragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        bg = (ImageView) getActivity().findViewById(R.id.forecastBG);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
