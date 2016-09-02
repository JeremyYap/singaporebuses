package io.github.jeremyyap.singaporebuses.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.jeremyyap.singaporebuses.data.BusServiceContract.BusStopEntry;

/**
 * Created by jeremyy on 9/2/2016.
 */
public class BusServiceDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BusService.db";

    public BusServiceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_BUS_STOP_TABLE = "CREATE TABLE " + BusStopEntry.TABLE_NAME + " (" +
                BusStopEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, " +
                BusStopEntry.COLUMN_NAME_DESC + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_NAME_ROAD + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_NAME_LATITUDE + " REAL NOT NULL, " +
                BusStopEntry.COLUMN_NAME_LONGITUDE + " REAL NOT NULL )";

        db.execSQL(SQL_CREATE_BUS_STOP_TABLE);

        final String SQL_CREATE_BUS_STOP_LOCATION_INDEX = "CREATE INDEX location_idx ON " +
                BusStopEntry.TABLE_NAME + " (" +
                BusStopEntry.COLUMN_NAME_LATITUDE + ", " +
                BusStopEntry.COLUMN_NAME_LONGITUDE + ")";

        db.execSQL(SQL_CREATE_BUS_STOP_LOCATION_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + BusStopEntry.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
