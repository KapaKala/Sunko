package fi.hk.sunko;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static fi.hk.sunko.MainActivity.checkPermission;

/**
 * Fragment for displaying current weather.
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
public class WeatherFragment extends Fragment {
    BGHelper mListener;

    final int PERMISSION_REQUEST_ID = 0;

    String locationCity = "";
    String locationCountry = "";
    boolean usingLocation = true;

    boolean bound = false;
    ImageView weatherIcon;
    ImageView weatherBG1;
    ImageView weatherBG2;
    ImageView forecastBG;
    ImageButton refreshButton;
    ImageButton editLocationButton;
    TextView locationText;
    TextView temperatureText;
    TextView weatherText;
    TextView infoText;
    TextView tempCView;
    TextView tempFView;
    AVLoadingIndicatorView progressBar;
    Intent intent;
    ShapeDrawable mDrawable;

    LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    JSONArray hourlyForecast;

    AnimationUtil ani;
    RecyclerView rv;
    WeatherDisplayHandler weatherDisplayHandler;

    /**
     * NewInstance constructor for creating a fragment with arguments.
     *
     * @param page the fragment's page
     * @param title the fragment's title
     * @return the created fragment itself
     */
    public static WeatherFragment newInstance(int page, String title) {
        WeatherFragment weatherFragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        weatherFragment.setArguments(args);
        return weatherFragment;
    }

    /**
     * Override method for the onAttach lifecycle method.
     *
     * @param context application context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (BGHelper) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnArticleSelectedListener");
        }
    }

    /**
     * Override method for the onCreateView lifecycle method. Sets up the fragment.
     *
     * @param inflater inflater, used for inflating the view
     * @param container not used
     * @param savedInstanceState not used
     * @return the view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        hourlyForecast = new JSONArray();
        weatherDisplayHandler = new WeatherDisplayHandler();
        ani = new AnimationUtil();

        weatherIcon        = (ImageView)              view.findViewById(R.id.imageView);
        weatherBG1         = (ImageView)              view.findViewById(R.id.primaryBG);
        weatherBG2         = (ImageView)              view.findViewById(R.id.secondaryBG);
        forecastBG         = (ImageView)              view.findViewById(R.id.forecastBG);
        refreshButton      = (ImageButton)            view.findViewById(R.id.refreshButton);
        editLocationButton = (ImageButton)            view.findViewById(R.id.editLocationButton);
        locationText       = (TextView)               view.findViewById(R.id.locationTextView);
        temperatureText    = (TextView)               view.findViewById(R.id.temperatureView);
        weatherText        = (TextView)               view.findViewById(R.id.weatherTextView);
        infoText           = (TextView)               view.findViewById(R.id.infoText);
        tempCView          = (TextView)               view.findViewById(R.id.tempCView);
        tempFView          = (TextView)               view.findViewById(R.id.tempFView);
        progressBar        = (AVLoadingIndicatorView) view.findViewById(R.id.progressBar);
        rv                 = (RecyclerView)           view.findViewById(R.id.hrv);
        rv.setHasFixedSize(true);

//        infoText.setText("Trying to find you...");

        weatherIcon.setColorFilter(Color.WHITE);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh(v);
            }
        });

        editLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickCityDialog(v);
            }
        });

        tempCView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).prefs.edit().putString(
                        ((MainActivity)getActivity()).tempFormatKey, "c"
                ).apply();
                System.out.println("TEMP SETTING SET TO C");
                tempCView.setBackgroundColor(Color.WHITE);
                tempCView.setTextColor(Color.GRAY);
                tempFView.setBackgroundColor(Color.TRANSPARENT);
                tempFView.setTextColor(Color.WHITE);
                getWeather();
            }
        });

        tempFView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).prefs.edit().putString(
                        ((MainActivity)getActivity()).tempFormatKey, "f"
                ).apply();
                System.out.println("TEMP SETTING SET TO F");
                tempFView.setBackgroundColor(Color.WHITE);
                tempFView.setTextColor(Color.GRAY);
                tempCView.setBackgroundColor(Color.TRANSPARENT);
                tempCView.setTextColor(Color.WHITE);
                getWeather();

            }
        });

        if (((MainActivity)getActivity()).prefs.getString(((MainActivity)getActivity()).tempFormatKey, "c").equals("c")) {
            tempCView.setBackgroundColor(Color.WHITE);
            tempCView.setTextColor(Color.GRAY);
            tempFView.setBackgroundColor(Color.TRANSPARENT);
            tempFView.setTextColor(Color.WHITE);
        } else {
            tempFView.setBackgroundColor(Color.WHITE);
            tempFView.setTextColor(Color.GRAY);
            tempCView.setBackgroundColor(Color.TRANSPARENT);
            tempCView.setTextColor(Color.WHITE);
        }

        Typeface medium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Nunito-Bold.ttf");
        Typeface book = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Nunito-SemiBold.ttf");
        infoText.setTypeface(medium);
        temperatureText.setTypeface(medium);
        weatherText.setTypeface(book);
        locationText.setTypeface(book);

        showPermissionDialog();

        intent = new Intent(getActivity(), LocationService.class);

        return view;
    }

    /**
     * Displays a dialog for confirming permission to use location services.
     */
    private void showPermissionDialog() {
        if (!LocationService.checkPermission(getActivity())) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_ID
            );
        }
    }

    /**
     * Method for getting weather info.
     *
     * Starts the LocationService service, which manages fetching the user's location, then gets
     * the weather information from the Weather Underground API. A local broadcast manager
     * receives what is sent by the service and alters the UI accordingly.
     */
    public void getWeather() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshButton.setClickable(false);
                editLocationButton.setClickable(false);

                if (weatherIcon.getAlpha() != 0) {
                    ani.hideAnimation(weatherIcon);
                    ani.hideAnimation(temperatureText);
                    ani.showAnimation(progressBar);
                    ani.hideAnimation(weatherText);
                    ani.hideAnimation(infoText);
                    infoText.setText("Searching...");
                    ani.showAnimation(infoText);
                }
            }
        });

        if(!bound) {
            Intent intent = new Intent(getActivity(), LocationService.class);

            Log.d("PUTTING EXTRAS", usingLocation + ", " + locationCity + ", " + locationCountry);

            intent.putExtra("usingLocation", usingLocation);
            intent.putExtra("locationCity", locationCity);
            intent.putExtra("locationCountry", locationCountry);
            getActivity().startService(intent);
            Log.d("MainActivity", "Service Bound");
            bound = true;
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final Intent finalIntent = intent;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshButton.setClickable(true);
                            editLocationButton.setClickable(true);

                            String error = finalIntent.getStringExtra("error");

                            if (error != null) {
                                ani.showAnimation(infoText);
                                ani.hideAnimation(progressBar);
                                ani.showAnimation(weatherText);
                                infoText.setText(error);
                                weatherText.setText(":(");
                                locationText.setText("");
                            } else {
                                tempCView.setAlpha(1f);
                                tempFView.setAlpha(1f);
                                ani.showAnimation(weatherIcon);
                                ani.showAnimation(temperatureText);
                                ani.hideAnimation(progressBar);
                                ani.showAnimation(weatherText);
                                ani.showAnimation(infoText);

                                String location = finalIntent.getStringExtra("location");
                                String weatherType = finalIntent.getStringExtra("weatherType").equals("")
                                        ? "Clear"
                                        : finalIntent.getStringExtra("weatherType");
                                String temperature = finalIntent.getStringExtra("temperature").equals("")
                                        ? "0"
                                        : finalIntent.getStringExtra("temperature");
                                int roundedTemp = (int) Math.round(Double.parseDouble(temperature));
                                double currentHour = finalIntent.getDoubleExtra("currentHour", 0);
                                double sunrise = finalIntent.getDoubleExtra("sunrise", 6);
                                double sunset = finalIntent.getDoubleExtra("sunset", 21);

                                locationText.setText(location);

                                weatherIcon.setImageResource(weatherDisplayHandler.setImage(weatherType, currentHour, sunrise, sunset));
                                infoText.setText(weatherDisplayHandler.setInfoText(weatherType, roundedTemp, currentHour, sunrise, sunset));
                                weatherText.setText(weatherType);
                                temperatureText.setText(roundedTemp + "°");

                                String jsonArray = finalIntent.getStringExtra("hourly");
                                String jsonForecast = finalIntent.getStringExtra("forecast");

                                try {
                                    JSONArray array = new JSONArray(jsonArray);
                                    JSONArray forecastArray = new JSONArray((jsonForecast));
                                    mListener.setForecast(forecastArray);

                                    hourlyForecast = array;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                RVAdapter adapter = new RVAdapter(hourlyForecast, sunrise, sunset);
                                rv.setAdapter(adapter);
                                LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                                rv.setLayoutManager(llm);

                                View v = getActivity().findViewById(R.id.weatherFragment);
                                int h = v.getHeight();
                                mDrawable = new ShapeDrawable(new RectShape());
                                mDrawable.getPaint().setShader(weatherDisplayHandler.setBackgroundGradient(h, weatherType, currentHour, sunrise, sunset));

                                ani.animateBackground(weatherBG1, weatherBG2, mDrawable);
                                mListener.setBG(mDrawable);
                            }
                        }
                    });
                }

                if (bound) {
                    Intent i = new Intent(getActivity(), LocationService.class);
                    getActivity().stopService(i);

                    Log.d("MainActivity", "Service unbound");
                    Intent stopIntent = new Intent(getActivity(), LocationService.class);
                    getActivity().stopService(stopIntent);

                    bound = false;
                }
            }
        }, new IntentFilter("weatherInfo"));
    }

    /**
     * Override method for the onResume lifecycle method. Checks if the app has location
     * permissions and whether or not GPS and network location services are enabled, and prompts
     * the user to fix if necessary.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (!bound) {
            if (checkPermission(getContext())) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getContext());
                    dialog.setTitle("Your location services seem to be disabled");
                    dialog.setMessage("Please enable them if you would like for the app to find weather information for your current location. \n\n" +
                            "Otherwise you can click cancel and manually search for a location.");
                    dialog.setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getContext().startActivity(myIntent);
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pickCityDialog(getView());
                        }
                    });
                    dialog.show();
                } else {
                    getWeather();
                }
            }
        }
    }


    /**
     * Handles permissions upon receiving them.
     *
     * @param requestCode the request code used in getting specific permissions
     * @param permissions an array of permissions
     * @param grantResults an array of the results of a permission query
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    Intent intent = new Intent(getActivity(), LocationService.class);
                    getActivity().startService(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(getActivity(), "Location Denied :(", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method used by the refresh ImageButton for refreshing the current weather information.
     *
     * @param v the view that calls this method
     */
    public void refresh(View v) {
        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh_rotate);
        v.startAnimation(rotation);
        getWeather();
    }

    /**
     * Dialog for selecting a city/area or your current location.
     *
     * @param v the view that calls this method
     */
    public void pickCityDialog(View v) {
        LayoutInflater linf = LayoutInflater.from(getActivity());
        final View inflater = linf.inflate(R.layout.pick_city_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setMessage("Get the weather from the location of your choosing or use your current location");
        alert.setView(inflater);

        final EditText cityEt = (EditText) inflater.findViewById(R.id.pcDialogCity);
        final EditText countryEt = (EditText) inflater.findViewById(R.id.pcDialogCountry);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String city = cityEt.getText().toString().replace(" ", "_");
                String country = countryEt.getText().toString().replace(" ", "_");

                if (!city.equals("") && !country.equals("")) {
                    usingLocation = false;
                    locationCity = city;
                    locationCountry = country;

                    Log.d("PickCityDialog", "City: " + city + ", Country: " + country);
                    getWeather();
                }
            }
        });

        alert.setNeutralButton("Use current location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                usingLocation = true;
                locationCity = "";
                locationCountry = "";

                if (!checkPermission(getContext())) {
                    showPermissionDialog();
                } else {
                    getWeather();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();

    }

    /**
     * Recycler view adapter that handles what is displayed in the recycler view.
     */
    class RVAdapter extends RecyclerView.Adapter<RVAdapter.ForecastHolder> {
        JSONArray hourlyForecast;
        double sunrise;
        double sunset;

        /**
         * Constructor for the adapter
         *
         * @param hourlyForecast forecast data to be displayed
         */
        RVAdapter(JSONArray hourlyForecast, double sunrise, double sunset) {
            this.hourlyForecast = hourlyForecast;
            this.sunrise = sunrise;
            this.sunset = sunset;
        }

        /**
         * Override method for creating RV's view holders.
         *
         * @param parent   parent view group, in this case the main activity
         * @param viewType the type of view
         * @return the inflated holder view
         */
        @Override
        public ForecastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hrv, parent, false);
            v.setAlpha(0.75f);

            return new ForecastHolder(v);
        }

        /**
         * Override method for binding the view holders. Sets up the displaying of the hourly
         * forecast.
         *
         * @param holder   the holder to be bound
         * @param position a self-increasing integer for iterating through the holders
         */
        @Override
        public void onBindViewHolder(ForecastHolder holder, int position) {
            try {
                JSONObject oneHourForecast = (JSONObject) hourlyForecast.get(position);
                String highTemp = oneHourForecast.getJSONObject("temp").getString(
                        ((MainActivity)getActivity()).prefs.getString(((MainActivity)getActivity()).tempFormatKey, "c").equals("c")
                                ? "metric"
                                : "english"
                ).equals("") ? "-" :
                        oneHourForecast.getJSONObject("temp").getString(((MainActivity)getActivity()).prefs.getString(((MainActivity)getActivity()).tempFormatKey, "c").equals("c")
                                ? "metric"
                                : "english") + "°";
                double hour = Double.parseDouble(oneHourForecast.getJSONObject("FCTTIME").getString("hour"));
                holder.tv.setText(highTemp);
                holder.iv.setImageResource(weatherDisplayHandler.setImage(oneHourForecast.getString("condition"), hour, sunrise, sunset));
                if (position == 0) {
                    holder.dv.setText("Now");
                } else {
                    holder.dv.setText(oneHourForecast.getJSONObject("FCTTIME").getString("hour"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Typeface book = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Nunito-SemiBold.ttf");
            holder.tv.setTypeface(book);
            holder.dv.setTypeface(book);
        }

        /**
         * Getter for amount of items in the recycler view.
         *
         * @return amount of items in the view
         */
        @Override
        public int getItemCount() {
            return hourlyForecast.length();
        }

        /**
         * Adapter's inner view holder class.
         */
        class ForecastHolder extends RecyclerView.ViewHolder {
            TextView tv;
            TextView dv;
            ImageView iv;

            /**
             * Constructor for the view holder.
             *
             * @param itemView the view that is inflated into the holders
             */
            ForecastHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.hrvtv);
                dv = (TextView) itemView.findViewById(R.id.hrvdv);
                iv = (ImageView) itemView.findViewById(R.id.hrviv);

            }
        }
    }

    /**
     * An interface for sending background shader info to be used in other fragments.
     */
    interface BGHelper {
        public void setBG(ShapeDrawable drawable);
        public ShapeDrawable getBG();
        public void setForecast(JSONArray forecast);
        public JSONArray getForecast();
    }
}
