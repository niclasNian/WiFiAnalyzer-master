package com.vrem.wifianalyzer.wifi.scanner;

import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/12.
 */

public interface IWifiDetailsCache {

    /**
     * 存入缓存
     * @param cache
     */
    void putCache(String key ,List<WiFiDetail> cache);


//    /**
//     * 存入缓存
//     * @param key
//     * @param wiFiDetails
//     */
//    void putCache(String key, List<WiFiDetail> wiFiDetails, long timeOut);

    /**
     * 获取缓存
     * */
    List<WiFiDetail> getCache(String key);

    /**
     * 清除对应缓存
     * */
    void clearCache(String key);
}
