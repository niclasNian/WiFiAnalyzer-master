package com.vrem.wifianalyzer.wifi.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ListView;

import com.vrem.wifianalyzer.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BaseUtils {
	public static ArrayList<Map<String, Object>> setDrawerItems(Context context) {
		ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
		String[] mDrawerItems = context.getResources().getStringArray(
				R.array.drawer_item_array);
		/**
		 * 设备管理
		 */
		Map<String, Object> deviceItem = new HashMap<String, Object>();
//		deviceItem.put("icon", R.drawable.device_menu);
		deviceItem.put("item", mDrawerItems[0]);
		mData.add(deviceItem);

		/**
		 * 用户信息
		 */
		Map<String, Object> userinfoItem = new HashMap<String, Object>();
//		userinfoItem.put("icon", R.drawable.userinfo);
		userinfoItem.put("item", mDrawerItems[1]);
		mData.add(userinfoItem);

		/**
		 * 设置
		 */
		Map<String, Object> settingItem = new HashMap<String, Object>();
//		settingItem.put("icon", R.drawable.setting);
		settingItem.put("item", mDrawerItems[2]);
		mData.add(settingItem);

		/**
		 * 帮助
		 */
		Map<String, Object> helpItem = new HashMap<String, Object>();
//		helpItem.put("icon", R.drawable.help);
		helpItem.put("item", mDrawerItems[3]);
		mData.add(helpItem);

		/**
		 * 退出登录
		 */
		Map<String, Object> exitItem = new HashMap<String, Object>();
//		exitItem.put("icon", R.drawable.exit);
		exitItem.put("item", mDrawerItems[4]);
		mData.add(exitItem);

		/**
		 * 关于
		 */
		Map<String, Object> aboutItem = new HashMap<String, Object>();
//		aboutItem.put("icon", R.drawable.about);
		aboutItem.put("item", mDrawerItems[5]);
		mData.add(aboutItem);
		return mData;
	}

	public static void activityJumping(Context context, Class<?> cls) {
		Intent it = new Intent();
		it.setClass(context, cls);
		it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it);
		((Activity) context).overridePendingTransition(
				R.anim.slide_left_in, R.anim.slide_right_out);
		((Activity) context).finish();
	}

	
//	public static void exitDialog(final Context context) {
//		new AlertDialog.Builder(context)
//				.setTitle("确认")
//				.setMessage("确定退出登录吗？")
//				.setPositiveButton("是", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        // TODO Auto-generated method stub
//                        SharedPreferences sharedPreferences = context
//                                .getSharedPreferences("user_info", 0);
//                        sharedPreferences.edit().putBoolean("exit", true).commit();
//                        Intent intent = new Intent();
//                        intent.setClass(context, LoginActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(intent);
//                        ((Activity) context).overridePendingTransition(
//                                R.anim.slide_left_in, R.anim.slide_right_out);
//                        ((Activity) context).finish();
//                    }
//                })
//				.setNegativeButton("否", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        // TODO Auto-generated method stub
//                        arg0.dismiss();
//                    }
//
//                }).show();
//	}

	public static void helpDialog(final Context context) {
//		new AlertDialog.Builder(context).setTitle("帮助")
//				.setMessage(context.getResources().getString(R.string.help))
//				.setNeutralButton("确定", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						arg0.dismiss();
//					}
//				}).show();
	}

	public static void aboutDialog(final Context context) {
//		new AlertDialog.Builder(context).setTitle("关于")
//				.setMessage(context.getResources().getString(R.string.about))
//				.setNeutralButton("确定", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						arg0.dismiss();
//					}
//				}).show();
	}

	public static void settingDialog(final Context context) {
		new AlertDialog.Builder(context).setTitle("版本更新")
				.setMessage("当前为最新版本 v1.0.0")
				.setNeutralButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						arg0.dismiss();
					}
				}).show();
	}

//	public static void userDialog(final Context context) {
//		View view = ((Activity) context).getLayoutInflater().inflate(
//				R.layout.login_log_dialog, null);
//		Dialog dialog = new Dialog(context);
//		dialog.setContentView(view);
//		dialog.setTitle("用户登录日志");
//		dialog.show();
//		ListView listview = (ListView) view.findViewById(R.id.log_list);
//		LogInfo.setLogInfo(context, listview);
//	}

	public static String getTime() {
		Calendar c = Calendar.getInstance();

		/*
		 * Time time = new Time("GMT+8"); time.setToNow(); int year = time.year;
		 * int month = time.month; int day = time.monthDay; int minute =
		 * time.minute; int hour = time.hour; int sec = time.second;
		 */

		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		return year + "-" + month + "-" + day + "    " + hour + ":" + minute
				+ ":" + sec;
	}

	public static void insertLog(Context context, String username,
                                 String loginTime) {
		SQLiteDatabase db = context.openOrCreateDatabase("userinfo.db",
				Context.MODE_PRIVATE, null);
		// db.execSQL("DROP TABLE IF EXISTS loginLog");
		db.execSQL("CREATE TABLE IF NOT EXISTS loginLog (_id INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR, logintime VARCHAR)");
		db.execSQL("INSERT INTO loginLog (username, logintime) VALUES ('"
				+ username + "','" + loginTime + "')");
		db.close();
	}
	
	
	/*public static String splitChannel(int channel){
		String mChannel = "";
		String sChannel = Integer.toBinaryString(channel);
		for(int i=0;i < sChannel.length(); i++){
			if(Integer.parseInt(sChannel.charAt(i)+"") == 1){
				if(mChannel.equals(""))
					mChannel += "," + (i+1);
				else
					mChannel += "," + (i+1); 
			}
		}
		return mChannel;
	}
	*/

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

	public static String JSONTokener(String in) {
		if (in != null && in.startsWith("\ufeff")) {
			in = in.substring(1);
		}
		return in;
	} 
	
}
