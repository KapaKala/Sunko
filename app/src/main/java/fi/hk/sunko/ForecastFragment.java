package fi.hk.sunko;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * Fragment for displaying a 10 day forecast.
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
public class ForecastFragment extends Fragment {
    RecyclerView rv;
    RVAdapter adapter;
    ImageView bg;

    WeatherDisplayHandler weatherDisplayHandler;

    JSONArray forecast = new JSONArray();

    /**
     * NewInstance constructor for creating a fragment with arguments.
     *
     * @param page the fragment's page
     * @param title the fragment's title
     * @return the created fragment itself
     */
    public static ForecastFragment newInstance(int page, String title) {
        ForecastFragment forecastFragment = new ForecastFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        forecastFragment.setArguments(args);
        return forecastFragment;
    }

    /**
     * Helper method for setting the forecast data
     *
     * @param forecast the forecast data
     */
    public void setForecast(JSONArray forecast) {
        this.forecast = forecast;
    }

    /**
     * Override method for the onCreateView lifecycle method.
     *
     * Inflates the view, as well as creates a broadcast listener for weather info coming from
     * the location service.
     *
     * @param inflater inflater used to inflate the view
     * @param container not used
     * @param savedInstanceState not used
     * @return the view itself
     */
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
                final Intent finalIntent = intent;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String error = finalIntent.getStringExtra("error");

                            if (error == null) {
                                String jsonArray = finalIntent.getStringExtra("forecast");

                                try {
                                    forecast = new JSONArray(jsonArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                setUpRv(forecast);
                            }
                        }
                    });
                }


            }
        }, new IntentFilter("weatherInfo"));

        return view;
    }

    /**
     * Override method for the onResume lifecycle method. Checks if MainActivity has weather data,
     * sets up the recycler view with the data if present.
     */
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

    /**
     * Sets up the RecyclerView with the provided forecast data.
     *
     * @param setUpForecast forecast data
     */
    private void setUpRv(JSONArray setUpForecast) {
        adapter = new RVAdapter(setUpForecast);
        rv.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);
    }


    /**
     * Recycler view adapter that handles what is displayed in the recycler view.
     */
    class RVAdapter extends RecyclerView.Adapter<RVAdapter.ForecastHolder>{
        JSONArray forecast;

        /**
         * Constructor for the adapter
         *
         * @param forecast forecast data to be displayed
         */
        RVAdapter(JSONArray forecast) {
            this.forecast = forecast;
        }

        /**
         * Override method for creating RV's view holders.
         *
         * @param parent parent view group, in this case the main activity
         * @param viewType the type of view
         * @return the inflated holder view
         */
        @Override
        public ForecastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv, parent, false);

            return new ForecastHolder(v);
        }

        /**
         * Override method for binding the view holders. Sets up the displaying of the forecast
         * information.
         *
         * @param holder the holder to be bound
         * @param position a self-increasing integer for iterating through the holders
         */
        @Override
        public void onBindViewHolder(ForecastHolder holder, int position) {
            try {
                JSONObject dayforecast = (JSONObject) forecast.get(position);
                String highTemp = dayforecast.getJSONObject("high").getString(
                        ((MainActivity)getActivity()).prefs.getString(((MainActivity)getActivity()).tempFormatKey, "c").equals("c")
                                ? "celsius"
                                : "fahrenheit"
                ).equals("") ? "-" :
                        dayforecast.getJSONObject("high").getString(((MainActivity)getActivity()).prefs.getString(((MainActivity)getActivity()).tempFormatKey, "c").equals("c")
                                ? "celsius"
                                : "fahrenheit") + "°";
                String lowTemp = dayforecast.getJSONObject("low").getString("celsius").equals("") ? "-" :
                        dayforecast.getJSONObject("low").getString(((MainActivity)getActivity()).prefs.getString(((MainActivity)getActivity()).tempFormatKey, "c").equals("c")
                                ? "celsius"
                                : "fahrenheit") + "°";
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
         * @return amount of items in the view
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
             * @param itemView the view that is inflated into the holders
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
