package com.vrem.wifianalyzer.wifi.common;

import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/2.
 */

public interface AsyncResponse {
    void onDataReceivedSuccess(List<WiFiDetail> listData);
}
