package com.vrem.wifianalyzer.wifi.scanner;

import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ZhenShiJie on 2018/4/12.
 */

public class WifiDetailImpl implements IWifiDetailsCache {

    private static Map<String,List<WiFiDetail>> listMap = new ConcurrentHashMap<String,List<WiFiDetail>>();

    //插入对应缓存
    @Override
    public void putCache(String key,List<WiFiDetail> cache) {
        listMap.put(key,cache);
    }

    //获取对应缓存
    @Override
    public List<WiFiDetail> getCache(String key) {
        if (this.isContains(key)){
            return listMap.get(key);
        }
        return null;
    }

    //清除对应缓存
    @Override
    public void clearCache(String key) {
        if (this.isContains(key)){
            listMap.remove(key);
        }
    }

    //判断缓存是否存在
    public boolean isContains(String key){
        return listMap.containsKey(key);
    }
}
