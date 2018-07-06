package com.vrem.wifianalyzer.wifi.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public class InteractRecordDBUtils {
    private static final String TAG = InteractRecordDBUtils.class.getSimpleName();
    static final int DATABASE_VERSION = 2;
    // DB名
    public static final String DATABASE_NAME = "InteractRecordDb";

    public static final String TABLE_INTERACTRECORD_TABLE ="interactrecord";
    public static final String KEY_ROWID = "_id"; // integer 自增长，主key
    public static final String DEVID = "devid"; // dev唯一识别号
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String TIME = "time";

    final Context mContext;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    public InteractRecordDBUtils(Context context) {
        this.mContext = context;
        mDBHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder createInteractRecordTable = new StringBuilder();
            createInteractRecordTable.append("create table ").append(TABLE_INTERACTRECORD_TABLE).append(" ( ")
                    .append(KEY_ROWID).append(" integer primary key autoincrement, ")
                    .append(DEVID).append(" text, ")
                    .append(REQUEST).append(" text, ")
                    .append(RESPONSE).append(" text, ")
                    .append(TIME).append(" integer ")
                    .append(");");
            db.execSQL(createInteractRecordTable.toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Auto-generated method stub
            Log.w(TAG, "Upgrading database from version " + oldVersion + "to " +
                    newVersion + ", which will destroy all old data" + TABLE_INTERACTRECORD_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERACTRECORD_TABLE);
            onCreate(db);
        }
    }

    public InteractRecordDBUtils open() {
        mDb = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    public void insert(String request, String response) {
        Time time = new Time();
        time.setToNow();
        long now = time.toMillis(false);

        ContentValues contentValues = new ContentValues();
        contentValues.put(REQUEST, request);
        contentValues.put(RESPONSE, response);
        contentValues.put(TIME, now);

        mDb.insert(TABLE_INTERACTRECORD_TABLE, null, contentValues);
    }

    public void easy_insert(String request, String response) {
        open();
        insert(request, response);
        close();
    }
}
