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

import android.support.annotation.NonNull;
import android.util.Log;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.vendor.model.VendorService;
import com.vrem.wifianalyzer.wifi.band.WiFiWidth;



import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WiFiData {
    public static final WiFiData EMPTY = new WiFiData(Collections.<WiFiDetail>emptyList(), WiFiConnection.EMPTY, Collections.<String>emptyList());

    private final List<WiFiDetail> wiFiDetails;//存放扫描所得周围wifi的详细信息
    private final WiFiConnection wiFiConnection;//存放当前wifi的存放信息
    private final List<String> wiFiConfigurations;//存放手机中曾经连接成功的wifi数据

    private JSONObject jsonObject;

    //接收扫描的周围wifi、连接wifi、曾经wifi
    public WiFiData(@NonNull List<WiFiDetail> wiFiDetails, @NonNull WiFiConnection wiFiConnection, @NonNull List<String> wiFiConfigurations) {
        this.wiFiDetails = wiFiDetails;
        this.wiFiConnection = wiFiConnection;
        this.wiFiConfigurations = wiFiConfigurations;
    }

    //测试
    public WiFiData(List<WiFiDetail> wiFiDetails,WiFiConnection wiFiConnection,List<String> wiFiConfigurations,JSONObject jsonObject) {
        this.wiFiDetails = wiFiDetails;
        this.wiFiConnection = wiFiConnection;
        this.wiFiConfigurations = wiFiConfigurations;
        this.jsonObject=jsonObject;
     }

    //获取当前连接的wifi的基本信息
    @NonNull
    public WiFiDetail getConnection() {
        WiFiDetail wiFiDetail = IterableUtils.find(wiFiDetails, new ConnectionPredicate());
        return wiFiDetail == null ? WiFiDetail.EMPTY : copyWiFiDetail(wiFiDetail);
    }

    @NonNull
    public List<WiFiDetail> getWiFiDetails(@NonNull Predicate<WiFiDetail> predicate, @NonNull SortBy sortBy) {
        return getWiFiDetails(predicate, sortBy, GroupBy.NONE);
    }

    //在这开始没有数据
    @NonNull
    public List<WiFiDetail> getWiFiDetails(@NonNull Predicate<WiFiDetail> predicate, @NonNull SortBy sortBy, @NonNull GroupBy groupBy) {
        List<WiFiDetail> results = getWiFiDetails(predicate);//获取getWiFiDetails返回的所有wifi列表
        if (!results.isEmpty() && !GroupBy.NONE.equals(groupBy)) {
            results = sortAndGroup(results, sortBy, groupBy);//调用sortAndGroup方法进行排序组合
        }
        Collections.sort(results, sortBy.comparator());//升序排序
        return results;
    }

    //排序组合
    @NonNull
    List<WiFiDetail> sortAndGroup(@NonNull List<WiFiDetail> wiFiDetails, @NonNull SortBy sortBy, @NonNull GroupBy groupBy) {
        List<WiFiDetail> results = new ArrayList<>();
        Collections.sort(wiFiDetails, groupBy.sortOrderComparator());
        WiFiDetail parent = null;
        for (WiFiDetail wiFiDetail : wiFiDetails) {
            if (parent == null || groupBy.groupByComparator().compare(parent, wiFiDetail) != 0) {
                if (parent != null) {
                    Collections.sort(parent.getChildren(), sortBy.comparator());
                }
                parent = wiFiDetail;
                results.add(parent);
            } else {
                parent.addChild(wiFiDetail);//将单条数据添加到WiFiDetail
            }
        }
        if (parent != null) {
            Collections.sort(parent.getChildren(), sortBy.comparator());
        }
        Collections.sort(results, sortBy.comparator());
        return results;
    }

    //获取wifi详情 模拟添加数据
    @NonNull
    private List<WiFiDetail> getWiFiDetails(@NonNull Predicate<WiFiDetail> predicate){
        List<WiFiDetail> wiFiDetails1 = new ArrayList<>();
        if (jsonObject != null){
            try {
                String data = jsonObject.getString("data"); //先String获取json数据中的data字段

                JSONObject jsonObject = new JSONObject(data);//再将String转成json
                JSONObject apsjson = jsonObject.getJSONObject("aps");//再将jsonObject中的aps字段拿出
                Iterator iterator = apsjson.keys();
                while (iterator.hasNext()){
                    String key = (String) iterator.next();
                    String value = apsjson.getString(key);
                    JSONObject valueJson = new JSONObject(value);
                    Iterator valueIter = valueJson.keys();
//                    Log.v("头部信息"+key,"");
//                    JSONArray jsonArray = new JSONArray();
                    String chanel=null,bssid=null,enc=null,essid=null,cipher=null,clientTmp=null,wps=null;
                    int power=0;
                    JSONArray clients = new JSONArray();
                    while (valueIter.hasNext()){
                        String key1 = (String) valueIter.next();
                        String value1 = valueJson.getString(key1);
                        if ("channel".equals(key1) || "bssid".equals(key1) || "enc".equals(key1) || "power".equals(key1) || "essid".equals(key1) || "sta_bssids".equals(key1)
                                || "wpa2_cipher".equals(key1) || "wps".equals(key1)){
                            if ("channel".equals(key1)){
                                chanel = value1;
                            }else if("bssid".equals(key1)){
                                bssid = value1;
                            }else if("enc".equals(key1)){
//                                Log.v("打印的数据"+key1,value1);
                                enc = value1;
                            }else if("power".equals(key1) && !"null".equals(value1)){
                                power =Integer.parseInt(value1);
                            }else if("essid".equals(key1)){
                                if ("null".equals(value1)){
                                    essid = "隐藏SSID";
                                }else {
                                    essid = value1;
                                }
                            }else if ("sta_bssids".equals(key1)){
//                                client = value1;
                                JSONObject jsonObjectStas = jsonObject.getJSONObject("stas");
                                JSONArray clientBssids =valueJson.getJSONArray("sta_bssids");
                                for (int i = 0; i < clientBssids.length(); i++) {//遍历客户端wifi的mac地址
                                    String clientBssid = clientBssids.getString(i);
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
                                    JSONObject clientJson = new JSONObject();
                                    clientJson.put("mac", clientBssid);
                                    clientJson.put("probe", probes);
                                    clients.put(clientJson);
                                }
                                clientTmp = clients.toString();
//                                addWiFiDetail.setClient(clientTmp);
                            }else if("wpa2_cipher".equals(key1)){
                                cipher = value1;
                            }else if ("wps".equals(key1)){
                                if ("null".equals(value1)){
                                    wps = "否";
                                }else {
                                    wps = "是";
                                }
                            }
                        }
//                        Log.v("打印的数据"+key1,value1);
                    }
                    WiFiWidth wiFiWidth =WiFiWidth.MHZ_40;//模拟wifi宽度
                    WiFiSignal addWiFiSignal = new WiFiSignal(1,2,wiFiWidth,power,chanel);//模拟wifi信号 13：power dbm,
                    WiFiDetail addWiFiDetail = new WiFiDetail(essid,bssid,enc,addWiFiSignal,clientTmp,cipher,wps,0);//模拟单条wifi的基本信息 1111:essid  fds:bssid  fdss:enc
                    wiFiDetails1.add(addWiFiDetail);//将模拟数据添加添加到wiFiDetails1集合中
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collection<WiFiDetail> selected = CollectionUtils.select(wiFiDetails1, predicate);//集合筛选
        Collection<WiFiDetail> collected = CollectionUtils.collect(selected, new Transform());//获取selected对象集合的属性集合，并返回
//        return new ArrayList<>(collected);
        if (wiFiDetails1.size()<=0){
            return wiFiDetails;
        }else {
            return wiFiDetails1;
        }
    }

    @NonNull
    public List<WiFiDetail> getWiFiDetails() {
//        List<WiFiDetail> wiFiDetails1 = (List<WiFiDetail>) wiFiDetails.get(1);
        return Collections.unmodifiableList(wiFiDetails);
    }

    @NonNull
    public List<String> getWiFiConfigurations() {
        return Collections.unmodifiableList(wiFiConfigurations);
    }

    @NonNull
    public WiFiConnection getWiFiConnection() {
        return wiFiConnection;
    }

    @NonNull
    private WiFiDetail copyWiFiDetail(WiFiDetail wiFiDetail) {
        VendorService vendorService = MainContext.INSTANCE.getVendorService();
        String vendorName = vendorService.findVendorName(wiFiDetail.getBSSID());
        WiFiAdditional wiFiAdditional = new WiFiAdditional(vendorName, wiFiConnection);
        return new WiFiDetail(wiFiDetail, wiFiAdditional,wiFiDetail.getClient(),wiFiDetail.getCipher(),wiFiDetail.getWps(),wiFiDetail.getRate());
    }

    private class ConnectionPredicate implements Predicate<WiFiDetail> {
        @Override
        public boolean evaluate(WiFiDetail wiFiDetail) {
            return new EqualsBuilder()
                .append(wiFiConnection.getSSID(), wiFiDetail.getSSID())
                .append(wiFiConnection.getBSSID(), wiFiDetail.getBSSID())
                .isEquals();
        }
    }

    private class Transform implements Transformer<WiFiDetail, WiFiDetail> {
        private final WiFiDetail connection;
        private final VendorService vendorService;

        private Transform() {
            this.connection = getConnection();
            this.vendorService = MainContext.INSTANCE.getVendorService();
        }

        @Override
        public WiFiDetail transform(WiFiDetail input) {
            if (input.equals(connection)) {
                return connection;//返回当前手机正在连接的wifi的基本信息
            }
            String vendorName = vendorService.findVendorName(input.getBSSID());
            boolean contains = wiFiConfigurations.contains(input.getSSID());
            WiFiAdditional wiFiAdditional = new WiFiAdditional(vendorName, contains);
            return new WiFiDetail(input, wiFiAdditional,input.getClient(),input.getCipher(),input.getWps(),input.getRate());
        }
    }

}
