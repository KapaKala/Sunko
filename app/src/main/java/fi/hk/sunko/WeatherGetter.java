package fi.hk.sunko;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask class for retrieving weather information from Weather Underground API
 *
 * @author Henri Kankaanpää
 * @version 1.0
 * @since 1.0
 */
class WeatherGetter extends AsyncTask<String, Void, JSONObject> {
    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    /**
     * Override method for the background task.
     *
     * Uses the URL received as a parameter to build a HttpURLConnection to the Weather Underground
     * API, reads the results, and sends them to the invoker of the class as a JSONObject.
     *
     * @param params A possible array of String parameters, in this case a single URL.
     * @return The retrieved weather data as a JSONObject
     */
    @Override
    protected JSONObject doInBackground(String... params) {
        String stringUrl = params[0];
        JSONObject result;
        String inputLine;

        try {
            URL myUrl = new URL(stringUrl);

            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                builder.append(inputLine);
            }

            reader.close();

            result = new JSONObject(builder.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }

    /**
     * Override method for when the task is done.
     *
     * @param r Returns the result to the super class
     */
    @Override
    protected void onPostExecute(JSONObject r) {
        super.onPostExecute(r);
    }
}
