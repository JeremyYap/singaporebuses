package com.jeremyy.singaporebuses;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    BusServicesAdapter mAdapter;
    ArrayList<BusService> mBusServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new BusServicesAdapter(this, new ArrayList<BusService>());

        if (savedInstanceState != null) {
            mBusServices = savedInstanceState.getParcelableArrayList("busServices");
            if (mBusServices != null)
                mAdapter.addAll(mBusServices);
        }

        ListView arrivalList = (ListView) findViewById(R.id.arrival_list);
        arrivalList.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchBusArrivalData(45439);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("busServices", mBusServices);
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

    private void fetchBusArrivalData(int busStopId) {
        final String BUS_ARRIVAL_BASE_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusArrival";
        final String ACCEPT_HEADER = "accept";
        final String ACCOUNT_KEY_HEADER = "AccountKey";
        final String UNIQUE_USER_ID_HEADER = "UniqueUserID";
        final String BUS_STOP_ID_PARAM = "BusStopID";
        final String SST_PARAM = "SST";
        final String SST = "True";

        Uri builtUri = Uri.parse(BUS_ARRIVAL_BASE_URL).buildUpon()
                .appendQueryParameter(BUS_STOP_ID_PARAM, String.valueOf(busStopId))
                .appendQueryParameter(SST_PARAM, SST)
                .build();
        String url = builtUri.toString();

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
                headers.put(ACCEPT_HEADER, "application/json");
                headers.put(ACCOUNT_KEY_HEADER, BuildConfig.DATAMALL_ACCOUNT_KEY);
                headers.put(UNIQUE_USER_ID_HEADER, BuildConfig.DATAMALL_UNIQUE_USER_ID);
                return headers;
            }
        };

        MySingleton requestQueue = MySingleton.getInstance(getApplicationContext());
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

            MySingleton requestQueue = MySingleton.getInstance(getApplicationContext());
            requestQueue.addToRequestQueue(request);
        }
    }

    private void updateBusArrivalList(JSONObject response, long currentMillis) {
        try {
            JSONArray services = response.getJSONArray("Services");
            mBusServices = BusService.fromJson(services, currentMillis);
            mAdapter.clear();
            mAdapter.addAll(mBusServices);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}