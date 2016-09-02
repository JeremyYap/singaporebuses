package io.github.jeremyyap.singaporebuses.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by jeremyy on 9/2/2016.
 */
public class BusServiceProvider extends ContentProvider {

    private BusServiceDbHelper mOpenHelper;
    private static UriMatcher sUriMatcher = buildUriMatcher();
    static final int BUS_STOP = 100;
    static final int BUS_STOP_WITH_ID = 101;
    static final int BUS_STOP_NEARBY = 102;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BusServiceContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, BusServiceContract.PATH_BUS_STOP, BUS_STOP);
        matcher.addURI(authority, BusServiceContract.PATH_BUS_STOP + "/#", BUS_STOP_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BusServiceDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case BUS_STOP:
                break;
            case BUS_STOP_WITH_ID:
                long busStopId = BusServiceContract.BusStopEntry.getBusStopIdFromUri(uri);
                selection = BusServiceContract.BusStopEntry._ID + " = ?";
                selectionArgs = new String[]{Long.toString(busStopId)};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return mOpenHelper.getReadableDatabase().query(
                BusServiceContract.BusStopEntry.TABLE_NAME,
                projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case BUS_STOP:
                return BusServiceContract.BusStopEntry.CONTENT_TYPE;
            case BUS_STOP_WITH_ID:
                return BusServiceContract.BusStopEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri retUri;

        switch (sUriMatcher.match(uri)) {
            case BUS_STOP:
                long _id = db.insert(BusServiceContract.BusStopEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    retUri = BusServiceContract.BusStopEntry.buildBusStopUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case BUS_STOP:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BusServiceContract.BusStopEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        if (selection == null) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case BUS_STOP:
                rowsDeleted = db.delete(BusServiceContract.BusStopEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        if (selection == null) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case BUS_STOP:
                rowsUpdated = db.update(BusServiceContract.BusStopEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
