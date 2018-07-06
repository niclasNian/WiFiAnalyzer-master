package com.vrem.wifianalyzer.wifi.dosClientModel;

/**
 * Created by ZhenShiJie on 2018/4/27.
 */

public class DosChildClientModel {
    private String child_bssid;//子dos_ap
    private int child_tx_datas;//子发送数据包
    private int child_rx_datas;//子接收数据包
    private int child_count;//子dos次数

    public DosChildClientModel(String child_bssid,int child_tx_datas, int child_rx_datas,int child_count) {
        this.child_bssid 	= child_bssid;
        this.child_tx_datas 	= child_tx_datas;
        this.child_rx_datas 	= child_rx_datas;
        this.child_count		= child_count;
    }

    public String getChild_bssid() {
        return child_bssid;
    }

    public void setChild_bssid(String child_bssid) {
        this.child_bssid = child_bssid;
    }

    public int getChild_tx_datas() {
        return child_tx_datas;
    }

    public void setChild_tx_datas(int child_tx_datas) {
        this.child_tx_datas = child_tx_datas;
    }

    public int getChild_rx_datas() {
        return child_rx_datas;
    }

    public void setChild_rx_datas(int child_rx_datas) {
        this.child_rx_datas = child_rx_datas;
    }

    public int getChild_count() {
        return child_count;
    }

    public void setChild_count(int child_count) {
        this.child_count = child_count;
    }
}
