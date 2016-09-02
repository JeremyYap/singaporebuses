package io.github.jeremyyap.singaporebuses;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.jeremyyap.singaporebuses.data.BusServiceContract.BusStopEntry;
import io.github.jeremyyap.singaporebuses.models.BusServiceEstimate;
import io.github.jeremyyap.singaporebuses.sync.DataMallHelper;
import io.github.jeremyyap.singaporebuses.sync.SyncAdapter;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_FINE_LOCATION = 1;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private BusServicesAdapter mAdapter;
    private ArrayList<BusServiceEstimate> mBusServiceEstimates;
    private int mBusStopID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new BusServicesAdapter(this, new ArrayList<BusServiceEstimate>());

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (savedInstanceState != null) {
            mBusServiceEstimates = savedInstanceState.getParcelableArrayList("busServices");
            if (mBusServiceEstimates != null)
                mAdapter.addAll(mBusServiceEstimates);
        }

        ListView arrivalList = (ListView) findViewById(R.id.arrival_list);
        arrivalList.setAdapter(mAdapter);

        SyncAdapter.initializeSyncAdapter(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdates();
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("busServices", mBusServiceEstimates);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.require_location_permission_title)
                        .setMessage(R.string.require_location_permission_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
            }
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        stopLocationUpdates();
        getNearestBusStop(location.getLatitude(), location.getLongitude());
        fetchBusArrivalData(mBusStopID);
        Log.d(LOG_TAG, location.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_denied)
                        .setMessage(R.string.allow_location_access)
                        .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Intent i = new Intent();
                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + MainActivity.this.getApplicationContext().getPackageName()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                MainActivity.this.getApplicationContext().startActivity(i);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void fetchBusArrivalData(int busStopId) {
        String url = DataMallHelper.buildBusArrivalUrl(busStopId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                getNetworkTime(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(DataMallHelper.ACCEPT_HEADER, "application/json");
                headers.put(DataMallHelper.ACCOUNT_KEY_HEADER, BuildConfig.DATAMALL_ACCOUNT_KEY);
                headers.put(DataMallHelper.UNIQUE_USER_ID_HEADER, BuildConfig.DATAMALL_UNIQUE_USER_ID);
                return headers;
            }
        };

        VolleyQueueSingleton requestQueue = VolleyQueueSingleton.getInstance(getApplicationContext());
        requestQueue.addToRequestQueue(request);
    }

    public void getNetworkTime(final JSONObject jsonData) {
        final long currentMillis = Calendar.getInstance().getTime().getTime();

        boolean isUsingAutoTime = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isUsingAutoTime = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        }

        if (isUsingAutoTime) {
            updateBusArrivalList(jsonData, currentMillis);
        } else {
            String url = "http://www.timeapi.org/utc/now";

            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            final SimpleDateFormat timeApiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.US);
                            try {
                                long networkMillis = timeApiDateFormat.parse(response).getTime();
                                updateBusArrivalList(jsonData, networkMillis);
                            } catch (ParseException e) {
                                updateBusArrivalList(jsonData, currentMillis);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            updateBusArrivalList(jsonData, currentMillis);
                            error.printStackTrace();
                        }
                    });

            VolleyQueueSingleton requestQueue = VolleyQueueSingleton.getInstance(getApplicationContext());
            requestQueue.addToRequestQueue(request);
        }
    }

    private void updateBusArrivalList(JSONObject response, long currentMillis) {
        mBusServiceEstimates = BusServiceEstimate.fromJson(response, currentMillis);
        if (mBusServiceEstimates != null) {
            mAdapter.clear();
            mAdapter.addAll(mBusServiceEstimates);
        }
    }

    /**
     * Helper method to find bus stops within approx. 100 meter radius of a location
     */
    public void getNearestBusStop(double latitude, double longitude) {
        // Use estimate of 111,111 m = 1 degree latitude/longitude
        final double radius = 100.0 / 111111;
        String selection =
                BusStopEntry.COLUMN_NAME_LATITUDE + " BETWEEN ? AND ? AND " +
                        BusStopEntry.COLUMN_NAME_LONGITUDE + " BETWEEN ? AND ?";
        String[] selectionArgs = new String[4];
        selectionArgs[0] = String.valueOf(latitude - radius);
        selectionArgs[1] = String.valueOf(latitude + radius);
        selectionArgs[2] = String.valueOf(longitude - radius);
        selectionArgs[3] = String.valueOf(longitude + radius);

        String deltaX = "(" + BusStopEntry.COLUMN_NAME_LATITUDE + " - " + String.valueOf(latitude) + ")";
        String deltaY = "(" + BusStopEntry.COLUMN_NAME_LONGITUDE + " - " + String.valueOf(longitude) + ")";
        String distanceColumn =  deltaX + " * " + deltaX + " + " + deltaY + " * " + deltaY;

        final String COLUMN_NAME_DISTANCE = "distance";
        String[] projection = new String[] {
                BusStopEntry._ID,
                BusStopEntry.COLUMN_NAME_ROAD,
                BusStopEntry.COLUMN_NAME_DESC,
                distanceColumn + " AS " + COLUMN_NAME_DISTANCE
        };
        String orderBy = COLUMN_NAME_DISTANCE + " ASC";

        Cursor cursor = getContentResolver().query(BusStopEntry.CONTENT_URI, projection, selection, selectionArgs, orderBy);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            mBusStopID = cursor.getInt(cursor.getColumnIndex((BusStopEntry._ID)));
        }
        cursor.close();
    }
}