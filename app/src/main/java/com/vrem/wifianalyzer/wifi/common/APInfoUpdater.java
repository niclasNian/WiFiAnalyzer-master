package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.scanner.WifiDetailImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/2.
 */

public class APInfoUpdater extends AsyncTask<Object, Object, List<WiFiDetail>>{
    Context mContext;
    ListView mListView;
    String mDevId;
    ProgressBar mProgressBar;
    int mTag;
    int mMainPage;
    int mSort;
    boolean mStep1Needed;

    TextView mRefresh;
    TextView mNoData;
    boolean mIsDialog;

    boolean mError;
    List<WiFiDetail> wiFiDetails = new ArrayList<>();
    public AsyncResponse asyncResponse;

    public APInfoUpdater(Context context, String devId, int tag, int mainPage, int sort) {
        this.mContext = context;
        this.mDevId = devId;
        this.mTag = tag;
        this.mMainPage = mainPage;
        this.mSort = sort;

        this.mError = false;
    }
    public APInfoUpdater (Context context, ListView listView, String devId, int tag, ProgressBar progressBar, int mainPage, int sort, TextView refresh, TextView noData, boolean isDialog) {
        mContext = context;
        mListView = listView;
        mDevId = devId;
        mTag = tag;
        mProgressBar = progressBar;
        mMainPage = mainPage;
        mSort = sort;
        mRefresh = refresh;
        mNoData = noData;
        mIsDialog = isDialog;

        mError = false;
    }

    public void setOnAsyncResponse(AsyncResponse asyncResponse)
    {
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected void onPreExecute() {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int scanStep1Done = devStatusDBUtils.getScanstep1done(mDevId);//查询数据库中是否有设备ID
        devStatusDBUtils.close();

        if (scanStep1Done == 0){ //数据库中没有设备ID
            DevStatusDBUtils insetrtID = new DevStatusDBUtils(mContext);
            insetrtID.open();
            insetrtID.tryInsertNewDev(mDevId);
            scanStep1Done = insetrtID.getScanstep1done(mDevId);//插入设备ID
            insetrtID.close();
            if (scanStep1Done == 0){
                mStep1Needed = true;
            }else{mStep1Needed = false;}
        }else{
            mStep1Needed = false;
        }
    }

    public static boolean doScanStep1 (Context context, String devID) throws JSONException {
        int r1 = WiFiDetail.scanStep1(context);
        if (r1 < 0) {
            return false;
        }

        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
        devStatusDBUtils.open();
        devStatusDBUtils.scanStep1Done(devID);//扫描结束时的sql语句 更新1
        devStatusDBUtils.close();

        return true;
    }

    @Override
    protected List<WiFiDetail> doInBackground(Object[] objects) {
        try {
            if (mStep1Needed) {
                doScanStep1(mContext, mDevId);
                return null;
            }

            JSONObject response = WiFiDetail.scanStep2(mContext);//获得周围wifi数据
            if (response == null) {
                return null;
            }
            List<WiFiDetail> apData = WiFiDetail.response2ApData(response, mTag, mSort);
            if (apData == null) {
                mError = true;
            }
            wiFiDetails = apData;
            if (wiFiDetails != null && asyncResponse !=null){
                asyncResponse.onDataReceivedSuccess(wiFiDetails);//将扫描结果存入asyncResponse接口当中，供其它类使用数据
            }
            return wiFiDetails;
        } catch (JSONException e) {
            mError = true;
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<WiFiDetail> param) {
        if (param == null) {
            if (mError) {
                Log.w("SCAN_ERROR", "STEP2 INVALID RESPONSE");
                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                devStatusDBUtils.open();
                devStatusDBUtils.scanStep2Error(mDevId);//更新2
                devStatusDBUtils.close();
            }
            return;
        }
        MacSsidDBUtils macSsidDBUtils = new MacSsidDBUtils(mContext);
        macSsidDBUtils.open();
        for (WiFiDetail apInfo : param) {
            try {
                macSsidDBUtils.insertOrUpdate(mDevId, apInfo.getBSSID(), apInfo.getSSID());
            } catch (Exception e) {
            }
        }
        macSsidDBUtils.close();
    }
}
