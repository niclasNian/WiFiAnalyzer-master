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

package com.vrem.wifianalyzer.wifi.scanner;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;

import com.vrem.util.EnumUtils;
import com.vrem.wifianalyzer.wifi.band.WiFiWidth;
import com.vrem.wifianalyzer.wifi.model.WiFiConnection;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;
import com.vrem.wifianalyzer.wifi.model.WiFiUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Transformer {

    //转换当前连接wifi的动态信息 将WifiInfo类型转成WiFiConnection
    WiFiConnection transformWifiInfo(WifiInfo wifiInfo) {
        if (wifiInfo == null || wifiInfo.getNetworkId() == -1) {
            return WiFiConnection.EMPTY;
        }
        return new WiFiConnection(
            WiFiUtils.convertSSID(wifiInfo.getSSID()),
            wifiInfo.getBSSID(),
            WiFiUtils.convertIpAddress(wifiInfo.getIpAddress()),
            wifiInfo.getLinkSpeed());
    }

    //将List<WifiConfiguration>类型转成List<String> 手机连接成功的所有wifi列表
    List<String> transformWifiConfigurations(List<WifiConfiguration> configuredNetworks) {
        return new ArrayList<>(CollectionUtils.collect(configuredNetworks, new ToString()));
    }

    //将List<CacheResult>集合转换成List<WiFiDetail> 扫描到的所有wifi数据
    List<WiFiDetail> transformCacheResults(List<CacheResult> cacheResults) {
        return new ArrayList<>(CollectionUtils.collect(cacheResults, new ToWiFiDetail()));
    }

    //获取wifi MHz跨度
    WiFiWidth getWiFiWidth(@NonNull ScanResult scanResult) {
        try {
            return EnumUtils.find(WiFiWidth.class, getFieldValue(scanResult, Fields.channelWidth), WiFiWidth.MHZ_20);
        } catch (Exception e) {
            return WiFiWidth.MHZ_20;
        }
    }

    //获取wifi中心频率
    int getCenterFrequency(@NonNull ScanResult scanResult, @NonNull WiFiWidth wiFiWidth) {
        try {
            int centerFrequency = getFieldValue(scanResult, Fields.centerFreq0);
            if (centerFrequency == 0) {
                centerFrequency = scanResult.frequency;
            } else if (isExtensionFrequency(scanResult, wiFiWidth, centerFrequency)) {
                centerFrequency = (centerFrequency + scanResult.frequency) / 2;
            }
            return centerFrequency;
        } catch (Exception e) {
            return scanResult.frequency;
        }
    }

    //扩展频率
    boolean isExtensionFrequency(@NonNull ScanResult scanResult, @NonNull WiFiWidth wiFiWidth, int centerFrequency) {
        return WiFiWidth.MHZ_40.equals(wiFiWidth) && Math.abs(scanResult.frequency - centerFrequency) >= WiFiWidth.MHZ_40.getFrequencyWidthHalf();
    }

    int getFieldValue(@NonNull ScanResult scanResult, @NonNull Fields field) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = scanResult.getClass().getDeclaredField(field.name());
        return (int) declaredField.get(scanResult);
    }

    //将List<CacheResult>类型的数据转为List<WiFiDetail>，WifiInfo转为WiFiConnection，List<WifiConfiguration>转为List<String> wifiConfigurations
    WiFiData transformToWiFiData(List<CacheResult> cacheResults, WifiInfo wifiInfo, List<WifiConfiguration> configuredNetworks, JSONObject jsonObject) {
        List<WiFiDetail> wiFiDetails = transformCacheResults(cacheResults);//转换扫描附近wifi结果
        WiFiConnection wiFiConnection = transformWifiInfo(wifiInfo);//转换当前连接wifi动态信息
        List<String> wifiConfigurations = transformWifiConfigurations(configuredNetworks);//转换手机中曾经连接成功的wifi列表
        return new WiFiData(wiFiDetails, wiFiConnection, wifiConfigurations,jsonObject); //将转换的wifi信息返回给WiFiData
    }
    WiFiData transformToWiFiData(List<WiFiDetail> wiFiDetails1, WifiInfo wifiInfo, List<WifiConfiguration> configuredNetworks) {
        List<WiFiDetail> wiFiDetails = wiFiDetails1;//转换扫描附近wifi结果
        WiFiConnection wiFiConnection = transformWifiInfo(wifiInfo);//转换当前连接wifi动态信息
        List<String> wifiConfigurations = transformWifiConfigurations(configuredNetworks);//转换手机中曾经连接成功的wifi列表
        return new WiFiData(wiFiDetails, wiFiConnection, wifiConfigurations); //将转换的wifi信息返回给WiFiData
    }

    //channelWidth 信道宽度；centerFreq0 中心频率
    enum Fields {
        centerFreq0,
        //        centerFreq1,
        channelWidth
    }

    //Transformer<I, O>输入类型，输出类型
    private class ToWiFiDetail implements org.apache.commons.collections4.Transformer<CacheResult, WiFiDetail> {
        @Override
        public WiFiDetail transform(CacheResult input) {
            ScanResult scanResult = input.getScanResult();
            WiFiWidth wiFiWidth = getWiFiWidth(scanResult);
            int centerFrequency = getCenterFrequency(scanResult, wiFiWidth);
            WiFiSignal wiFiSignal = new WiFiSignal(scanResult.frequency, centerFrequency, wiFiWidth, input.getLevelAverage(),"");
            return new WiFiDetail(scanResult.SSID, scanResult.BSSID, scanResult.capabilities, wiFiSignal,"", "","",0);
        }
    }

    private class ToString implements org.apache.commons.collections4.Transformer<WifiConfiguration, String> {
        @Override
        public String transform(WifiConfiguration input) {
            return WiFiUtils.convertSSID(input.SSID);
        }
    }
}
