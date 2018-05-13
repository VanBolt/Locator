package educationalproject.vanja.locator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText LastName;
    private EditText Name;
    private EditText SurName;
    private EditText Pass;
    private Button Send;
    private TextView textView;

    private TextView GpsLat;
    private TextView GpsLon;
    private TextView GpsEnabled;
    private TextView NetworkLat;
    private TextView NetworkLon;
    private TextView NetworkEnabled;

    private LocationManager locationManager;
    static public final int REQUEST_LOCATION = 1;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LastName = (EditText) findViewById(R.id.lastName);
        Name = (EditText) findViewById(R.id.name);
        SurName = (EditText) findViewById(R.id.surName);
        Pass = (EditText) findViewById(R.id.pass);
        Send = (Button) findViewById(R.id.send);

        GpsLat = (TextView) findViewById(R.id.gpsLat);
        GpsLon = (TextView) findViewById(R.id.gpsLon);
        GpsEnabled = (TextView) findViewById(R.id.enabledGPS);
        NetworkLat = (TextView) findViewById(R.id.networkLat);
        NetworkLon = (TextView) findViewById(R.id.networkLon);
        NetworkEnabled = (TextView) findViewById(R.id.enabledNetwork);

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyAsyncTask().execute();
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    class MyAsyncTask extends AsyncTask<String, String, String> {

        String f, p, lon,lat, answerHTTP;

        String server = "http://ibolotov-001-site1.atempurl.com/Mobile/GetLocation";
        //String server = "http://localhost:51692/mobile/GetLocation";

        @Override
        protected void onPreExecute() {
            f = LastName.getText().toString() + " " + Name.getText().toString() + " " + SurName.getText().toString();
            p = Pass.getText().toString();
            if((GpsLat.getText().toString()!=null && GpsLat.getText().toString()!="")
                    && (GpsLon.getText().toString()!=null && GpsLon.getText().toString()!="")){
                lat = GpsLat.getText().toString();
                lon = GpsLon.getText().toString();
            }else if((NetworkLat.getText().toString()!=null && NetworkLat.getText().toString()!="")
                    && (NetworkLon.getText().toString()!=null && NetworkLon.getText().toString()!="")) {
                lat = NetworkLat.getText().toString();
                lon = NetworkLon.getText().toString();
            }

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> postDataParams = new HashMap<String, String>();
            postDataParams.put("FIO", f);
            postDataParams.put("pass", p);
            postDataParams.put("lat", lat);
            postDataParams.put("lon", lon);
            answerHTTP = performPostCall(server, postDataParams);
            if (answerHTTP == null || answerHTTP == "") {
                HttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost(params[0]);
                ResponseHandler<String> handler = new BasicResponseHandler();
                try {
                    answerHTTP = client.execute(request, handler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast toast = Toast.makeText(getApplicationContext(), answerHTTP, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public String performPostCall(String requestURL,
                                  HashMap<String, String> postDataParams) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    protected void onResume() {
        super.onResume();

        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000 * 0, 0, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                    locationListener);
            checkEnabled();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            try {
                showLocation(locationManager.getLastKnownLocation(provider));
            } catch (SecurityException e) {
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            /*if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }*/
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            GpsLat.setText(Double.toString(location.getLatitude()));
            GpsLon.setText(Double.toString(location.getLongitude()));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            NetworkLat.setText(String.valueOf(location.getLatitude()));
            NetworkLon.setText(String.valueOf(location.getLongitude()));
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f",
                location.getLatitude(), location.getLongitude());
    }

    private void checkEnabled() {
        GpsEnabled.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        NetworkEnabled.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

}
