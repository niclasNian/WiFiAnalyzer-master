package com.vrem.wifianalyzer.wifi.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.adapter.DeviceListAdapter;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo extends Base {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5515271584326125151L;
	private String devId;
	private String commit;
	private int devType;
	private int workType;
	private String stLasttime;
	private boolean isAlive;
	private String params;
	private static DeviceListAdapter deviceListAdapter;

	public String getDevId() {
		return devId;
	}

	public void setDevId(String string) {
		this.devId = string;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public int getDevType() {
		return devType;
	}

	public void setDevType(int devType) {
		this.devType = devType;
	}

	public int getWorkType() {
		return workType;
	}

	public void setWorkType(int workType) {
		this.workType = workType;
	}

	public String getStLasttime() {
		return stLasttime;
	}

	public void setStLasttime(String stLasttime) {
		this.stLasttime = stLasttime;
	}
	
	public boolean getAlive(){
		return isAlive;
	}
	
	public void setAlive(boolean isAlive){
		this.isAlive = isAlive;
	}
	
	public String getParams(){
		return params;
	}
	
	public void setParams(String params){
		this.params = params;
	}

	public static int handlingToWorkType(String handling) {
		if (handling.equals("")) {
			return 100; // 空闲
		}
		else if (handling.equals("handshake")) {
			return 103; // 密码强度测试 - 抓包
		}
		else if (handling.equals("wps_crack")){
			return 104;
		}
		else if (handling.equals("fakeap_4g")) {
			return 105;
		}
		else if (handling.equals("fakeap_wifi")) {
			return 201;
		}
		else if (handling.equals("dos_ap")) {
			return 101;
		}
		else if (handling.equals("dos_single")) {
			return 102;
		}
		else if (handling.equals("dos_multi")) {
			return 110;
		}
		else return 2017;
	}

	public static void setDeviceInfo(final Context context,final ListView mainContainer, final ProgressBar progressBar,
									 final TextView refresh, final TextView noData, final RelativeLayout bottomLayout) throws JSONException {
		final List<DeviceInfo> deviceData = new ArrayList<DeviceInfo>();

		progressBar.setVisibility(View.VISIBLE);
		DeviceListActivity.flag = 0;
		((Activity) context).invalidateOptionsMenu();
		mainContainer.setVisibility(View.GONE);

//		Intent intent = ((Activity)context).getIntent();
		String devID = PrefSingleton.getInstance().getString("device");
		DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
		devStatusDBUtils.open();
		devStatusDBUtils.tryInsertNewDev(devID);
		String handling = devStatusDBUtils.getHandling(devID);
		devStatusDBUtils.close();
		int workType = handlingToWorkType(handling);

		JSONObject response = new JSONObject("{\"devices\":[{\"status\":\"{}\",\"work_type\":" + new Integer(workType).toString() + ",\"uid\":1,\"dev_type\":51,\"devid\":\"" + devID + "\",\"st_lasttime\":1627174220,\"commit\":\"" + devID + "\",\"is_alive\":true,\"cmd_param\":{}}],\"error\":0}");

		int length = response.getJSONArray("devices")
				.length();
		for (int i = 0; i < length; i++) {
			DeviceInfo deviceInfo = new DeviceInfo();
			deviceInfo.setDevId(((JSONObject) response.getJSONArray("devices").get(i)).getString("devid"));
			deviceInfo.setCommit(((JSONObject) response
					.getJSONArray("devices").get(i))
					.getString("commit"));
			deviceInfo.setDevType(((JSONObject) response
					.getJSONArray("devices").get(i))
					.getInt("dev_type"));
			deviceInfo.setWorkType(((JSONObject) response
					.getJSONArray("devices").get(i))
					.getInt("work_type"));
			deviceInfo.setStLasttime(((JSONObject) response
					.getJSONArray("devices").get(i))
					.getString("st_lasttime"));
			deviceInfo.setAlive(((JSONObject) response
					.getJSONArray("devices").get(i))
					.getBoolean("is_alive"));
			deviceInfo.setParams(((JSONObject) response
					.getJSONArray("devices").get(i))
					.getJSONObject("cmd_param").toString());
			deviceData.add(deviceInfo);
		}

		progressBar.setVisibility(View.GONE);
		mainContainer.setVisibility(View.VISIBLE);
		DeviceListActivity.flag = 1;
		((Activity) context).invalidateOptionsMenu();
		if(deviceData.size() == 0){
			noData.setVisibility(View.VISIBLE);
			refresh.setVisibility(View.GONE);
		}else{
			noData.setVisibility(View.GONE);
			refresh.setVisibility(View.GONE);
			deviceListAdapter = new DeviceListAdapter(context,deviceData, R.layout.device_listitem, mainContainer, progressBar, refresh, noData, bottomLayout);
			mainContainer.setAdapter(deviceListAdapter);
		}
	}

	public static void setDeviceInfo_bak(final Context context,
                                         final ListView mainContainer, final ProgressBar progressBar, final TextView refresh, final TextView noData, final RelativeLayout bottomLayout) throws JSONException {
		progressBar.setVisibility(View.VISIBLE);
		DeviceListActivity.flag = 0;
		((Activity) context).invalidateOptionsMenu();
		mainContainer.setVisibility(View.GONE);
		SharedPreferences userInfo = context.getSharedPreferences("user_info",
				0);
		String token = userInfo.getString("token", "");
		String username = userInfo.getString("username", "");
		String ip = userInfo.getString("ip", "");
		final List<DeviceInfo> deviceData = new ArrayList<DeviceInfo>();
		JSONObject obj = new JSONObject();
		obj.put("username", username);
		obj.put("token", token);
		String url = "http://" + ip + "/mobi_api/v1/getdevices";
		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.POST, url, obj,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						// display response
						try {
							int length = response.getJSONArray("devices")
									.length();
							for (int i = 0; i < length; i++) {
								DeviceInfo deviceInfo = new DeviceInfo();
								deviceInfo.setDevId(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getString("devid"));
								deviceInfo.setCommit(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getString("commit"));
								deviceInfo.setDevType(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getInt("dev_type"));
								deviceInfo.setWorkType(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getInt("work_type"));
								deviceInfo.setStLasttime(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getString("st_lasttime"));
								deviceInfo.setAlive(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getBoolean("is_alive"));
								deviceInfo.setParams(((JSONObject) response
										.getJSONArray("devices").get(i))
										.getJSONObject("cmd_param").toString());
								deviceData.add(deviceInfo);
							}
							
							progressBar.setVisibility(View.GONE);
							mainContainer.setVisibility(View.VISIBLE);
							DeviceListActivity.flag = 1;
							((Activity) context).invalidateOptionsMenu();
							if(deviceData.size() == 0){
								noData.setVisibility(View.VISIBLE);
								refresh.setVisibility(View.GONE);
							}else{
								noData.setVisibility(View.GONE);
								refresh.setVisibility(View.GONE);
							deviceListAdapter = new DeviceListAdapter(context,
									deviceData, R.layout.device_listitem, mainContainer, progressBar, refresh, noData, bottomLayout);
							mainContainer.setAdapter(deviceListAdapter);
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(context, "通讯错误，请重试", Toast.LENGTH_SHORT)
						.show();
						progressBar.setVisibility(View.GONE);
						DeviceListActivity.flag = 1;
						((Activity) context).invalidateOptionsMenu();
						refresh.setVisibility(View.VISIBLE);
						noData.setVisibility(View.GONE);
					}
				});

		// add it to the RequestQueue
		getRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		if(VolleySingleton.getInstance(context).getRequestQueue() != null)
			VolleySingleton.getInstance(context).getRequestQueue().add(getRequest);
	}

	public static void setCommit(final Context context, String devId, String commit, final ListView listview, final ProgressBar progressBar, final TextView refresh, final TextView noData, final RelativeLayout bottomLayout) throws JSONException {
		SharedPreferences userInfo = context.getSharedPreferences(
						"user_info", 0);
		String token = userInfo.getString(
				"token", "");
		String username = userInfo.getString(
				"username", "");
		String ip = userInfo.getString(
				"ip", "");
		JSONObject obj = new JSONObject();
		obj.put("username", username);
		obj.put("token", token);
		obj.put("devid", devId);
		obj.put("commit", commit);
		String url = "http://" + ip + "/mobi_api/v1/setcommit";
		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.POST, url, obj,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT)
								.show();
						try {
							setDeviceInfo(context, listview, progressBar, refresh, noData, bottomLayout);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(context, "设置失败", Toast.LENGTH_SHORT)
								.show();
					}
				});
		getRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		// add it to the RequestQueue
		if(VolleySingleton.getInstance(context).getRequestQueue() != null)
			VolleySingleton.getInstance(context).getRequestQueue().add(getRequest);
	}

	public static void sendCommand(final Context context,
			final DeviceInfo deviceInfo, final JSONObject params,
			final String command) throws JSONException {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"user_info", 0);
		String token = sharedPreferences.getString("token", "");
		String username = sharedPreferences.getString("username", "");
		String ip = sharedPreferences.getString("ip", "");
		String url = "http://" + ip + "/mobi_api/v1/runcommand";
		JSONObject obj = new JSONObject();

		obj.put("username", username);
		obj.put("token", token);
		obj.put("devid", deviceInfo.getDevId());
		obj.put("command", command);
		if (command.equals("monitor"))
			obj.put("params", "");
		else
			obj.put("params", params);
		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.POST, url, obj,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						// display response
						int errorCode = 1;
						try {
							errorCode = response.getInt("error");

						} catch (JSONException e) {
							e.printStackTrace();
						}
						if (errorCode == 0) {
							if (command.equals("wifi_dos_channel") || command.equals("wifi_dos_ssid")) {
								Intent intent = new Intent();
								intent.setClass(context,
										DeviceListActivity.class);
								intent.putExtra("deviceinfo", deviceInfo);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								context.startActivity(intent);	
								((Activity) context).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
								((Activity) context).finish();
							}else if(command.equals("wifi_sniffer")) {
								Intent intent = new Intent();
								intent.setClass(context,DeviceListActivity.class);
								intent.putExtra("deviceinfo", deviceInfo);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								context.startActivity(intent);
								((Activity) context).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
								((Activity) context).finish();
							}else if(command.equals("wifi_wps")) {
								Intent intent = new Intent();
								intent.setClass(context,DeviceListActivity.class);
								intent.putExtra("deviceinfo", deviceInfo);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								context.startActivity(intent);
								((Activity) context).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
								((Activity) context).finish();
							}else if(command.equals("wifi_fake_ap")) {
								Intent intent = new Intent();
								intent.setClass(context,DeviceListActivity.class);
								intent.putExtra("deviceinfo", deviceInfo);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								context.startActivity(intent);
								((Activity) context).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
								((Activity) context).finish();
							}else if(command.equals("connect_wifi_and_fake")) {
                                Intent intent = new Intent();
                                intent.setClass(context,DeviceListActivity.class);
                                intent.putExtra("deviceinfo", deviceInfo);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                                ((Activity) context).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
                                ((Activity) context).finish();
                            }
						} else {
							Toast.makeText(context, "状态错误，请重试", Toast.LENGTH_SHORT)
									.show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(context, "通讯错误，请重试", Toast.LENGTH_SHORT)
						.show();
					}
				});
		getRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		// add it to the RequestQueue
		if(VolleySingleton.getInstance(context).getRequestQueue() != null)
			VolleySingleton.getInstance(context).getRequestQueue().add(getRequest);
	}
	
	public static void sendCommand(final Context context,
                                   final DeviceInfo deviceInfo, final JSONObject params,
                                   final String command, final ListView listview, final ProgressBar progressBar, final TextView refresh, final TextView noData, final RelativeLayout bottomLayout) throws JSONException {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"user_info", 0);
		String token = sharedPreferences.getString("token", "");
		String username = sharedPreferences.getString("username", "");
		String ip = sharedPreferences.getString("ip", "");
		String url = "http://" + ip + "/mobi_api/v1/runcommand";
		JSONObject obj = new JSONObject();

		obj.put("username", username);
		obj.put("token", token);
		obj.put("devid", deviceInfo.getDevId());
		obj.put("command", command);
		if (command.equals("monitor"))
			obj.put("params", "");
		else
			obj.put("params", params);

		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.POST, url, obj,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						// display response
						int errorCode = 1;
						try {
							errorCode = response.getInt("error");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (errorCode == 0) {
							if(command.equals("monitor")) {
								try {
									DeviceInfo.setDeviceInfo(context, listview, progressBar, refresh, noData, bottomLayout);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Toast.makeText(context, "停止成功", Toast.LENGTH_SHORT)
								.show();
							}
						} else {
							Toast.makeText(context, "状态错误，请重试", Toast.LENGTH_SHORT)
									.show();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(context, "通讯错误，请重试", Toast.LENGTH_LONG)
						.show();
					}
				});
		getRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		// add it to the RequestQueue
		if(VolleySingleton.getInstance(context).getRequestQueue() != null)
			VolleySingleton.getInstance(context).getRequestQueue().add(getRequest);
	}
	
	
	public static void updateView(){
		deviceListAdapter.notifyDataSetChanged();
	}

}
