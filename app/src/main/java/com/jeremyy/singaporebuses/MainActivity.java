package com.jeremyy.singaporebuses;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<BusService> mAdapter;
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
            mAdapter.addAll(mBusServices);
        }

        ListView arrivalList = (ListView) findViewById(R.id.arrival_list);
        arrivalList.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchBusArrivalTask().execute(45439);
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

    public class FetchBusArrivalTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            final String LOG_TAG = FetchBusArrivalTask.class.getSimpleName();
            final String BUS_ARRIVAL_BASE_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusArrival";
            final String ACCOUNT_KEY = BuildConfig.DATAMALL_ACCOUNT_KEY;
            final String UNIQUE_USER_ID = BuildConfig.DATAMALL_UNIQUE_USER_ID;
            final String ACCEPT_HEADER = "accept";
            final String ACCOUNT_KEY_HEADER = "AccountKey";
            final String UNIQUE_USER_ID_HEADER = "UniqueUserID";
            final String BUS_STOP_ID_PARAM = "BusStopID";
            final String SST_PARAM = "SST";
            final String SST = "True";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonString = null;

            try {
                Uri builtUri = Uri.parse(BUS_ARRIVAL_BASE_URL).buildUpon()
                        .appendQueryParameter(BUS_STOP_ID_PARAM, params[0].toString())
                        .appendQueryParameter(SST_PARAM, SST)
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.addRequestProperty(ACCEPT_HEADER, "application/json");
                urlConnection.addRequestProperty(ACCOUNT_KEY_HEADER, ACCOUNT_KEY);
                urlConnection.addRequestProperty(UNIQUE_USER_ID_HEADER, UNIQUE_USER_ID);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonString = buffer.toString();
            } catch (final IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return jsonString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonResult = new JSONObject(result);
                JSONArray services = jsonResult.getJSONArray("Services");
                mBusServices = BusService.fromJson(services);
                mAdapter.clear();
                mAdapter.addAll(mBusServices);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
