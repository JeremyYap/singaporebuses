package io.github.jeremyyap.singaporebuses.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jeremyy on 9/2/2016.
 */
public final class BusServiceContract {

    public static final String CONTENT_AUTHORITY = "io.github.jeremyyap.singaporebuses";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BUS_STOP = "stop";

    private BusServiceContract() {}

    public static class BusStopEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUS_STOP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BUS_STOP;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BUS_STOP;

        public static final String TABLE_NAME = "stop";
        public static final String COLUMN_NAME_ROAD = "road";
        public static final String COLUMN_NAME_DESC = "description";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";

        public static Uri buildBusStopUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getBusStopIdFromUri(Uri uri) {
            return Long.valueOf(uri.getPathSegments().get(1));
        }
    }
}
