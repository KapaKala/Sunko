package fi.hk.sunko;

import android.Manifest;
import android.animation.ObjectAnimator;
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
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    final int PERMISSION_REQUEST_ID = 0;

    String locationCity = "";
    String locationCountry = "";
    boolean usingLocation = true;

    boolean bound = false;
    LocationService locationService;
    LocationServiceConnection locationServiceConnection;
    ImageView weatherIcon;
    ImageView weatherBG;
    ImageButton refreshButton;
    TextView locationText;
    TextView weatherText;
    TextView infoText;
    ProgressBar progressBar;
    Intent intent;

    WeatherDisplayHandler weatherDisplayHandler;

//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;
//    private AudioManager mAudioManager;

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

        locationServiceConnection = new LocationServiceConnection();

        weatherDisplayHandler = new WeatherDisplayHandler();

        weatherIcon   = (ImageView)   findViewById(R.id.imageView);
        weatherBG     = (ImageView)   findViewById(R.id.weatherBG);
        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        locationText  = (TextView)    findViewById(R.id.locationTextView);
        weatherText   = (TextView)    findViewById(R.id.weatherTextView);
        infoText      = (TextView)    findViewById(R.id.infoText);
        progressBar   = (ProgressBar) findViewById(R.id.progressBar);

        weatherText.setText("Trying to find you...");

        weatherIcon.setColorFilter(Color.WHITE);

        setStatusBarTranslucent(true);

//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mNotificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

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

        if (weatherIcon.getAlpha() == 1) {
            weatherIcon.setAlpha(0f);
            progressBar.setAlpha(1f);
        }

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
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshButton.setClickable(true);

                if (weatherIcon.getAlpha() == 0) {
                    weatherIcon.setAlpha(1f);
                    progressBar.setAlpha(0);
                }

                String error = intent.getStringExtra("error");
                if (error != null) {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                } else {
                    String location = intent.getStringExtra("location");
                    String weatherType = intent.getStringExtra("weatherType");
                    String temperature = intent.getStringExtra("temperature");
                    int currentHour = intent.getIntExtra("currentHour", 0);
                    int sunrise = intent.getIntExtra("sunrise", 6);
                    int sunset = intent.getIntExtra("sunset", 21);

                    String jsonArray = intent.getStringExtra("forecast");

                    try {
                        JSONArray array = new JSONArray(jsonArray);
                        System.out.println(array.getString(1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    locationText.setText(location);

                    weatherIcon.setImageResource(weatherDisplayHandler.setImage(weatherType, currentHour, sunrise, sunset));
                    setWeatherText(weatherType, temperature);

                    View v = findViewById(R.id.mainActivityLayout);
                    int h = v.getHeight();
                    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
                    mDrawable.getPaint().setShader(weatherDisplayHandler.setBackgroundGradient(h, weatherType, currentHour, sunrise, sunset));
                    weatherBG.setBackground(mDrawable);
                    weatherBG.setAlpha(1f);
                    ObjectAnimator bgFade = ObjectAnimator.ofFloat(weatherBG, "alpha", 0, 1);
                    bgFade.setDuration(500);
                    bgFade.setInterpolator(new AccelerateInterpolator());
                    bgFade.start();

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

    @Override
    public void onSensorChanged(SensorEvent event) {
//        float values[] = event.values;
//
//        if (values[2] < -9.75f && values[2] > -9.85f) {
//            Log.d("SENSOR", String.valueOf(values[2]));
//            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_ALLOW_RINGER_MODES);
//        } else {
//            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_ALLOW_RINGER_MODES);
//
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void refresh(View v) {
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_rotate);
        v.startAnimation(rotation);
        getWeather();
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

    private void setWeatherText(String type, String temperature) {
        int roundedTemp = (int) Math.floor(Double.parseDouble(temperature));
        weatherText.setText("The weather is currently " + type.toLowerCase() + "\n" +
                "It is " + roundedTemp + " degrees outside" );

        if (roundedTemp > 15) {
            infoText.setText("It's warm enough alright");
        } else {
            infoText.setText("Might be a bit too cold to sunbathe right now");
        }

    }

    public void pickCityDialog(View v) {
        LayoutInflater linf = LayoutInflater.from(this);
        final View inflater = linf.inflate(R.layout.pick_city_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Here you can choose a city to display its weather instead of your location's.");
        alert.setView(inflater);

        final EditText cityEt = (EditText) inflater.findViewById(R.id.pcDialogCity);
        final EditText countryEt = (EditText) inflater.findViewById(R.id.pcDialogCountry);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String city = cityEt.getText().toString().replace(" ", "_");
                String country = countryEt.getText().toString().replace(" ", "_");

                usingLocation = false;
                locationCity = city;
                locationCountry = country;

                Log.d("PickCityDialog", "City: " + city + ", Country: " + country);

                getWeather();
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
}
