package fi.hk.sunko;

import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LocationService extends Service implements LocationListener {
    LocationManager locationManager;
    Location location;
    boolean isLocationFound = false;
    boolean usingLocation;

    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getBaseContext());
    IBinder localBinder = new LocalBinder();


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

    public void getWeatherInfo(Object param1, Object param2) {
        String url;
        if (usingLocation) {
            url = "http://api.wunderground.com/api/9c724765b2ea3c24/geolookup/astronomy/conditions/forecast/q/" + param1 + "," + param2 + ".json";
        } else {
            Log.d("COUNTRY: " + param1, ", CITY: " + param2);
            url = "http://api.wunderground.com/api/9c724765b2ea3c24/geolookup/astronomy/conditions/forecast/q/" + param1 + "/" + param2 + ".json";
        }
        Log.d("REQUEST URL", url);
        WeatherGetter getter = new WeatherGetter();
        try {
            JSONObject info = getter.execute(url).get();
            JSONObject currentObservation = info.getJSONObject("current_observation");

            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();

            String stringTime = currentObservation.getString("local_time_rfc822");
            int currentHour = Integer.parseInt(stringTime.substring(17, 19));

            String location = currentObservation.getJSONObject("display_location").getString("city") + ", " +
                    currentObservation.getJSONObject("display_location").getString("country");

            JSONObject sunPhase = info.getJSONObject("sun_phase");
            int sunrise = sunPhase.getJSONObject("sunrise").getInt("hour");
            int sunset = sunPhase.getJSONObject("sunset").getInt("hour");
            Log.d("SUN PHASE", "" + sunPhase.toString());

            JSONArray forecast = info.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");

            System.out.println(forecast);

            Intent intent = new Intent("weatherInfo");
            intent.putExtra("location", location);
            intent.putExtra("iconuri", currentObservation.getString("icon_url"));
            intent.putExtra("weatherType", currentObservation.getString("weather"));
            intent.putExtra("temperature", currentObservation.getString("temp_c"));
            intent.putExtra("currentHour", currentHour);
            intent.putExtra("sunrise", sunrise);
            intent.putExtra("sunset", sunset);
            intent.putExtra("forecast", forecast.toString());
            manager.sendBroadcast(intent);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Intent intent = new Intent("weatherInfo");
            intent.putExtra("error", "No weather info was found, sorry!");
            manager.sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void doGPS() {
        if (checkPermission(getApplicationContext())) {

            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

            if (isLocationFound) {
                Log.d("LOCATION", location.toString());

                getWeatherInfo(location.getLatitude(), location.getLongitude());

                stopSelf();
            }
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service Destroyed", Toast.LENGTH_LONG).show();

        super.onDestroy();

        locationManager.removeUpdates(this);
    }

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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
}
