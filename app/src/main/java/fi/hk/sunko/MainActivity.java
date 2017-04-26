package fi.hk.sunko;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    final int PERMISSION_REQUEST_ID = 0;

    boolean bound = false;
    LocationService locationService;
    LocationServiceConnection locationServiceConnection;
    ImageView weatherIcon;
    ImageView weatherBG;
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

        weatherDisplayHandler = new WeatherDisplayHandler(getApplicationContext());

        weatherIcon  = (ImageView)   findViewById(R.id.imageView);
        weatherBG    = (ImageView)   findViewById(R.id.weatherBG);
        locationText = (TextView)    findViewById(R.id.locationTextView);
        weatherText  = (TextView)    findViewById(R.id.weatherTextView);
        infoText     = (TextView)    findViewById(R.id.infoText);
        progressBar  = (ProgressBar) findViewById(R.id.progressBar);

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
        startService(intent);

        if(!bound) {
            intent = new Intent(this, LocationService.class);
            bindService(intent, locationServiceConnection, Context.BIND_NOT_FOREGROUND);
            Log.d("MainActivity", "Service Bound");
            bound = true;

        }
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (weatherIcon.getAlpha() == 0) {
                    weatherIcon.setAlpha(1f);
                    progressBar.setAlpha(0);
                }
                String location = intent.getStringExtra("location");
                String weatherType = intent.getStringExtra("weatherType");
                String temperature = intent.getStringExtra("temperature");
                int currentHour = intent.getIntExtra("currentHour", 0);
                int sunrise = intent.getIntExtra("sunrise", 6);
                int sunset = intent.getIntExtra("sunset", 21);

                locationText.setText(location);

                weatherIcon.setImageResource(weatherDisplayHandler.setImage(weatherType, currentHour, sunrise, sunset));
                setWeatherText(weatherType, temperature);

                if (weatherType.equals("Clear")) {
                    View v = findViewById(R.id.mainActivityLayout);
                    int h = v.getHeight();
                    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
                    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                    weatherBG.setBackground(mDrawable);
                    weatherBG.setAlpha(0f);
                    ObjectAnimator bgFade = ObjectAnimator.ofFloat(weatherBG, "alpha", 0, 1);
                    bgFade.setDuration(500);
                    bgFade.setInterpolator(new AccelerateInterpolator());
                    bgFade.start();
                }

                if (bound) {
                    Intent i = new Intent(MainActivity.this, LocationService.class);
                    stopService(i);

                    Log.d("MainActivity", "Service unbound");
                    unbindService(locationServiceConnection);

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
//                    Intent intent = new Intent(this, LocationService.class);
//                    startService(intent);
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView weatherIcon;

        public DownloadImageTask(ImageView weatherIcon) {
            this.weatherIcon = weatherIcon;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            final String REQUEST_METHOD = "GET";
            final int READ_TIMEOUT = 15000;
            final int CONNECTION_TIMEOUT = 15000;
            String stringUrl = params[0];

            Bitmap icon = null;
            try {
                URL myUrl = new URL(stringUrl);

                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();

                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                connection.connect();

                InputStream in = connection.getInputStream();

                icon = BitmapFactory.decodeStream(in);

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            weatherIcon.setImageResource(R.drawable.ic_moon_50);
            weatherIcon.setColorFilter(Color.DKGRAY);
//            weatherIcon.setImageBitmap(bitmap);
            weatherIcon.setScaleType(ImageView.ScaleType.FIT_XY);
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
}
