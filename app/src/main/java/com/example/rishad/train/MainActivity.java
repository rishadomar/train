package com.example.rishad.train;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "MainActivity";
    protected static final String BaseUrl = "http://52.3.249.119:8083";
    /**
     * Provides the entry point to Google Play services.
     */
    GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    protected TextView mYouAreHereText;
    protected Button mWhereamiButton;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 120;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
            * FASTEST_INTERVAL_IN_SECONDS;
    //private LocationClient locationClient;
    private LocationRequest mlocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mYouAreHereText = (TextView) findViewById((R.id.youAreHere));

        //
        // Setup the whereami button
        //
        mWhereamiButton = (Button) findViewById(R.id.buttonWhereami);
        mWhereamiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.whereamiClickHandler();
            }
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void whereamiClickHandler()
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
            return;
        }
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        displayMyLocation(latitude, longitude);
    }

    private void displayMyLocation(double latitude, double longitude) {
        String url = BaseUrl + "/whereami?latitude=" +
                String.format("%f", latitude) +
                "&longitude=" +
                String.format("%f", longitude);
        System.out.println("Making request ...");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("response is: " + response);
                        try {
                            JSONObject json = new JSONObject(response);
                            String station1 = json.getString("Station1");
                            String station2 = json.getString("Station2");
                            if (station1.equals(station2)) {
                                mYouAreHereText.setText("You are here: " + station1);
                            } else {
                                mYouAreHereText.setText("You are between: " + station1 + " and " + station2);
                            }
                        } catch (JSONException e) {
                            mYouAreHereText.setText("Unable to parse response");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Eek Something went wrong!");
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        displayMyLocation(location.getLatitude(), location.getLongitude());
    }
}
