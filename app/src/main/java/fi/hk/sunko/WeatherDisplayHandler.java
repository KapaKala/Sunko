package fi.hk.sunko;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

class WeatherDisplayHandler {
    int setImage(String weatherType, double currentHour, double sunrise, double sunset) {

        if (currentHour < sunset && currentHour > sunrise) {
            switch (weatherType) {
                case "Clear":
                    return R.drawable.ic_sun;
                case "Fog":
                case "Haze":
                    return R.drawable.ic_fog;
                case "Cloudy":
                    return R.drawable.ic_cloud;
                case "Drizzle":
                    return R.drawable.ic_cloud_drizzle;
                case "Rain":
                    return R.drawable.ic_cloud_rain;
                case "Light Rain Showers":
                    return R.drawable.ic_cloud_rain_2;
                case "Chance of Rain":
                    return R.drawable.ic_cloud_rain_sun;
                case "Chance of a Thunderstorm":
                    return R.drawable.ic_cloud_lightning_sun;
                case "Thunderstorm":
                    return R.drawable.ic_cloud_lightning;
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
                    return R.drawable.ic_mood_bad_black_24dp;
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
                case "Chance of Rain":
                    return R.drawable.ic_cloud_rain_moon;
                case "Chance of a Thunderstorm":
                    return R.drawable.ic_cloud_lightning_moon;
                case "Thunderstorm":
                    return R.drawable.ic_cloud_lightning;
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
                    return R.drawable.ic_mood_bad_black_24dp;
            }
        }


    }

    Shader setBackgroundGradient(int h, String weatherType, double currentHour, double sunrise, double sunset) {
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#FF4E50"), Color.parseColor("#F9D423"), Shader.TileMode.REPEAT));

        if (currentHour < sunrise + 1 && currentHour > sunrise - 1 && weatherType.equals("Clear")) {
            return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#58bef2"), Color.parseColor("#ffe280"), Shader.TileMode.REPEAT));
        } else if (currentHour < sunset + 1 && currentHour > sunset - 1 && weatherType.equals("Clear")) {
            return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#0b184b"), Color.parseColor("#ea8eba"), Shader.TileMode.REPEAT));
        } else if (currentHour < sunset && currentHour > sunrise) {
            switch (weatherType) {
                case "Clear":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#0b52c4"), Color.parseColor("#6dc8f6"), Shader.TileMode.REPEAT));
                case "Mostly Cloudy":
                case "Overcast":
                case "Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#1a222f"), Color.parseColor("#3e5c80"), Shader.TileMode.REPEAT));
                case "Light Rain Showers":
                case "Drizzle":
                case "Rain":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#5b7089"), Color.parseColor("#91acd2"), Shader.TileMode.REPEAT));
                case "Ice Pellets":
                case "Snow":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#606b80"), Color.parseColor("#dadde2"), Shader.TileMode.REPEAT));
                case "Scattered Clouds":
                case "Partly Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#4c87c3"), Color.parseColor("#bad1e1"), Shader.TileMode.REPEAT));
                default:
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#cccccc"), Color.parseColor("#dedede"), Shader.TileMode.REPEAT));
            }
        } else {
            switch (weatherType) {
                case "Clear":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#040d30"), Color.parseColor("#383d95"), Shader.TileMode.REPEAT));
                case "Mostly Cloudy":
                case "Overcast":
                case "Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#283341"), Color.parseColor("#435a79"), Shader.TileMode.REPEAT));
                case "Drizzle":
                case "Light Rain Showers":
                case "Rain":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#0f1632"), Color.parseColor("#534f60"), Shader.TileMode.REPEAT));
                case "Ice Pellets":
                case "Snow":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#1c283f"), Color.parseColor("#7e96b7"), Shader.TileMode.REPEAT));
                case "Scattered Clouds":
                case "Partly Cloudy":
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#16202c"), Color.parseColor("#34587d"), Shader.TileMode.REPEAT));
                default:
                    return mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#cccccc"), Color.parseColor("#dedede"), Shader.TileMode.REPEAT));
            }
        }
    }

    String setInfoText(String weatherType, int temperature, double currentHour, double sunrise, double sunset) {
        if (temperature > 16) {
            if (currentHour < sunset && currentHour > sunrise) {
                switch (weatherType) {
                    case "Clear":
                        return "Weather seems nice, go outside, get some sun!";
                    case "Overcast":
                    case "Cloudy":
                    case "Mostly Cloudy":
                        return "Not really the best weather for getting sun";
                    case "Drizzle":
                    case "Rain":
                    case "Light Rain Showers":
                        return "You might need an umbrella";
                    case "Scattered Clouds":
                    case "Partly Cloudy":
                        return "You might be able to catch some sun";
                    default:
                        return "Oops!";
                }
            } else {
                return "Can't really sunbathe at night";
            }
        } else if (temperature > 8){
            return "It's kinda chilly, you might need a jacket";
        } else {
            return "It's cold, grab a jacket";
        }


    }
}
