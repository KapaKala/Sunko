package fi.hk.sunko;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Service class for fetching the user's location.
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
public class LocationService extends Service implements LocationListener {
    LocationManager locationManager;
    Location location;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    boolean isLocationFound = false;
    boolean usingLocation;
//    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());



    /**
     * Override method for the onStartCommand lifecycle method, ran when service is started.
     *
     * @param intent The intent passed to the service
     * @param flags Possible flags passed to the service
     * @param startId Possible start ID passed to the service
     * @return Tells the application what to do if it needs to kill the service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(getApplicationContext(), "Location Service Started", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        usingLocation = intent.getBooleanExtra("usingLocation", true);

        if (usingLocation) {
            doGPS();
        } else {
            getWeatherInfo(intent.getStringExtra("locationCountry"), intent.getStringExtra("locationCity"));
        }

        return START_NOT_STICKY;
    }

    /**
     * Uses either the user's location or given city and country names to fetch weather info.
     *
     * Depending on the information received in the intent when starting the service, fetches either
     * locational weather info or from a specific city/area. This information is then sent back to
     * the main activity through a broadcast manager.
     *
     * @param param1 Either latitude or country name
     * @param param2 Either longitude or city name
     */
    public void getWeatherInfo(Object param1, Object param2) {
        final String url;
        if (usingLocation) {
            url = "http://api.wunderground.com/api/" + getApplication().getResources().getString(R.string.API_KEY_WU) + "/astronomy/conditions/forecast10day/hourly/q/" + param1 + "," + param2 + ".json";
        } else {
            Log.d("COUNTRY: " + param1, ", CITY: " + param2);
            url = "http://api.wunderground.com/api/" + getApplication().getResources().getString(R.string.API_KEY_WU) + "/astronomy/conditions/forecast10day/hourly/q/" + param1 + "/" + param2 + ".json";
        }
        Log.d("REQUEST URL", url);
        final WeatherGetter getter = new WeatherGetter();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject info = getter.execute(url).get();
                    JSONObject currentObservation = info.getJSONObject("current_observation");

//                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();

                    String location = currentObservation.getJSONObject("display_location").getString("full");
                    String weather = currentObservation.getString("weather").equals("") ? "Clear" : currentObservation.getString("weather");
                    String temperature = currentObservation.getString("temp_c").equals("") ? "?" : currentObservation.getString("temp_c");

                    JSONObject sunPhase = info.getJSONObject("moon_phase");
                    double currentHour = Double.parseDouble(sunPhase.getJSONObject("current_time").getInt("hour") + "."
                            + sunPhase.getJSONObject("current_time").getInt("minute"));
                    double sunrise = Double.parseDouble(sunPhase.getJSONObject("sunrise").getInt("hour") + "."
                            + sunPhase.getJSONObject("sunrise").getInt("minute"));
                    double sunset = Double.parseDouble(sunPhase.getJSONObject("sunset").getInt("hour") + "."
                            + sunPhase.getJSONObject("sunset").getInt("minute"));

                    JSONArray forecast = info.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");
                    JSONArray hourly = info.getJSONArray("hourly_forecast");

                    Intent intent = new Intent("weatherInfo");
                    intent.putExtra("location", location);
                    intent.putExtra("weatherType", weather);
                    intent.putExtra("temperature", temperature);
                    intent.putExtra("currentHour", currentHour);
                    intent.putExtra("sunrise", sunrise);
                    intent.putExtra("sunset", sunset);
                    intent.putExtra("forecast", forecast.toString());
                    intent.putExtra("hourly", hourly.toString());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Intent intent = new Intent("weatherInfo");
                    intent.putExtra("error", "No weather info was found, sorry!");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }
        }).start();

    }

    /**
     * Checks wether or not permission has been given to use location services.
     *
     * @param context Application context
     * @return Whether or not permission has been given
     */
    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Fetches the user's location.
     *
     * Uses both GPS and network providers for determining the user's location. Both get checked
     * whether they're enabled or not, and depending on that as well as the location accuracy/if
     * location was found, sets the current location as the best option.
     */
    public void doGPS() {
        Location net_loc = null, gps_loc = null;
        if (checkPermission(getApplicationContext())) {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //do nothing
            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    if (locationManager != null) {
                        net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                    if (isGPSEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        if (locationManager != null) {
                            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }

                    if (net_loc != null && gps_loc != null) {
                        if (net_loc.getAccuracy() < gps_loc.getAccuracy()) {
                            location = net_loc;
                        } else {
                            location = gps_loc;
                        }
                    } else {
                        if (gps_loc != null) {
                            location = gps_loc;
                        } else if (net_loc != null) {
                            location = net_loc;
                        }
                    }

                    if (location != null) {
                        getWeatherInfo(location.getLatitude(), location.getLongitude());

                        stopSelf();
                    }
                }
            }
        }
    }

    /**
     * Override method for onDestroy lifecycle method.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        locationManager.removeUpdates(this);
    }

    /**
     * Override method for when the service gets bound.
     *
     * @param intent Intent passed when binding
     * @return Null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Override method for location changes.
     *
     * @param location Fetches new location info if location changed.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location Changed to", location.toString());
        if (!isLocationFound) {
            isLocationFound = true;
            System.out.println("Location found");
        }
        this.location = location;
        doGPS();
    }

    /**
     * Override method for provider status changes.
     *
     * @param provider The name of the provider that changed
     * @param status New status of the provider
     * @param extras Any extras that might get passed
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Overrider method for when a provider gets enabled.
     *
     * @param provider The name of the provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            isGPSEnabled = true;
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            isNetworkEnabled = true;
        }
    }

    /**
     * Overrider method for when a provider gets disabled.
     *
     * @param provider The name of the provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            isGPSEnabled = false;
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            isNetworkEnabled = false;
        }
    }

}
