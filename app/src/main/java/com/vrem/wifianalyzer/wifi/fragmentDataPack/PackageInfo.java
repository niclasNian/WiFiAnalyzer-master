package com.vrem.wifianalyzer.wifi.fragmentDataPack;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.SnifferFile;
import com.vrem.wifianalyzer.wifi.common.SnifferFilesDBUtils;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;
import com.vrem.wifianalyzer.wifi.model.Base;
import com.vrem.wifianalyzer.wifi.model.DeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackageInfo extends Base {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1228817297387905427L;
	private String ssid;
	private String mac;
	private String downloadUrl;
	private String id;
	private static PackageDownloadAdapter packageDownloadAdapter;
	
	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}
	
	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static void setPackageInfo(final Context context, String deviceId,
                                      final ListView listview, final ProgressBar progressBar, final TextView refresh, final TextView noData) throws JSONException {
		progressBar.setVisibility(View.VISIBLE);

		final List<PackageInfo> packageData = new ArrayList<PackageInfo>();
		JSONObject obj = new JSONObject();

		SnifferFilesDBUtils snifferFilesDBUtils = new SnifferFilesDBUtils(context);
		snifferFilesDBUtils.open();
		Map<Integer,SnifferFile> map = snifferFilesDBUtils.getSnifferFiles(deviceId);
		snifferFilesDBUtils.close();

		for (Integer i: map.keySet()) {
			SnifferFile snifferFile = map.get(i);
			PackageInfo packageInfo = new PackageInfo();
			packageInfo.setDownloadUrl(PrefSingleton.getInstance().getString("url").replace("http://", "") + "/" + snifferFile.file);

			packageInfo.setId(i.toString());
			packageInfo.setMac(snifferFile.file.split("-")[1]);//snifferFile.file.split("-")[1] + "-" + snifferFile.file.split("-")[2] + ".cap");//snifferFile.file.split("-")[1].split("\\.")[0]);

			packageInfo.setSsid(snifferFile.essid);

			packageData.add(packageInfo);
		}

		progressBar.setVisibility(View.GONE);
		if(packageData.size() == 0){
			noData.setVisibility(View.VISIBLE);
			refresh.setVisibility(View.GONE);
		}else{
			noData.setVisibility(View.GONE);
			refresh.setVisibility(View.GONE);
			packageDownloadAdapter = new PackageDownloadAdapter(context,
					packageData, R.layout.data_pack_listitem,  progressBar);
			listview.setAdapter(packageDownloadAdapter);
		}
	}
	
	public static void setPackageInfo_bak(final Context context, DeviceInfo deviceInfo,
                                          final ListView listview, final ProgressBar progressBar, final TextView refresh, final TextView noData) throws JSONException {
		progressBar.setVisibility(View.VISIBLE);
		SharedPreferences userInfo = context.getSharedPreferences("user_info",
				0);
		String token = userInfo.getString("token", "");
		String username = userInfo.getString("username", "");
		String ip = userInfo.getString("ip", "");
		final List<PackageInfo> packageData = new ArrayList<PackageInfo>();
		JSONObject obj = new JSONObject();
		obj.put("devid", deviceInfo.getDevId());
		obj.put("token", token);
		String url = "http://" + ip + "/mobi_api/v1/snifffile";
		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.POST, url, obj,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						// display response

						try {
							int length = response.getJSONArray("files")
									.length();
							for (int i = 0; i < length; i++) {
								PackageInfo packageInfo = new PackageInfo();
								packageInfo.setDownloadUrl(((JSONObject) response
										.getJSONArray("files").get(i))
										.getString("url"));
								
								packageInfo.setId(((JSONObject) response
										.getJSONArray("files").get(i))
										.getString("id"));
								packageInfo.setMac(((JSONObject) response
										.getJSONArray("files").get(i))
										.getString("bssid"));
								
								packageInfo.setSsid(((JSONObject) response
										.getJSONArray("files").get(i))
										.getString("ssid"));
								
								packageData.add(packageInfo);
							}
							
							progressBar.setVisibility(View.GONE);
							if(packageData.size() == 0){
								noData.setVisibility(View.VISIBLE);
								refresh.setVisibility(View.GONE);
							}else{
								noData.setVisibility(View.GONE);
								refresh.setVisibility(View.GONE);
							packageDownloadAdapter = new PackageDownloadAdapter(context,packageData, R.layout.data_pack_listitem,  progressBar);
							listview.setAdapter(packageDownloadAdapter);
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
						refresh.setVisibility(View.VISIBLE);
						noData.setVisibility(View.GONE);
					}
				});

		// add it to the RequestQueue
		if(VolleySingleton.getInstance(context).getRequestQueue() != null)
			VolleySingleton.getInstance(context).getRequestQueue().add(getRequest);
	}
}
