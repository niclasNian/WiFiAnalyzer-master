package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DevStatusDBUtils {
    private static final String TAG = DevStatusDBUtils.class.getSimpleName();
    static final int DATABASE_VERSION = 4;
    // DB名
    public static final String DATABASE_NAME = "DevStatusDb";

    public static final String TABLE_DEVSTATUS_TABLE ="devstatus";
    public static final String KEY_ROWID = "_id"; // integer 自增长，主key
    public static final String DEVID = "devid"; // dev唯一识别号
    public static final String HANDLING = "handling"; // 有内容 - dev正在处理相应请求（handshake|...）；空串 - dev空闲
    public static final String HANDLINGDETAIL = "handlingdetail"; // case handshake - handshake抓包结果对应的文件名; case fakeap - ap name; case dos - dos攻击对象详情，形如"abc" | "11" | "9,10,11"
    public static final String SCANSTEP1DONE = "scanstep1done"; // 1-y; 0-n
    public static final String HANDSHAKESTEP1DONE = "handshakestep1done"; // 1-y;0-n // dos
    public static final String HANDSHAKESTEP2DONE = "handshakestep2done"; // 1-y;0-n // common handshake
    public static final String HANDSHAKEPREPARECOUNT = "handshakepreparecount"; // dos timer
    public static final String FAKEAPSTEP1DONE = "fakeapstep1done"; // 1-y;0-n
    public static final String DOSSTEP1DONE = "dosstep1done"; // 1-y;0-n

    public static final String WPSCRACK = "wpscrack";//破解

    final Context mContext;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    public DevStatusDBUtils(Context context) {
        this.mContext = context;
        mDBHelper = new DatabaseHelper(mContext);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder createDevStatusTable = new StringBuilder();
            createDevStatusTable.append("create table ").append(TABLE_DEVSTATUS_TABLE).append(" ( ")
                    .append(KEY_ROWID).append(" integer primary key autoincrement, ")
                    .append(DEVID).append(" text unique, ")
                    .append(HANDLING).append(" text, ")
                    .append(HANDLINGDETAIL).append(" text, ")
                    .append(SCANSTEP1DONE).append(" integer, ")
                    .append(HANDSHAKESTEP1DONE).append(" integer, ")
                    .append(HANDSHAKESTEP2DONE).append(" integer, ")
                    .append(HANDSHAKEPREPARECOUNT).append(" integer, ")
                    .append(FAKEAPSTEP1DONE).append(" integer, ")
                    .append(WPSCRACK).append(" integer, ")
                    .append(DOSSTEP1DONE).append(" integer ")
                    .append(");");
            db.execSQL(createDevStatusTable.toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Auto-generated method stub
            Log.w(TAG, "Upgrading database from version " + oldVersion + "to " +
                    newVersion + ", which will destroy all old data" + TABLE_DEVSTATUS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVSTATUS_TABLE);
            onCreate(db);
        }
    }

    public DevStatusDBUtils open() {
        mDb = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    public void tryInsertNewDev(String devID) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert or ignore into ").append(TABLE_DEVSTATUS_TABLE).append(" (")
                .append(DEVID).append(", ")
                .append(HANDLING).append(", ")
                .append(HANDLINGDETAIL).append(", ")
                .append(SCANSTEP1DONE).append(", ")
                .append(HANDSHAKESTEP1DONE).append(", ")
                .append(HANDSHAKESTEP2DONE).append(", ")
                .append(HANDSHAKEPREPARECOUNT).append(", ")
                .append(FAKEAPSTEP1DONE).append(", ")
                .append(WPSCRACK).append(", ")
                .append(DOSSTEP1DONE).append(" ")
                .append(") values ('")
                .append(devID).append("', '', '', 0, 0, 0, 0, 0, 0, 0)");
        mDb.execSQL(sql.toString());
    }

    public void scanStep1Done(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=1, handling='', handlingdetail='', handshakestep1done=0, handshakestep2done=0, handshakepreparecount=0, fakeapstep1done=0, dosstep1done=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新1：SQL：", sql);
    }

    public void wpscrackStep1Done(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=1, handling='', handlingdetail='', handshakestep1done=0, handshakestep2done=0, handshakepreparecount=0, fakeapstep1done=0, dosstep1done=0, wpscrack=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新1：SQL：", sql);
    }

    public void scanStep2Error(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0,handling='' where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新2：SQL：", sql);
    }

    public void preScan(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set handling='' where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("exit=true执行的SQL:", sql);
    }

    public void handshakeStep1Done(String devID, String detail) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0, handling='handshake', handlingdetail='" + detail + "', handshakestep1done=1,handshakestep2done=0,handshakepreparecount=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新3 ：SQL", sql);
    }

    public void handshakeStep2DoneDos(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0, handling='handshake',handshakestep2done=1,handshakepreparecount=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新4：SQL", sql);
    }

    public void handshakeStep2Done(String devID, String detail) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0, handling='handshake', handlingdetail='" + detail + "', handshakestep2done=1,handshakepreparecount=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新5：SQL", sql);
    }

    public void handshakePrepareCountUpdate(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set handshakepreparecount=handshakepreparecount+1 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新6：SQL", sql);
    }

    public int getHandshakepreparecount(String devID) {
        String sql = "select handshakepreparecount from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        int h = -1;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            h = cursor.getInt(0);
            break;
        }

        return h;
    }

    public void handshakeCancel(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set handling='', handlingdetail='', handshakestep1done=0, handshakestep2done=0, handshakepreparecount=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新7：SQL", sql);
    }

    public void fakeAPStep1Done(String devID, String out, String detail) { // other's step1done set 0 needn't be handled here, but in 空闲 dev ops, which is in main thread, scanStep1Done
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0, handling='fakeap_" + out + "', handlingdetail='" + detail + "', fakeapstep1done=1 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新8：SQL", sql);
    }

    public void fakeAPCancel(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set handling='', handlingdetail='', fakeapstep1done=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新9：SQL", sql);
    }

    public void crackStep1Done(String devID) { // other's step1done set 0 needn't be handled here, but in 空闲 dev ops, which is in main thread
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0, handling='', handlingdetail='', dosstep1done=0 , wpscrack=1 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新10：SQL：", sql);
    }

    public void dosStep1Done(String devID, String type, String detail) { // other's step1done set 0 needn't be handled here, but in 空闲 dev ops, which is in main thread
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0, handling='dos_" + type + "', handlingdetail='" + detail + "', dosstep1done=1 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新10：SQL：", sql);
    }

    public void dosCancel(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set handling='', handlingdetail='', dosstep1done=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新11：SQL：", sql);
    }

    public void preHandling(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("更新12：SQL", sql);
    }

    /*public void statusClear(String devID) {
        String sql = "update " + TABLE_DEVSTATUS_TABLE + " set scanstep1done=0,handling='',handshakestep1done=0 where devid='" + devID + "'";
        mDb.execSQL(sql);
        Log.w("SQL", sql);
    }*/

    public String getHandling(String devID) {
        String sql = "select handling from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        String handling = "";
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
//            if (cursor.getString(0).equals("")){
//                handling = "wps_crack";
//            }else {
//                handling = cursor.getString(0);
//            }
            handling = cursor.getString(0);
            break;
        }

        return handling;
    }

    public String getHandlingDetail(String devID) {
        String sql = "select handlingdetail from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        String handlingdetail = "";
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            handlingdetail = cursor.getString(0);
            break;
        }

        return handlingdetail;
    }

    public int getScanstep1done(String devID) {
        String sql = "select scanstep1done from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        int scanStep1Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            scanStep1Done = cursor.getInt(0);
            break;
        }

        return scanStep1Done;
    }

    public int getCrackstep1done(String devID) {
        String sql = "select wpscrack from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        int crackStep1Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            crackStep1Done = cursor.getInt(0);
            break;
        }

        return crackStep1Done;
    }

    public int getHandshakestep1done(String devID) {
        String sql = "select handshakestep1done from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        int handshakeStep1Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            handshakeStep1Done = cursor.getInt(0);
            break;
        }

        return handshakeStep1Done;
    }

    public int getHandshakestep2done(String devID) {
        String sql = "select handshakestep2done from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        int handshakeStep2Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            handshakeStep2Done = cursor.getInt(0);
            break;
        }

        return handshakeStep2Done;
    }

    public int getFakeapstep1done(String devID) {
        String sql = "select fakeapstep1done from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        int fakeAPStep1Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            fakeAPStep1Done = cursor.getInt(0);
            break;
        }

        return fakeAPStep1Done;
    }

    public int getDosstep1done(String devID) {
        String sql = "select dosstep1done from " + TABLE_DEVSTATUS_TABLE + " where devid='" + devID + "'";

        Log.v("查询语句:",sql);
        int dosStep1Done = 0;
        Cursor cursor = mDb.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            dosStep1Done = cursor.getInt(0);
            break;
        }

        return dosStep1Done;
    }
}
