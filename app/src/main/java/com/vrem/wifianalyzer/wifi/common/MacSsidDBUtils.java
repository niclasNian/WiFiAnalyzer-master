package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MacSsidDBUtils {
    private static final String TAG = MacSsidDBUtils.class.getSimpleName();
    static final int DATABASE_VERSION = 1;
    // DB名
    public static final String DATABASE_NAME = "MacSsidDb";

    public static final String TABLE_MACSSID_TABLE ="macssid";
    public static final String KEY_ROWID = "_id"; // integer 自增长，主key
    public static final String DEVID = "devid"; // dev唯一识别号
    public static final String MAC = "mac";
    public static final String SSID = "ssid";

    final Context mContext;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    public MacSsidDBUtils(Context context) {
        this.mContext = context;
        mDBHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder createMacSsidTable = new StringBuilder();
            createMacSsidTable.append("create table ").append(TABLE_MACSSID_TABLE).append(" ( ")
                    .append(KEY_ROWID).append(" integer primary key autoincrement, ")
                    .append(DEVID).append(" text, ")
                    .append(MAC).append(" text, ")
                    .append(SSID).append(" text, ")
                    .append("unique (").append(DEVID).append(",").append(MAC).append(")")
                    .append(");");
            db.execSQL(createMacSsidTable.toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Auto-generated method stub
            Log.w(TAG, "Upgrading database from version " + oldVersion + "to " +
                    newVersion + ", which will destroy all old data" + TABLE_MACSSID_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MACSSID_TABLE);
            onCreate(db);
        }
    }

    public MacSsidDBUtils open() {
        mDb = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    public void insertOrUpdate(String devID, String mac, String ssid) {
        StringBuilder sql1 = new StringBuilder();
        sql1.append("insert or ignore into ").append(TABLE_MACSSID_TABLE).append(" (")
                .append(DEVID).append(", ")
                .append(MAC).append(", ")
                .append(SSID).append(" ")
                .append(") values ('")
                .append(devID).append("', '")
                .append(mac).append("', '")
                .append(ssid).append("')");
        mDb.execSQL(sql1.toString());
        //Log.w("SQL", sql1.toString());

        String sql2 = "update macssid set ssid='" + ssid + "' where devid='" + devID + "' and mac='" + mac + "'";
        mDb.execSQL(sql2);
        //Log.w("SQL", sql2.toString());
    }

    public String getSSID(String devID, String mac) {
        String sql = "select ssid from macssid where devid='" + devID + "' and mac='" + mac + "'";

        String ssid = "";
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            ssid = cursor.getString(0);
            break;
        }

        return ssid;
    }
}
