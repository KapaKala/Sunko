package fi.hk.sunko;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

class WeatherDisplayHandler {
    int setImage(String weatherType, int currentHour, int sunrise, int sunset) {

        if (currentHour < sunset && currentHour > sunrise) {
            switch (weatherType) {
                case "Clear":
                    return R.drawable.ic_sun;
                case "Cloudy":
                    return R.drawable.ic_cloud;
                case "Drizzle":
                    return R.drawable.ic_cloud_drizzle;
                case "Rain":
                    return R.drawable.ic_cloud_rain;
                case "Light Rain Showers":
                    return R.drawable.ic_cloud_rain_2;
                case "Snow":
                    return R.drawable.ic_snow;
                case "Scattered Clouds":
                case "Partly Cloudy":
                    return R.drawable.ic_cloud_sun;
                case "Mostly Cloudy":
                    return R.drawable.ic_clouds_sun;
                case "Overcast":
                    return R.drawable.ic_clouds;
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
                case "Drizzle":
                    return R.drawable.ic_cloud_drizzle;
                case "Rain":
                    return R.drawable.ic_cloud_rain;
                case "Light Rain Showers":
                    return R.drawable.ic_cloud_rain_2;
                case "Snow":
                    return R.drawable.ic_snow;
                case "Scattered Clouds":
                case "Partly Cloudy":
                    return R.drawable.ic_cloud_moon;
                case "Mostly Cloudy":
                    return R.drawable.ic_clouds_moon;
                case "Overcast":
                    return R.drawable.ic_clouds;
                case "Ice Pellets":
                    return R.drawable.ic_cloud_hail;
                default:
                    return 0;
            }
        }


    }

    Shader setBackgroundGradient(int h, String weatherType, int currentHour, int sunrise, int sunset) {
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));

        if (currentHour < sunset && currentHour > sunrise) {
            switch (weatherType) {
                case "Clear":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Drizzle":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Rain":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Light Rain Showers":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#4e4376"), Color.parseColor("#2b5876"), Shader.TileMode.REPEAT));
                case "Snow":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#dedede"), Color.parseColor("#ededed"), Shader.TileMode.REPEAT));
                case "Scattered Clouds":
                case "Partly Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Mostly Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Overcast":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Ice Pellets":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                default:
                    return null;
            }
        } else {
            switch (weatherType) {
                case "Clear":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Drizzle":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Rain":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                case "Light Rain Showers":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#4e4376"), Color.parseColor("#2b5876"), Shader.TileMode.REPEAT));
                case "Snow":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#898989"), Color.parseColor("#d7d7d7"), Shader.TileMode.REPEAT));
                case "Scattered Clouds":
                case "Partly Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#404b58"), Color.parseColor("#526072"), Shader.TileMode.REPEAT));
                case "Mostly Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#4c5259"), Color.parseColor("#647488"), Shader.TileMode.REPEAT));
                case "Overcast":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#4c5259"), Color.parseColor("#647488"), Shader.TileMode.REPEAT));
                case "Ice Pellets":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));
                default:
                    return null;
            }
        }
    }
}
