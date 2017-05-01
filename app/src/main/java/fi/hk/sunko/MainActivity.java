package fi.hk.sunko;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

public class MainActivity extends AppCompatActivity{
    final int PERMISSION_REQUEST_ID = 0;

    String locationCity = "";
    String locationCountry = "";
    boolean usingLocation = true;

    boolean bound = false;
    LocationService locationService;
    LocationServiceConnection locationServiceConnection;
    ImageView weatherIcon;
    ImageView weatherBG1;
    ImageView weatherBG2;
    ImageButton refreshButton;
    ImageButton editLocationButton;
    TextView locationText;
    TextView weatherText;
    TextView infoText;
    AVLoadingIndicatorView progressBar;
    Intent intent;

    JSONArray forecast;

    AnimationUtil ani;
    RecyclerView rv;
    WeatherDisplayHandler weatherDisplayHandler;

    public MainActivity() {

    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        forecast = new JSONArray();
        locationServiceConnection = new LocationServiceConnection();
        weatherDisplayHandler = new WeatherDisplayHandler();
        ani = new AnimationUtil();

        weatherIcon        = (ImageView)              findViewById(R.id.imageView);
        weatherBG1         = (ImageView)              findViewById(R.id.primaryBG);
        weatherBG2         = (ImageView)              findViewById(R.id.secondaryBG);
        refreshButton      = (ImageButton)            findViewById(R.id.refreshButton);
        editLocationButton = (ImageButton)            findViewById(R.id.editLocationButton);
        locationText       = (TextView)               findViewById(R.id.locationTextView);
        weatherText        = (TextView)               findViewById(R.id.weatherTextView);
        infoText           = (TextView)               findViewById(R.id.infoText);
        progressBar        = (AVLoadingIndicatorView) findViewById(R.id.progressBar);
        rv                 = (RecyclerView)           findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        infoText.setText("Trying to find you...");

        weatherIcon.setColorFilter(Color.WHITE);

        setStatusBarTranslucent(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mNotificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        Typeface medium = Typeface.createFromAsset(getAssets(), "fonts/GothamRnd-Medium.otf");
        Typeface book = Typeface.createFromAsset(getAssets(), "fonts/GothamRnd-Book.otf");
        infoText.setTypeface(medium);
        weatherText.setTypeface(book);
        locationText.setTypeface(book);

        showPermissionDialog();

        intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void showPermissionDialog() {
        if (!LocationService.checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_ID
            );
        }
    }

    public void getWeather() {
        refreshButton.setClickable(false);
        editLocationButton.setClickable(false);



            ani.hideAnimation(weatherIcon);
            ani.showAnimation(progressBar);
            ani.hideAnimation(weatherText);
            ani.hideAnimation(infoText);
            infoText.setText("Searching...");
            ani.showAnimation(infoText);
            ani.hideAnimation(rv);


        if(!bound) {
            Intent intent = new Intent(this, LocationService.class);

            Log.d("PUTTING EXTRAS", usingLocation + ", " + locationCity + ", " + locationCountry);

            intent.putExtra("usingLocation", usingLocation);
            intent.putExtra("locationCity", locationCity);
            intent.putExtra("locationCountry", locationCountry);
            startService(intent);
            Log.d("MainActivity", "Service Bound");
            bound = true;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshButton.setClickable(true);
                editLocationButton.setClickable(true);

                String error = intent.getStringExtra("error");
                if (error != null) {
                    weatherIcon.setImageResource(R.drawable.ic_mood_bad_black_24dp);
                    ani.showAnimation(infoText);
                    ani.hideAnimation(progressBar);
                    ani.showAnimation(weatherIcon);
                    ani.showAnimation(weatherText);
                    infoText.setText("No weather info found, try again!");
                    weatherText.setText("Oops!");
                    locationText.setText("");
                } else {

                        ani.showAnimation(weatherIcon);
                        ani.hideAnimation(progressBar);
                        ani.showAnimation(weatherText);
                        ani.showAnimation(infoText);
                        ani.showAnimation(rv);


                    String location = intent.getStringExtra("location");
                    String weatherType = intent.getStringExtra("weatherType").equals("")
                            ? "Clear"
                            : intent.getStringExtra("weatherType");

                    String temperature = intent.getStringExtra("temperature");
                    int roundedTemp = (int) Math.floor(Double.parseDouble(temperature));
                    double currentHour = intent.getDoubleExtra("currentHour", 0);
                    double sunrise = intent.getDoubleExtra("sunrise", 6);
                    double sunset = intent.getDoubleExtra("sunset", 21);

                    String jsonArray = intent.getStringExtra("forecast");

                    try {
                        JSONArray array = new JSONArray(jsonArray);
                        forecast = array;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RVAdapter adapter = new RVAdapter(forecast);
                    rv.setAdapter(adapter);
                    LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                    rv.setLayoutManager(llm);

                    locationText.setText(location);

                    weatherIcon.setImageResource(weatherDisplayHandler.setImage(weatherType, currentHour, sunrise, sunset));
                    infoText.setText(weatherDisplayHandler.setInfoText(weatherType, roundedTemp, currentHour, sunrise, sunset));
                    weatherText.setText(roundedTemp + "°" + "\n" +
                            weatherType);

                    View v = findViewById(R.id.mainActivityLayout);
                    int h = v.getHeight();
                    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
                    mDrawable.getPaint().setShader(weatherDisplayHandler.setBackgroundGradient(h, weatherType, currentHour, sunrise, sunset));
                    if (weatherBG1.getAlpha() == 0f) {
                        weatherBG1.setBackground(mDrawable);
                        changeBackground(weatherBG1, weatherBG2);
                    } else {
                        weatherBG2.setBackground(mDrawable);
                        changeBackground(weatherBG2, weatherBG1);
                    }
                }

                if (bound) {
                    Intent i = new Intent(MainActivity.this, LocationService.class);
                    stopService(i);

                    Log.d("MainActivity", "Service unbound");
                    Intent stopIntent = new Intent(MainActivity.this, LocationService.class);
                    stopService(stopIntent);

                    bound = false;
                }
            }
        }, new IntentFilter("weatherInfo"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWeather();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("PERMISSION GIVEN");
                try {
                    Intent intent = new Intent(this, LocationService.class);
                    startService(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "Location Denied :(", Toast.LENGTH_LONG).show();
        }
    }

    public void refresh(View v) {
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_rotate);
        v.startAnimation(rotation);
        getWeather();
    }

    private void changeBackground(View v1, View v2) {
        ani.showAnimation(v1);
        ani.hideAnimation(v2);
//        ObjectAnimator bgIn = ObjectAnimator.ofFloat(v1, "alpha", 0, 1);
//        bgIn.setDuration(1000);
//        ObjectAnimator bgOut = ObjectAnimator.ofFloat(v2, "alpha", 1, 0);
//        bgOut.setDuration(1000);
//
//        AnimatorSet set = new AnimatorSet();
//        set.play(bgOut).after(bgIn);
//        set.start();
    }


    private class LocationServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder localBinder) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) localBinder;
            locationService = binder.getService();
            locationService.doGPS();
            Log.d("MainActivity", "MainActivity has connected the service");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("MainActivity", "MainActivity has disconnected the service");
        }
    }

    public void pickCityDialog(View v) {
        LayoutInflater linf = LayoutInflater.from(this);
        final View inflater = linf.inflate(R.layout.pick_city_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

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

                getWeather();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();

    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ForecastHolder>{
        JSONArray forecast;

        RVAdapter(JSONArray forecast) {
            this.forecast = forecast;
        }

        @Override
        public ForecastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv, parent, false);
            return new ForecastHolder(v);
        }

        @Override
        public void onBindViewHolder(ForecastHolder holder, int position) {
            try {
                JSONObject dayforecast = (JSONObject) forecast.get(position);
                holder.tv.setText(dayforecast.getJSONObject("high").getString("celsius") + "° | " +
                                  dayforecast.getJSONObject("low").getString("celsius") + "°");
                holder.iv.setImageResource(weatherDisplayHandler.setImage(dayforecast.getString("conditions"), 12, 6, 21));
                holder.dv.setText(dayforecast.getJSONObject("date").getString("weekday_short"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            holder.tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.tv.setTextSize(16);
        }

        @Override
        public int getItemCount() {
            return forecast.length();
        }

        public class ForecastHolder extends RecyclerView.ViewHolder {
            TextView tv;
            TextView dv;
            ImageView iv;

            ForecastHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.rvtv);
                dv = (TextView) itemView.findViewById(R.id.rvdv);
                iv = (ImageView) itemView.findViewById(R.id.rviv);

                Typeface book = Typeface.createFromAsset(getAssets(), "fonts/GothamRnd-Book.otf");
                tv.setTypeface(book);
            }

        }

    }
}
