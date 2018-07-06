package com.vrem.wifianalyzer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.wifi.model.ClientInfo;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by ZhenShiJie on 2018/3/21.
 */

public class WifiInfoActivity extends Activity {

    private WiFiDetail wiFiDetail;

    private TextView channel;
    private TextView client;
    private TextView signal;
    private TextView encry;
    private TextView wps;
    private TextView method;
    private TextView mac;
    private TextView company;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_info);
        LayoutInflater lif = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View wifiListView = lif.inflate(R.layout.wifi_info,null);
        channel = wifiListView.findViewById(R.id.channel);
        client = wifiListView.findViewById(R.id.client);
        signal = wifiListView.findViewById(R.id.signal);
        encry = wifiListView.findViewById(R.id.encry);
        wps = wifiListView.findViewById(R.id.wps);
        method = wifiListView.findViewById(R.id.method);
        mac = wifiListView.findViewById(R.id.mac);
        company = wifiListView.findViewById(R.id.company);
        listView = findViewById(R.id.clientlist);
        listView.addHeaderView(wifiListView);
        TextView nodata = findViewById(R.id.nodata);
        String wifiDetailJson = getIntent().getStringExtra("wifiDetail");
        wiFiDetail = new Gson().fromJson(wifiDetailJson,WiFiDetail.class);
        try {
            ClientInfo.setClientInfo(WifiInfoActivity.this, wiFiDetail, listView,
                    nodata);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String strchannel = wiFiDetail.getWiFiSignal().getChannel();
        channel.setText(strchannel);
        JSONArray clientJson = null;
        try {
            if (wiFiDetail.getClient()!=null&& !"".equals(wiFiDetail.getClient())){
                clientJson = new JSONArray(wiFiDetail.getClient());
                int inClient = clientJson.length();
                client.setText(""+inClient);
            }else{
                client.setText(""+0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int j =wiFiDetail.getWiFiSignal().getLevel();
        String strSignal = String.valueOf(j);
        signal.setText(strSignal);
        String strEnc = wiFiDetail.getCapabilities();
        if (strEnc.length()>4){
            String str1 =strEnc.substring(2,strEnc.length()-2);//截取字符串 ["abs"]
            encry.setText(str1);
        }else{
            encry.setText(strEnc);
        }
        if (wiFiDetail.getWps().equals("true"))
            wps.setText("是");
        else
            wps.setText("否");
        String strMethod = wiFiDetail.getCipher();
        if (strMethod.length()>=8){
            String tmp = wiFiDetail.getCipher().substring(0,4);
            method.setText(tmp);
        }else{
            method.setText("无");
        }
        String strMac = wiFiDetail.getBSSID();
        mac.setText(strMac);
//        Log.v("获取到的数据：",wiFiDetail.getSSID());
    }
}
