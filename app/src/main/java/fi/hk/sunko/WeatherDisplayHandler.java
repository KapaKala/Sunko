package fi.hk.sunko;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;

public class WeatherDisplayHandler {
    Context context;
//    Calendar calendar;
//    int currentTimeHours;

    public WeatherDisplayHandler(Context context) {
        this.context = context;

    }

    public int setImage(String weatherType, int currentHour, int sunrise, int sunset) {
//        calendar = Calendar.getInstance();
//        currentTimeHours = calendar.get(Calendar.HOUR_OF_DAY);
//        Log.d("CHECKING HOURS", "Current hour: " + currentTimeHours);

        if (currentHour < sunset && currentHour > sunrise) {
            switch (weatherType) {
                case "Clear":
                    return R.drawable.ic_sun;
                case "Cloudy":
                    return R.drawable.ic_cloud;
                case "Rain":
                    return R.drawable.ic_cloud_rain;
                case "Snow":
                    return R.drawable.ic_snow;
                case "Partly Cloudy":
                    return R.drawable.ic_cloud_sun;
                case "Ice Pellets":
                    return R.drawable.ic_cloud_hail;
                default:
                    return 0;
            }
        } else {
            switch (weatherType) {
                case "Clear":
                    return R.drawable.ic_moon_50;
                case "Cloudy":
                    return R.drawable.ic_cloud;
                case "Rain":
                    return R.drawable.ic_cloud_rain;
                case "Snow":
                    return R.drawable.ic_snow;
                case "Partly Cloudy":
                    return R.drawable.ic_cloud_moon;
                case "Ice Pellets":
                    return R.drawable.ic_cloud_hail;
                default:
                    return 0;
            }
        }


    }
}
