package com.vrem.wifianalyzer.wifi.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhenShiJie on 2018/3/23.
 */

public class ClientInfo {

    private String mac;
    private String probe;
    private String company;



    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    //设置单条热点客户端
    public static void setClientInfo(Context context, WiFiDetail wiFiDetail,ListView listview, TextView noData) throws JSONException {
//        ListView listViewTmp = listview;
        final List<ClientInfo> clientData = new ArrayList<ClientInfo>();
        if (wiFiDetail.getClient()!=null&& !"".equals(wiFiDetail.getClient())){
            JSONArray client = new JSONArray(wiFiDetail.getClient());
            int length =  client.length();
            SQLiteDatabase db = context.openOrCreateDatabase("companymac.db",
                    Context.MODE_PRIVATE, null);
            for (int i = 0; i < length; i++) {
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.setMac(client.getJSONObject(i).getString("mac"));
                clientInfo.setProbe(client.getJSONObject(i).getString("probe"));

                String company_info = "无厂商信息";
//                String pre_mac = client.getJSONObject(i).getString("mac").substring(0, 8).replace(":", "").toUpperCase();
//                Cursor c = db.rawQuery("SELECT * FROM companymac WHERE mac ='" + pre_mac + "'", null);
//                while (c.moveToNext()) {
//                company_info = c.getString(c.getColumnIndex("company"));
//                }
//                c.close();
                clientInfo.setCompany(company_info);
                clientData.add(clientInfo);
            }
            db.close();

            if(clientData.size() == 0){
                noData.setVisibility(View.VISIBLE);
            }

            ClientListAdapter clientListAdapter = new ClientListAdapter(context,clientData, R.layout.client_list_item);
            listview.setAdapter(clientListAdapter);
        }
    }
    //设置所有热点客户端
    public static void setAllClientInfo(Context context,List<WiFiDetail> wiFiDetails,ListView listView,TextView noData) throws JSONException {
        List<WiFiDetail> wiFiDetailsTmp = wiFiDetails;
        final List<ClientInfo> clientData = new ArrayList<ClientInfo>();
        for (int i =0; i<wiFiDetailsTmp.size();i++){ //遍历所有热点
            if (wiFiDetailsTmp.get(i).getClient()!=null || !"".equals(wiFiDetailsTmp.get(i).getClient()) || !wiFiDetailsTmp.get(i).getClient().equals("[]")){
                JSONArray client = new JSONArray(wiFiDetailsTmp.get(i).getClient());
                ClientInfo clientInfo = new ClientInfo();
                if (client.length()>0){
                    for (int j =0; j<client.length();j++){
                        clientInfo.setMac(client.getJSONObject(j).getString("mac"));
                        clientInfo.setProbe(client.getJSONObject(j).getString("probe"));
                        String company_info = "无厂商信息";

                        clientInfo.setCompany(company_info);
                        clientData.add(clientInfo);
                    }
                }
            }
        }

        if(clientData.size() == 0){
            noData.setVisibility(View.VISIBLE);
        }
        ClientListAdapter clientListAdapter = new ClientListAdapter(context,clientData, R.layout.client_list_item);
        listView.setAdapter(clientListAdapter);
    }
}
