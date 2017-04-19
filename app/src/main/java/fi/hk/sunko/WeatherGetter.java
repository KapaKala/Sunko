package fi.hk.sunko;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class WeatherGetter extends AsyncTask<String, Void, JSONObject> {
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

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

            result =  new JSONObject(builder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONObject r) {
        super.onPostExecute(r);
    }
}
