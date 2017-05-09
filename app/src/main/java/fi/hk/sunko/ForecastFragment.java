package fi.hk.sunko;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ForecastFragment extends Fragment {
    RecyclerView rv;
    RVAdapter adapter;
    ImageView bg;

    WeatherDisplayHandler weatherDisplayHandler;

    private String title;
    private int page;
    JSONArray forecast = new JSONArray();

    // newInstance constructor for creating fragment with arguments
    public static ForecastFragment newInstance(int page, String title) {
        ForecastFragment forecastFragment = new ForecastFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        forecastFragment.setArguments(args);
        return forecastFragment;
    }

    public void setForecast(JSONArray forecast) {
        this.forecast = forecast;
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
        final View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        bg = (ImageView) getActivity().findViewById(R.id.forecastBG);

        weatherDisplayHandler = new WeatherDisplayHandler();
        rv = (RecyclerView) view.findViewById(R.id.rv);
//                rv.setHasFixedSize(true);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("WEATHER RECEIVED IN FORECASTFRAGMENT");
                final Intent finalIntent = intent;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String jsonArray = finalIntent.getStringExtra("forecast");

                        try {
                            JSONArray array = new JSONArray(jsonArray);
                            forecast = array;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        setUpRv(forecast);
                    }
                });

            }
        }, new IntentFilter("weatherInfo"));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final JSONArray mainForecast = ((MainActivity)getActivity()).getForecast();
        System.out.println("FORECAST RECEIVED AS " + mainForecast);

        if (mainForecast != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUpRv(mainForecast);
                }
            });
        }

    }

    private void setUpRv(JSONArray setUpForecast) {
        adapter = new RVAdapter(setUpForecast);
        rv.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);
    }


    /**
     * Recycler view inner class that handles it's app logic.
     */
    class RVAdapter extends RecyclerView.Adapter<RVAdapter.ForecastHolder>{
        JSONArray forecast;

        /**
         * Constructor for the adapter
         *
         * @param forecast Forecast data to be displayed
         */
        RVAdapter(JSONArray forecast) {
            this.forecast = forecast;
        }

        /**
         * Override method for creating RV's view holders.
         *
         * @param parent Parent view group, in this case the main activity
         * @param viewType The type of view
         * @return The inflated holder view
         */
        @Override
        public ForecastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv, parent, false);

            return new ForecastHolder(v);
        }

        /**
         * Override method for binding the view holders.
         *
         * @param holder The holder to be bound
         * @param position A self-increasing integer for iterating through the holders
         */
        @Override
        public void onBindViewHolder(ForecastHolder holder, int position) {
            try {
                JSONObject dayforecast = (JSONObject) forecast.get(position);
                String highTemp = dayforecast.getJSONObject("high").getString("celsius").equals("") ? "-" :
                        dayforecast.getJSONObject("high").getString("celsius") + "°";
                String lowTemp = dayforecast.getJSONObject("low").getString("celsius").equals("") ? "-" :
                        dayforecast.getJSONObject("low").getString("celsius") + "°";
                holder.thv.setText(highTemp);
                holder.tlv.setText(lowTemp);
                holder.iv.setImageResource(weatherDisplayHandler.setImage(dayforecast.getString("conditions"), 12, 6, 21));
                holder.dv.setText(dayforecast.getJSONObject("date").getString("weekday"));
                holder.dnv.setText(dayforecast.getJSONObject("date").getString("day"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Typeface book = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Nunito-SemiBold.ttf");
            holder.thv.setTypeface(book);
            holder.tlv.setTypeface(book);
            holder.dv.setTypeface(book);
            holder.dnv.setTypeface(book);
        }

        /**
         * Getter for amount of items in the recycler view.
         *
         * @return Amount of items in the view
         */
        @Override
        public int getItemCount() {
            return forecast.length();
        }

        /**
         * Adapter's inner view holder class.
         */
        class ForecastHolder extends RecyclerView.ViewHolder {
            TextView thv;
            TextView tlv;
            TextView dv;
            TextView dnv;
            ImageView iv;

            /**
             * Constructor for the view holder.
             *
             * @param itemView The view that is inflated into the holders
             */
            ForecastHolder(View itemView) {
                super(itemView);
                thv = (TextView) itemView.findViewById(R.id.rvthv);
                tlv = (TextView) itemView.findViewById(R.id.rvtlv);
                dv = (TextView) itemView.findViewById(R.id.rvdv);
                dnv = (TextView) itemView.findViewById(R.id.rvdnv);
                iv = (ImageView) itemView.findViewById(R.id.rviv);

            }
        }
    }
}
