package spartons.com.googlemapspolylineexample.distanceModule;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class DistanceFinder {

    private static final String DISTANCE_MATRIX_DIRECTION_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?";
    private static final String GOOGLE_DIRECTION_API_KEY = "";  // replace with your google direction api
    private DistanceFinderListener listener;
    private String origin;
    // will be a list in the future
    private String destination;

    public DistanceFinder(DistanceFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDistanceFinderStart();
        new DownloadRawData().execute(createUrl());
    }
    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");
        String mode = "walking";
        return DISTANCE_MATRIX_DIRECTION_URL + "origins=" + urlOrigin + "&destinations=" + urlDestination + "&mode=" + mode + "&key=" + GOOGLE_DIRECTION_API_KEY;
    }



    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Destination> destinations = new ArrayList<>();

        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRows = jsonData.getJSONArray("rows");
        // only the fist origin
        JSONObject jsonRow = jsonRows.getJSONObject(0);
        JSONArray jsonDestinations = jsonRow.getJSONArray("elements");
        for (int i = 0; i < jsonDestinations.length(); i++) {
            JSONObject jsonDestination = jsonDestinations.getJSONObject(i);
            Destination destination = new Destination();
            JSONObject jsonDistance = jsonDestination.getJSONObject("distance");
            destination.distance = new spartons.com.googlemapspolylineexample.distanceModule.Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            // change in the future for multiple destinations
            destination.location = this.destination;
            destinations.add(destination);
        }

        listener.onDistanceFinderSuccess(destinations);
    }




    @SuppressLint("StaticFieldLeak")
    private class DownloadRawData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String link = params[0];
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }






}

