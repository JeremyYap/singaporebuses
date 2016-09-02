package io.github.jeremyyap.singaporebuses.sync;

import android.net.Uri;

import io.github.jeremyyap.singaporebuses.BuildConfig;

/**
 * Created by jeremyy on 9/2/2016.
 */
public final class DataMallHelper {
    public static final  String ACCEPT_HEADER = "accept";
    public static final String ACCOUNT_KEY_HEADER = "AccountKey";
    public static final String UNIQUE_USER_ID_HEADER = "UniqueUserID";
    public static final String ACCOUNT_KEY = BuildConfig.DATAMALL_ACCOUNT_KEY;
    public static final String UNIQUE_USER_ID = BuildConfig.DATAMALL_UNIQUE_USER_ID;

    public static String buildBusArrivalUrl(int busStopId) {
        final String BUS_ARRIVAL_BASE_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusArrival";
        final String BUS_STOP_ID_PARAM = "BusStopID";
        final String SST_PARAM = "SST";
        final String SST = "True";

        Uri builtUri = Uri.parse(BUS_ARRIVAL_BASE_URL).buildUpon()
                .appendQueryParameter(BUS_STOP_ID_PARAM, String.valueOf(busStopId))
                .appendQueryParameter(SST_PARAM, SST)
                .build();
        return builtUri.toString();
    }

    public static String buildBusStopUrl(int page) {
        final int PAGE_SIZE = 50;
        final String BUS_STOP_BASE_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusStops";
        final String SKIP_PARAM = "$skip";

        int skip = PAGE_SIZE * page;

        Uri builtUri = Uri.parse(BUS_STOP_BASE_URL).buildUpon()
                .appendQueryParameter(SKIP_PARAM, String.valueOf(skip))
                .build();
        return builtUri.toString();
    }
}
