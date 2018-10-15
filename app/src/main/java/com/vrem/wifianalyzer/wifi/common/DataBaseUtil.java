package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

import com.vrem.wifianalyzer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * copy数据库到apk包
 * 
 * @author WN
 * @date 2018/09/10
 * 
 */
public class DataBaseUtil {

	private Context context;
	public static String dbName = "companymac.db";// 数据库的名字
	private static String DATABASE_PATH;// 数据库在手机里的路径

    public DataBaseUtil(){

    }

	public DataBaseUtil(Context context) {
		this.context = context;
		String packageName = context.getPackageName();
		DATABASE_PATH="/data/data/"+packageName+"/databases/";
	}

	/**
	 * 判断数据库是否存在
	 * 
	 * @return false or true
	 */
	public boolean checkDataBase() {
		SQLiteDatabase db = null;
		try {
			String databaseFilename = DATABASE_PATH + dbName;
			db = SQLiteDatabase.openDatabase(databaseFilename, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {

		}
		if (db != null) {
			db.close();
		}
		return db != null ? true : false;
	}

	/**
	 * 复制数据库到手机指定文件夹下
	 * 
	 * @throws IOException
	 */
	public void copyDataBase() throws IOException {
		String databaseFilenames = DATABASE_PATH + dbName;
		File dir = new File(DATABASE_PATH);
		if (!dir.exists())// 判断文件夹是否存在，不存在就新建一个
			dir.mkdir();
		FileOutputStream os = new FileOutputStream(databaseFilenames);// 得到数据库文件的写入流
		InputStream is = context.getResources().openRawResource(R.raw.companymac );// 得到数据库文件的数据流
		byte[] buffer = new byte[8192];
		int count = 0;
		while ((count = is.read(buffer)) > 0) {
			os.write(buffer, 0, count);
			os.flush();
		}
		is.close();
		os.close();
	}

	/**
     * 初始化厂商数据库
     * */
	public void initMacDB(){
	    //Environment 访问外部环境的类，MEDIA_MOUNTED表明对象是否存在并具有读写权限
	    boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	    if (hasSDCard){
            DataBaseUtil util = new DataBaseUtil(context);
            boolean dbExist = util.checkDataBase();
            if (dbExist) {
            } else {
                try {
                    util.copyDataBase();
                } catch (IOException e) {
                    throw new Error("Error copying database");
                }
            }
        }else {

        }
    }

    /**
     * 查询mac厂商
	 * @date 2018/09/11 11:23
	 * @author wn
     * @param mac 查询的mac地址 即BSSID
     * @return company_info为空返回无厂商信息，否则返回查询到的数据
     * */
    public String queryCompany(Context context,String mac){
        SQLiteDatabase db = context.openOrCreateDatabase("companymac.db",
                Context.MODE_PRIVATE, null);
        String company_info = null;
        String pre_mac = null;
        pre_mac = mac.substring(0, 8).replace(":", "").toUpperCase();
        Cursor c = db.rawQuery("SELECT * FROM companymac WHERE mac ='" + pre_mac + "'", null);
        while (c.moveToNext()) {
            company_info = c.getString(c.getColumnIndex("company"));
        }
        c.close();
        db.close();
        return company_info == null ? "无厂商信息":company_info;
    }

}
