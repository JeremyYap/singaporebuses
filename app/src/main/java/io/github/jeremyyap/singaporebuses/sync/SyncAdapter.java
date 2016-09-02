package io.github.jeremyyap.singaporebuses.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.github.jeremyyap.singaporebuses.R;
import io.github.jeremyyap.singaporebuses.data.BusServiceContract;

/**
 * Created by jeremyy on 9/2/2016.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * 60 * 24 * 28;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL/20 + 1;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        int page = 0;
        int resultsFetched;
        try {
            do {
                String response = getBusStopsByPage(page++);
                JSONArray jsonArray = new JSONObject(response).getJSONArray("value");
                resultsFetched = jsonArray.length();
                ContentValues[] cvArray = new ContentValues[resultsFetched];

                for (int i = 0; i < resultsFetched; i++) {
                    JSONObject busStop = jsonArray.getJSONObject(i);
                    cvArray[i] = new ContentValues();
                    cvArray[i].put(BusServiceContract.BusStopEntry._ID, busStop.getInt("BusStopCode"));
                    cvArray[i].put(BusServiceContract.BusStopEntry.COLUMN_NAME_ROAD, busStop.getString("RoadName"));
                    cvArray[i].put(BusServiceContract.BusStopEntry.COLUMN_NAME_DESC, busStop.getString("Description"));
                    cvArray[i].put(BusServiceContract.BusStopEntry.COLUMN_NAME_LATITUDE, busStop.getDouble("Latitude"));
                    cvArray[i].put(BusServiceContract.BusStopEntry.COLUMN_NAME_LONGITUDE, busStop.getDouble("Longitude"));
                }
                getContext().getContentResolver().bulkInsert(BusServiceContract.BusStopEntry.CONTENT_URI, cvArray);
            } while (resultsFetched > 0);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error", e);
        }
    }

    private String getBusStopsByPage(int page) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String busStopInfoStr = null;

        try {
            URL url = new URL(DataMallHelper.buildBusStopUrl(page));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty(DataMallHelper.ACCOUNT_KEY_HEADER, DataMallHelper.ACCOUNT_KEY);
            urlConnection.setRequestProperty(DataMallHelper.UNIQUE_USER_ID_HEADER, DataMallHelper.UNIQUE_USER_ID);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            busStopInfoStr = sb.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally{
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
        return busStopInfoStr;
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( accountManager.getPassword(newAccount) == null ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), SYNC_INTERVAL);
        }
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
