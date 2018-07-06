package com.vrem.wifianalyzer.wifi.scanner;

import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/12.
 */

public class WifiDetailsCache {

    private List<WiFiDetail> datas;//保存的数据

    private  long timeOut;//设置数据失效时间,为0表示永不失效

    private  long lastRefeshTime;//最后刷新时间

    public WifiDetailsCache(List<WiFiDetail> datas, long timeOut, long lastRefeshTime) {
        this.datas = datas;
        this.timeOut = timeOut;
        this.lastRefeshTime = lastRefeshTime;
    }
    public List<WiFiDetail> getDatas() {
        return datas;
    }
    public void setDatas(List<WiFiDetail> datas) {
        this.datas = datas;
    }
    public long getTimeOut() {
        return timeOut;
    }
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
    public long getLastRefeshTime() {
        return lastRefeshTime;
    }
    public void setLastRefeshTime(long lastRefeshTime) {
        this.lastRefeshTime = lastRefeshTime;
    }

}
