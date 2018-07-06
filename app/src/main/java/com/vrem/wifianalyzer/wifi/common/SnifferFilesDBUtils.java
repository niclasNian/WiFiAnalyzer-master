package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SnifferFilesDBUtils {
    private static final String TAG = SnifferFilesDBUtils.class.getSimpleName();
    static final int DATABASE_VERSION = 3;
    // DB名
    public static final String DATABASE_NAME = "SnifferFilesStatusDb";

    public static final String TABLE_SNIFFERFILES_TABLE ="snifferfiles";
    public static final String KEY_ROWID = "_id"; // integer 自增长，主key
    public static final String DEVID = "devid"; // dev唯一识别号
    public static final String FILE = "file";
    public static final String ESSID = "essid";

    final Context mContext;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    public SnifferFilesDBUtils(Context context) {
        this.mContext = context;
        mDBHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建Permission信息表
            StringBuilder createSnifferFilesTable = new StringBuilder();
            createSnifferFilesTable.append("create table ").append(TABLE_SNIFFERFILES_TABLE).append(" ( ")
                    .append(KEY_ROWID).append(" integer primary key autoincrement, ")
                    .append(DEVID).append(" text, ")
                    .append(FILE).append(" text, ")
                    .append(ESSID).append(" text ")
                    .append(");");
            db.execSQL(createSnifferFilesTable.toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Auto-generated method stub
            Log.w(TAG, "Upgrading database from version " + oldVersion + "to " +
                    newVersion + ", which will destroy all old data" + TABLE_SNIFFERFILES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SNIFFERFILES_TABLE);
            onCreate(db);
        }
    }

    public SnifferFilesDBUtils open() {
        mDb = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    public void insertNewFile(String devID, String file, String essid) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(TABLE_SNIFFERFILES_TABLE).append(" (")
                .append(DEVID).append(", ")
                .append(FILE).append(", ")
                .append(ESSID).append(" ")
                .append(") values ('")
                .append(devID).append("', '")
                .append(file).append("', '")
                .append(essid).append("')");
        mDb.execSQL(sql.toString());
    }

    public String getHandling(String devID) {
        String sql = "select handling from " + TABLE_SNIFFERFILES_TABLE + " where devid='" + devID + "'";

        String handling = "";
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            handling = cursor.getString(0);
            break;
        }

        return handling;
    }

    public int getScanstep1done(String devID) {
        String sql = "select scanstep1done from " + TABLE_SNIFFERFILES_TABLE + " where devid='" + devID + "'";

        int scanStep1Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            scanStep1Done = cursor.getInt(0);
            break;
        }

        return scanStep1Done;
    }

    public Map<Integer,SnifferFile> getSnifferFiles(String devID){
        Map<Integer,SnifferFile> snifferFileMap = new HashMap<>();

        String sql = "select " + KEY_ROWID  + "," + DEVID + "," + FILE + "," + ESSID + " from " + TABLE_SNIFFERFILES_TABLE + " where devid='" + devID + "' order by " + KEY_ROWID + " desc" ;
        Log.w("SQL", sql);

        Cursor cursor = mDb.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            SnifferFile snifferFile = new SnifferFile();
            snifferFile.rowID = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
            snifferFile.devID = cursor.getString(cursor.getColumnIndex(DEVID));
            snifferFile.file = cursor.getString(cursor.getColumnIndex(FILE));
            snifferFile.essid = cursor.getString(cursor.getColumnIndex(ESSID));
            snifferFileMap.put(snifferFile.rowID, snifferFile);
        }
        cursor.close();

        return snifferFileMap;
    }
}
