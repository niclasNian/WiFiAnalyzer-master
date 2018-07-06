/*
 * WiFiAnalyzer
 * Copyright (C) 2018  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.adapter.APDialogListAdapter;
import com.vrem.wifianalyzer.wifi.band.WiFiWidth;
import com.vrem.wifianalyzer.wifi.common.BaseUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//单条wifi详细信息
public class WiFiDetail implements Comparable<WiFiDetail> {
    public static final WiFiDetail EMPTY = new WiFiDetail(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, WiFiSignal.EMPTY, "", "","",0);
    private static final String SSID_EMPTY = "***";//用于返回空SSID的wifiSSID信息

    private final List<WiFiDetail> children;//单项wifi信息的集合
    private final String SSID; //wifiSSID
    private final String BSSID;//mac地址
    private final String capabilities;//加密方式
    private final WiFiSignal wiFiSignal;//wifi信号
    private final WiFiAdditional wiFiAdditional;//wifi中心频率，主频率，水平，频段、MHz差、信道

    private final String client;//客户端mac
    private final String cipher; //算法
    private final String wps;
    private final double rate;

    private static APDialogListAdapter apDialogListAdapter;

    public WiFiDetail(@NonNull String SSID, @NonNull String BSSID, @NonNull String capabilities,
                      @NonNull WiFiSignal wiFiSignal, @NonNull WiFiAdditional wiFiAdditional,String client,String cipher,String wps,double rate) {
        if (SSID.equals("")){
            this.SSID = "隐藏SSID";
        }else {
            this.SSID = SSID;
        }
        this.BSSID = BSSID;
        this.capabilities = capabilities;
        this.wiFiSignal = wiFiSignal;
        this.wiFiAdditional = wiFiAdditional;
        this.children = new ArrayList<>();

        this.client = client;
        this.cipher = cipher;
        this.wps = wps;
        this.rate = rate;
    }

    //接收下面WiFiDetail构造方法回传的数据
    public WiFiDetail(@NonNull String SSID, @NonNull String BSSID, @NonNull String capabilities, @NonNull WiFiSignal wiFiSignal, String client, String cipher,String wps,double rate) {
        this(SSID, BSSID, capabilities, wiFiSignal, WiFiAdditional.EMPTY,client,cipher,wps,rate);
    }

    //定义构造方法，回传给上面的WiFiDetail构造方法
    public WiFiDetail(@NonNull WiFiDetail wiFiDetail, @NonNull WiFiAdditional wiFiAdditional,String client, String cipher,String wps,double rate) {
        this(wiFiDetail.SSID, wiFiDetail.BSSID, wiFiDetail.getCapabilities(), wiFiDetail.getWiFiSignal(), wiFiAdditional,client,cipher,wps,rate);
    }

    public double getRate() {
        return rate;
    }
    //返回wifi加密方式
    public Security getSecurity() {
        return Security.findOne(capabilities);
    }

    public String getSSID() {
        return isHidden() ? SSID_EMPTY : SSID;//为true则返回SSID_EMPTY 反之
    }

    boolean isHidden() {
        return StringUtils.isBlank(SSID);//判断SSID是否为空，为空返回true 反之
    }

    public String getWps(){
        return wps;
    }

    public String getBSSID() {
        return BSSID;
    }
    public String getClient(){
        return client;
    }
    public String getCipher(){
        return cipher;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public WiFiSignal getWiFiSignal() {
        return wiFiSignal;
    }

    public WiFiAdditional getWiFiAdditional() {
        return wiFiAdditional;
    }

    public List<WiFiDetail> getChildren() {
        return children;
    }

    public boolean noChildren() {
        return !getChildren().isEmpty();
    }

    //返回SSID BSSID
    public String getTitle() {
        return String.format("%s (%s)", getSSID(), BSSID);
    }

    //添加单条wifi数据到list集合中
    public void addChild(@NonNull WiFiDetail wiFiDetail) {
        children.add(wiFiDetail);
    }

    /**
     * 重写object的equals、hashCode、compareTo、toString方法 对数据属性做相应的封装
     * */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WiFiDetail that = (WiFiDetail) o;

        return new EqualsBuilder()
            .append(getSSID(), that.getSSID())
            .append(getBSSID(), that.getBSSID())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getSSID())
            .append(getBSSID())
            .toHashCode();
    }

    @Override
    public int compareTo(@NonNull WiFiDetail another) {
        return new CompareToBuilder()
            .append(getSSID(), another.getSSID())
            .append(getBSSID(), another.getBSSID())
            .toComparison();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static void setAPInfo(final Context context,
                                 final ListView listview, String devId, final int tag,
                                 final ProgressBar progressBar, final int mainPage, final int sort,
                                 final TextView refresh, final TextView noData,
                                 final boolean isDialog) throws JSONException {
        final List<WiFiDetail> apData = new ArrayList<>();

//        if (mainPage == 1) {
//            ScanActivity.flag = 0;
//            ((Activity) context).invalidateOptionsMenu();
//        }
        progressBar.setVisibility(View.VISIBLE);
        listview.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "user_info", 0);
        String token = sharedPreferences.getString("token", "");
        String username = sharedPreferences.getString("username", "");
        String ip = sharedPreferences.getString("ip", "");
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("token", token);
        obj.put("devid", devId);
        String url = "http://" + ip + "/mobi_api/v1/deviceinfo";
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.POST, url, obj,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response_tmp) {
                        // display response
                        JSONObject jo = null;
                        try {
                            JSONObject response = new JSONObject("{\"action\":\"action\",\"status\":0,\"data\":{\"aps\":{\"5a:fb:84:b4:9a:8d\":{\"wpa_cipher\":[],\"sta_bssids\":[\"10:2a:b3:dd:3d:98\"],\"bssid\":\"5a:fb:84:b4:9a:8d\",\"enc\":[\"WPA2\"],\"channel\":1,\"last_time\":832.841157,\"wps\":\"configured\",\"essid\":\"Lab13\",\"first_time\":832.221925,\"basic_rates\":[24,12,6],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[\"PSK\"],\"wpa2_cipher\":[\"CCMP\"],\"tx_datas\":0,\"power\":-60,\"beacons\":7},\"f0:b4:29:6a:ca:d9\":{\"wpa_cipher\":[],\"sta_bssids\":[\"10:2a:b3:dd:3d:98\"],\"bssid\":\"f0:b4:29:6a:ca:d9\",\"enc\":[\"WPA2\"],\"channel\":1,\"last_time\":832.853479,\"wps\":\"configured\",\"essid\":\"Lab_BCMI_233\",\"first_time\":832.322223,\"basic_rates\":[1,2,11,5.5],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[36,6,9,12,48,18,54,24],\"wpa2_auth\":[\"PSK\"],\"wpa2_cipher\":[\"TKIP\",\"CCMP\"],\"tx_datas\":0,\"power\":-78,\"beacons\":6},\"a8:bd:27:24:e4:a4\":{\"wpa_cipher\":[],\"sta_bssids\":[],\"bssid\":\"a8:bd:27:24:e4:a4\",\"enc\":[\"WPA2\"],\"channel\":1,\"last_time\":832.800368,\"wps\":null,\"essid\":\"eduroam\",\"first_time\":832.288364,\"basic_rates\":[24,12,6],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[\"MGT\"],\"wpa2_cipher\":[\"CCMP\"],\"tx_datas\":0,\"power\":-80,\"beacons\":4},\"7c:dd:90:e0:00:e6\":{\"wpa_cipher\":[],\"sta_bssids\":[\"10:2a:b3:dd:3d:98\"],\"bssid\":\"7c:dd:90:e0:00:e6\",\"enc\":[\"WPA2\"],\"channel\":1,\"last_time\":832.891639,\"wps\":null,\"essid\":\"default_e0_00_e6\",\"first_time\":832.195933,\"basic_rates\":[1,2,11,5.5],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[36,6,9,12,48,18,54,24],\"wpa2_auth\":[\"PSK\"],\"wpa2_cipher\":[\"CCMP\"],\"tx_datas\":0,\"power\":-28,\"beacons\":8},\"a8:bd:27:24:e4:a0\":{\"wpa_cipher\":[],\"sta_bssids\":[],\"bssid\":\"a8:bd:27:24:e4:a0\",\"enc\":[\"OPEN\"],\"channel\":1,\"last_time\":832.79931,\"wps\":null,\"essid\":\"SJTU-Web\",\"first_time\":832.184792,\"basic_rates\":[24,12,6],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[],\"wpa2_cipher\":[],\"tx_datas\":0,\"power\":-80,\"beacons\":7},\"a8:bd:27:24:e4:a5\":{\"wpa_cipher\":[],\"sta_bssids\":[\"70:56:81:a0:e5:21\",\"f0:b4:29:9e:68:88\"],\"bssid\":\"a8:bd:27:24:e4:a5\",\"enc\":[\"WEP\"],\"channel\":1,\"last_time\":832.894687,\"wps\":null,\"essid\":\"SJTU\",\"first_time\":832.186203,\"basic_rates\":[24,12,6],\"rx_datas\":1,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[\"PSK\"],\"wpa2_cipher\":[\"CCMP\"],\"tx_datas\":1,\"power\":-78,\"beacons\":4},\"a8:bd:27:24:e4:a3\":{\"wpa_cipher\":[],\"sta_bssids\":[\"10:2a:b3:dd:3d:98\"],\"bssid\":\"a8:bd:27:24:e4:a3\",\"enc\":[\"OPEN\"],\"channel\":1,\"last_time\":832.841411,\"wps\":null,\"essid\":\"ChinaUnicom\",\"first_time\":832.185583,\"basic_rates\":[24,12,6],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[],\"wpa2_cipher\":[],\"tx_datas\":0,\"power\":-80,\"beacons\":4},\"a8:bd:27:24:e4:a1\":{\"wpa_cipher\":[],\"sta_bssids\":[],\"bssid\":\"a8:bd:27:24:e4:a1\",\"enc\":[\"OPEN\"],\"channel\":1,\"last_time\":832.799516,\"wps\":null,\"essid\":\"CMCC\",\"first_time\":832.287529,\"basic_rates\":[24,12,6],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[],\"wpa2_cipher\":[],\"tx_datas\":0,\"power\":-78,\"beacons\":5},\"a8:bd:27:24:e4:a2\":{\"wpa_cipher\":[],\"sta_bssids\":[],\"bssid\":\"a8:bd:27:24:e4:a2\",\"enc\":[\"OPEN\"],\"channel\":1,\"last_time\":832.799963,\"wps\":null,\"essid\":\"CMCC-EDU\",\"first_time\":832.799963,\"basic_rates\":[24,12,6],\"rx_datas\":0,\"wpa_auth\":[],\"extra_rates\":[48,9,18,36,54],\"wpa2_auth\":[],\"wpa2_cipher\":[],\"tx_datas\":0,\"power\":-74,\"beacons\":1}},\"stas\":{\"70:56:81:a0:e5:21\":{\"ap_bssid\":\"a8:bd:27:24:e4:a5\",\"rate_from\":null,\"probes\":[],\"first_time\":832.701825,\"rx_datas\":0,\"last_time\":832.701825,\"channel\":1,\"bssid\":\"70:56:81:a0:e5:21\",\"tx_datas\":1,\"rate_to\":6,\"power\":-78},\"f0:b4:29:9e:68:88\":{\"ap_bssid\":null,\"rate_from\":6,\"probes\":[],\"first_time\":832.894061,\"rx_datas\":0,\"last_time\":832.894687,\"channel\":1,\"bssid\":\"f0:b4:29:9e:68:88\",\"tx_datas\":0,\"rate_to\":6,\"power\":-80},\"10:2a:b3:dd:3d:98\":{\"ap_bssid\":null,\"rate_from\":1,\"probes\":[],\"first_time\":832.816341,\"rx_datas\":0,\"last_time\":832.853479,\"channel\":1,\"bssid\":\"10:2a:b3:dd:3d:98\",\"tx_datas\":0,\"rate_to\":1,\"power\":-66}}},\"id\":1}");
                            jo = new JSONObject(BaseUtils.JSONTokener(response.getString("data")));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            JSONObject jsonObjectAps = jo.getJSONObject("aps");
                            Iterator<?> keys = jsonObjectAps.keys();
                            String essid = null,enc = null,wps = null, bssid = null,channel = null,client = null;
                            double cRate = 0;
                            int power = 0;
                            while (keys.hasNext()) {
                                String key = (String)keys.next();
                                if (key.equals("")) continue;
                                JSONObject jsonObjectAp = jsonObjectAps.getJSONObject(key);
                                if (tag == 1 && jsonObjectAp.get("wps") == null) continue;
                                WiFiDetail apInfo = null;
                                essid = jsonObjectAp.getString("essid");
                                power = jsonObjectAp.getInt("power");
                                enc = jsonObjectAp.getJSONArray("enc").getString(0);
                                wps = jsonObjectAp.get("wps") == null ? "false" : "true";
                                bssid = key;
                                String cipher = "";
                                JSONArray wpa_cipher = jsonObjectAp.getJSONArray("wpa_cipher");
                                JSONArray wpa2_cipher = jsonObjectAp.getJSONArray("wpa2_cipher");
                                for (int i = 0; i < wpa_cipher.length(); i++) {
                                    if (cipher.equals("")) {
                                        cipher = cipher + wpa_cipher.getString(i);
                                    }
                                    else {
                                        cipher = cipher + " " + wpa_cipher.getString(i);
                                    }
                                }
                                for (int i = 0; i < wpa2_cipher.length(); i++) {
                                    if (cipher.equals("")) {
                                        cipher = cipher + wpa2_cipher.getString(i);
                                    }
                                    else {
                                        cipher = cipher + " " + wpa2_cipher.getString(i);
                                    }
                                }
//                                apInfo.setCipher(cipher);
//                                apInfo.setClient("[]");
                                client = "[]";
//                                apInfo.setChannel(jsonObjectAp.getInt("channel"));
                                channel = String.valueOf(jsonObjectAp.getInt("channel"));

                                JSONArray basic_rates = jsonObjectAp.getJSONArray("basic_rates");//获取基础速率组
                                JSONArray extra_rates = jsonObjectAp.getJSONArray("extra_rates");//获取额外速率组
                                double minRate = -1;
                                for (int i = 0; i < basic_rates.length(); i++) {
                                    if (basic_rates.getDouble(i) < minRate || minRate < 0) {
                                        minRate = basic_rates.getDouble(i);
                                    }
                                }
                                if (basic_rates.length() == 0) {
                                    for (int i = 0; i < extra_rates.length(); i++) {
                                        if (extra_rates.getDouble(i) < minRate || minRate < 0) {
                                            minRate = extra_rates.getDouble(i);
                                        }
                                    }
                                }
                                cRate = minRate;

                                WiFiWidth wiFiWidth =WiFiWidth.MHZ_40;//模拟wifi宽度
                                WiFiSignal addWiFiSignal = new WiFiSignal(1,2,wiFiWidth,power,channel);//模拟wifi信号 13：power dbm,
                                WiFiDetail addWiFiDetail = new WiFiDetail(essid,bssid,enc,addWiFiSignal,client,cipher,wps,cRate);//模拟单条wifi的基本信息 1111:essid  fds:bssid  fdss:enc
                                apData.add(addWiFiDetail);
                            }
//                            switch (sort) {
//                                case 0:
//                                    break;
//                                case 1:
//                                    ChannelComparator channelComparator = new ChannelComparator();
//                                    Collections.sort(apData, channelComparator);
//                                    break;
//                                case 2:
//                                    SignalComparator signaleComparator = new SignalComparator();
//                                    Collections.sort(apData, signaleComparator);
//                                    break;
//                                case 3:
//                                    WpsComparator wpsComparator = new WpsComparator();
//                                    Collections.sort(apData, wpsComparator);
//                                    break;
//                                case 4:
//                                    PrivacyComparator privacyComparator = new PrivacyComparator();
//                                    Collections.sort(apData, privacyComparator);
//                                    break;
//                                case 5:
//                                    ClientComparator clientComparator = new ClientComparator();
//                                    Collections.sort(apData, clientComparator);
//                                    break;
//                                default:
//                                    break;
//                            }


                            progressBar.setVisibility(View.GONE);
                            if (apData.size() == 0) {
                                noData.setVisibility(View.VISIBLE);
                                refresh.setVisibility(View.GONE);
                            } else {
                                noData.setVisibility(View.GONE);
                                refresh.setVisibility(View.GONE);
                                listview.setVisibility(View.VISIBLE);
                                if (isDialog) {
                                    apDialogListAdapter = new APDialogListAdapter(context, apData,R.layout.scan_dialog_listitem);
                                    listview.setAdapter(apDialogListAdapter);
                                } else {
//                                    apListAdapter = new APListAdapter(context,apData, R.layout.scan_listitem);
//                                    listview.setAdapter(apListAdapter);
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressBar.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
                Toast.makeText(context, "通讯错误，请重试", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        getRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // add it to the RequestQueue
        if (VolleySingleton.getInstance(context).getRequestQueue() != null)
            VolleySingleton.getInstance(context).getRequestQueue()
                    .add(getRequest);
    }

    public static int scanStep1(final Context context) throws JSONException {
        String url = "http://192.168.100.1:9494";

        JSONObject obj = new JSONObject();
        int gId = PrefSingleton.getInstance().getInt("id");
        PrefSingleton.getInstance().putInt("id", gId + 1);
        obj.put("id", gId); // 1

        JSONArray channels = new JSONArray();
        JSONObject param = new JSONObject();
        param.put("channels", channels); // 2-1
        param.put("interval", 1.5); // 2-2
        param.put("action", "scan"); // 2-3
        obj.put("param", param); // 3

        Log.w("SCAN_STEP_1", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(5 - 1, TimeUnit.SECONDS);
            int status = response.getInt("status");
            if (status == 0) {
                Log.w("成功", "RESPONSE:" + response.toString());
                return 0;
            } else {
                Log.w("失败", "UNEXPECTED RESPONSE: " + response.toString());
                return -2;
            }
        } catch (TimeoutException e) {
            Log.w("超时", "TIMEOUT");
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return -3;
    }

    public static JSONObject scanStep2(Context context) throws JSONException {
        String url = "http://192.168.100.1:9494";//http://192.168.100.1:9494

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "action");
        obj.put("param", param);

        Log.w("SCAN_STEP_2", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();//声明同步的网络请求对象
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);//声明接收对象
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//设置超时和重试请求,第一个代表超时时间,第三个参数代表最大重试次数,这里设置为1.0f代表如果超时，则不重试
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);//把请求加入队列 此时已产生数据

        try {
            JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);//获取请求结果 包含扫描的wifi数据
            int status = response.getInt("status");
            if (status == 0) {
                Log.w("SCAN_STEP_2", "RESPONSE:" + response.toString());
                return response;
            } else {
                Log.w("SCAN_STEP_2", "UNEXPECTED RESPONSE: " + response.toString());
                return null;
            }
        } catch (TimeoutException e) {
            Log.w("超时", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<WiFiDetail> response2ApData(JSONObject response, int tag, int sort) throws JSONException {

        List<WiFiDetail> apData = new ArrayList<WiFiDetail>();
        if (response.getInt("id") == -1) {//表明没有数据
            return null;
        }
        JSONObject jo = new JSONObject(BaseUtils.JSONTokener(response.getString("data")));//获取到data数据
        JSONObject jsonObjectAps = jo.getJSONObject("aps");
        JSONObject jsonObjectStas = jo.getJSONObject("stas");
        Iterator<?> keys = jsonObjectAps.keys();
        String chanel=null,bssid=null,enc=null,essid=null,cipher="",clientTmp=null,wps=null;
        int power=0;
        double cRate=0;
        while (keys.hasNext()) {//遍历所有扫描到的数据
            String key = (String)keys.next();
            if (key.equals("")) continue;
            JSONObject jsonObjectAp = jsonObjectAps.getJSONObject(key);//获取单条wifi数据的所有信息
            if (tag == 1 && jsonObjectAp.get("wps") == null) continue;
            if (jsonObjectAp.get("essid") instanceof String) {
//                apInfo.setSsid(jsonObjectAp.getString("essid"));//设置wifi名称
                essid = jsonObjectAp.getString("essid");
            }
            else {
//                apInfo.setSsid("");
                essid = "";
            }

            int tmp = 0;
            try {
                tmp = jsonObjectAp.getInt("power");//获取功率
            } catch (JSONException e) {
                tmp = 0;
            }
//            apInfo.setPower(tmp);//设置功率
            power = tmp;

            JSONArray ja = jsonObjectAp.getJSONArray("enc");//获取加密方式 可能是多种加密方式，所以为Array
            String privacy = "";
            if (ja.length() > 0) {
                privacy = ja.getString(0);
            }
//            apInfo.setPrivacy(privacy);//设置加密方式
            enc = privacy;
//            apInfo.setWps((jsonObjectAp.get("wps") == null) ? "false" : "true");
//            wps = (jsonObjectAp.get("wps") == null) ? "false" : "true";
            if (jsonObjectAp.get("wps") == null){
                wps = "false";
            }else if(jsonObjectAp.get("wps").equals("configured")) {
                wps = "true";
            }else{
                wps = "false";
            }
//            apInfo.setMac(key);//设置mac地址
            bssid = key;
            JSONArray wpa_cipher = jsonObjectAp.getJSONArray("wpa_cipher");//获取wpa暗号
            JSONArray wpa2_cipher = jsonObjectAp.getJSONArray("wpa2_cipher");//获取wpa2暗号
            for (int i = 0; i < wpa_cipher.length(); i++) {//遍历暗号
                if (cipher.equals("")) {
                    cipher = cipher + wpa_cipher.getString(i);//拼接暗号
                }
                else {
                    cipher = cipher + " " + wpa_cipher.getString(i);//拼接暗号
                }
            }
            for (int i = 0; i < wpa2_cipher.length(); i++) {//遍历暗号
                if (cipher.equals("")) {
                    cipher = cipher + wpa2_cipher.getString(i);//拼接暗号
                }
                else {
                    cipher = cipher + " " + wpa2_cipher.getString(i);//拼接暗号
                }
            }
//            apInfo.setCipher(cipher);//给wifi对象设置暗号

            //apInfo.setClient("[]");
            JSONArray clientBssids = jsonObjectAp.getJSONArray("sta_bssids");
            JSONArray clients = new JSONArray();
            for (int i = 0; i < clientBssids.length(); i++) {//遍历客户端wifi的mac地址
                String clientBssid = clientBssids.getString(i);
                JSONArray probesArr;
                String probes;
                if (jsonObjectStas.has(clientBssid) && jsonObjectStas.getJSONObject(clientBssid).has("probes")) {
                    probes = jsonObjectStas.getJSONObject(clientBssid).getJSONArray("probes").toString();
                }
                else {
                    probes = "";
                }
                if (probes.equals("[]")) {
                    probes = "无";
                }
                probes = probes.replace("[", "").replace("]", "").replace("\"", "");
                JSONObject client = new JSONObject();
                client.put("mac", clientBssid);
                client.put("probe", probes);
                clients.put(client);
            }
            clientTmp = clients.toString();

            chanel = String.valueOf(jsonObjectAp.getInt("channel"));
            JSONArray basic_rates = jsonObjectAp.getJSONArray("basic_rates");//获取基础速率组
            JSONArray extra_rates = jsonObjectAp.getJSONArray("extra_rates");//获取额外速率组
            double minRate = -1;
            for (int i = 0; i < basic_rates.length(); i++) {
                if (basic_rates.getDouble(i) < minRate || minRate < 0) {
                    minRate = basic_rates.getDouble(i);
                }
            }
            if (basic_rates.length() == 0) {
                for (int i = 0; i < extra_rates.length(); i++) {
                    if (extra_rates.getDouble(i) < minRate || minRate < 0) {
                        minRate = extra_rates.getDouble(i);
                    }
                }
            }
            cRate = minRate;
            WiFiWidth wiFiWidth =WiFiWidth.MHZ_40;//模拟wifi宽度
            WiFiSignal addWiFiSignal = new WiFiSignal(1,2,wiFiWidth,power,chanel);//模拟wifi信号 13：power dbm,
            WiFiDetail addWiFiDetail = new WiFiDetail(essid,bssid,enc,addWiFiSignal,clientTmp,cipher,wps,cRate);//模拟单条wifi的基本信息 1111:essid  fds:bssid  fdss:enc
            apData.add(addWiFiDetail);//将单个wifi对象添加到wifi对象组list
        }
//        switch (sort) {
//            case 0:
//                break;
//            case 1:
//                ChannelComparator channelComparator = new ChannelComparator();
//                Collections.sort(apData, channelComparator);
//                break;
//            case 2:
//                SignalComparator signaleComparator = new SignalComparator();
//                Collections.sort(apData, signaleComparator);
//                break;
//            case 3:
//                WpsComparator wpsComparator = new WpsComparator();
//                Collections.sort(apData, wpsComparator);
//                break;
//            case 4:
//                PrivacyComparator privacyComparator = new PrivacyComparator();
//                Collections.sort(apData, privacyComparator);
//                break;
//            case 5:
//                ClientComparator clientComparator = new ClientComparator();
//                Collections.sort(apData, clientComparator);
//                break;
//            default:
//                break;
//        }
        return apData;
    }

    //wps设备返回结果
    public static List<WiFiDetail> responseWpsApData(JSONObject response, int tag, int sort) throws JSONException {

        List<WiFiDetail> apData = new ArrayList<WiFiDetail>();
        if (response.getInt("id") == -1) {//表明没有数据
            return null;
        }
        JSONObject jo = new JSONObject(BaseUtils.JSONTokener(response.getString("data")));//获取到data数据
        JSONObject jsonObjectAps = jo.getJSONObject("aps");
        JSONObject jsonObjectStas = jo.getJSONObject("stas");
        Iterator<?> keys = jsonObjectAps.keys();
        String chanel=null,bssid=null,enc=null,essid=null,cipher="",clientTmp=null,wps=null;
        int power=0;
        double cRate=0;
        while (keys.hasNext()) {//遍历所有扫描到的数据
            String key = (String)keys.next();
            if (key.equals("")) continue;
            JSONObject jsonObjectAp = jsonObjectAps.getJSONObject(key);//获取单条wifi数据的所有信息
            if (tag == 1 && jsonObjectAp.get("wps") == null) continue;
            if (jsonObjectAp.get("essid") instanceof String) {
                essid = jsonObjectAp.getString("essid");
            }
            else {
                essid = "";
            }

            int tmp = 0;
            try {
                tmp = jsonObjectAp.getInt("power");//获取功率
            } catch (JSONException e) {
                tmp = 0;
            }
            power = tmp;

            JSONArray ja = jsonObjectAp.getJSONArray("enc");//获取加密方式 可能是多种加密方式，所以为Array
            String privacy = "";
            if (ja.length() > 0) {
                privacy = ja.getString(0);
            }
            enc = privacy;
            if (jsonObjectAp.get("wps") == null){
                wps = "false";
            }else if(jsonObjectAp.get("wps").equals("configured")) {
                wps = "true";
            }else{
                wps = "false";
            }
            bssid = key;
            JSONArray wpa_cipher = jsonObjectAp.getJSONArray("wpa_cipher");//获取wpa暗号
            JSONArray wpa2_cipher = jsonObjectAp.getJSONArray("wpa2_cipher");//获取wpa2暗号
            for (int i = 0; i < wpa_cipher.length(); i++) {//遍历暗号
                if (cipher.equals("")) {
                    cipher = cipher + wpa_cipher.getString(i);//拼接暗号
                }
                else {
                    cipher = cipher + " " + wpa_cipher.getString(i);//拼接暗号
                }
            }
            for (int i = 0; i < wpa2_cipher.length(); i++) {//遍历暗号
                if (cipher.equals("")) {
                    cipher = cipher + wpa2_cipher.getString(i);//拼接暗号
                }
                else {
                    cipher = cipher + " " + wpa2_cipher.getString(i);//拼接暗号
                }
            }
            JSONArray clientBssids = jsonObjectAp.getJSONArray("sta_bssids");
            JSONArray clients = new JSONArray();
            for (int i = 0; i < clientBssids.length(); i++) {//遍历客户端wifi的mac地址
                String clientBssid = clientBssids.getString(i);
                JSONArray probesArr;
                String probes;
                if (jsonObjectStas.has(clientBssid) && jsonObjectStas.getJSONObject(clientBssid).has("probes")) {
                    probes = jsonObjectStas.getJSONObject(clientBssid).getJSONArray("probes").toString();
                }
                else {
                    probes = "";
                }
                if (probes.equals("[]")) {
                    probes = "无";
                }
                probes = probes.replace("[", "").replace("]", "").replace("\"", "");
                JSONObject client = new JSONObject();
                client.put("mac", clientBssid);
                client.put("probe", probes);
                clients.put(client);
            }
            clientTmp = clients.toString();

            chanel = String.valueOf(jsonObjectAp.getInt("channel"));
            JSONArray basic_rates = jsonObjectAp.getJSONArray("basic_rates");//获取基础速率组
            JSONArray extra_rates = jsonObjectAp.getJSONArray("extra_rates");//获取额外速率组
            double minRate = -1;
            for (int i = 0; i < basic_rates.length(); i++) {
                if (basic_rates.getDouble(i) < minRate || minRate < 0) {
                    minRate = basic_rates.getDouble(i);
                }
            }
            if (basic_rates.length() == 0) {
                for (int i = 0; i < extra_rates.length(); i++) {
                    if (extra_rates.getDouble(i) < minRate || minRate < 0) {
                        minRate = extra_rates.getDouble(i);
                    }
                }
            }
            cRate = minRate;

            if (wps.equals("true")){
                WiFiWidth wiFiWidth =WiFiWidth.MHZ_40;//模拟wifi宽度
                WiFiSignal addWiFiSignal = new WiFiSignal(1,2,wiFiWidth,power,chanel);//模拟wifi信号 13：power dbm,
                WiFiDetail addWiFiDetail = new WiFiDetail(essid,bssid,enc,addWiFiSignal,clientTmp,cipher,wps,cRate);//模拟单条wifi的基本信息 1111:essid  fds:bssid  fdss:enc
                apData.add(addWiFiDetail);//将单个wifi对象添加到wifi对象组list
            }
        }
        return apData;
    }
}