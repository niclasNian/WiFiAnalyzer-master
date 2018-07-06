package com.vrem.wifianalyzer.wifi.dosClientModel;

import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/27.
 */

public class DosGroupClientModel {
    private String group_bssid;//父dos_ap
    private int group_tx_datas;//父发送数据包
    private int group_rx_datas;//父接收数据包
    private int group_count;//父dos次数
    private List<DosChildClientModel> dosChildClientModelList;//子ap集合

    public DosGroupClientModel(String group_bssid,int group_tx_datas,int group_rx_datas,int group_count,List<DosChildClientModel> dosChildClientModelList){
        this.group_bssid                = group_bssid;
        this.group_tx_datas             = group_tx_datas;
        this.group_rx_datas             = group_rx_datas;
        this.group_count                = group_count;
        this.dosChildClientModelList    = dosChildClientModelList;
    }

    public String getGroup_bssid() {
        return group_bssid;
    }

    public void setGroup_bssid(String group_bssid) {
        this.group_bssid = group_bssid;
    }

    public int getGroup_tx_datas() {
        return group_tx_datas;
    }

    public void setGroup_tx_datas(int group_tx_datas) {
        this.group_tx_datas = group_tx_datas;
    }

    public int getGroup_rx_datas() {
        return group_rx_datas;
    }

    public void setGroup_rx_datas(int group_rx_datas) {
        this.group_rx_datas = group_rx_datas;
    }

    public int getGroup_count() {
        return group_count;
    }

    public void setGroup_count(int group_count) {
        this.group_count = group_count;
    }

    public List<DosChildClientModel> getDosChildClientModelList() {
        return dosChildClientModelList;
    }

    public void setDosChildClientModelList(List<DosChildClientModel> dosChildClientModelList) {
        this.dosChildClientModelList = dosChildClientModelList;
    }
}
