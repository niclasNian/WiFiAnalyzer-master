package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.adapter.APDialogListAdapter;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/10.
 */

public class CommonUpdater extends AsyncTask<Object, Object, List<WiFiDetail>> {
    Context mContext;
    ListView mListView;
    String mDevId;
    int mTag;
    ProgressBar mProgressBar;
    int mMainPage;
    int mSort;
    TextView mRefresh;
    TextView mNoData;
    boolean mIsDialog;

    boolean mStep1Needed;

    boolean mError;

    public CommonUpdater(Context context, ListView listView, String devId, int tag, ProgressBar progressBar, int mainPage, int sort, TextView refresh, TextView noData, boolean isDialog) {
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

    @Override
    protected void onPreExecute() {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int scanStep1Done = devStatusDBUtils.getScanstep1done(mDevId);
        devStatusDBUtils.close();

        if (scanStep1Done == 0) { //查询数据库中是否有设备ID
            DevStatusDBUtils insetrtID = new DevStatusDBUtils(mContext);
            insetrtID.open();
            insetrtID.tryInsertNewDev(mDevId);
            scanStep1Done = insetrtID.getScanstep1done(mDevId);//插入设备ID
            insetrtID.close();
            if (scanStep1Done == 0){
                mStep1Needed = true;
            }else{mStep1Needed = false;}
            mProgressBar.setVisibility(View.VISIBLE);
            if (mListView.getAdapter() == null) {
                mListView.setVisibility(View.GONE);
            }
        }
        else { // scanStatus == 1
            mStep1Needed = false;
            if (mNoData.getVisibility() == View.GONE && mListView.getAdapter() == null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
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

    //1
    @Override
    protected List<WiFiDetail> doInBackground  (Object... params) {
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

            return apData;
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
            } catch (Exception e) {}
        }
        macSsidDBUtils.close();

        mProgressBar.setVisibility(View.GONE);
        if (param.size() == 0) {
            mNoData.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.GONE);
        } else {
            mNoData.setVisibility(View.GONE);
            mRefresh.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            if (mIsDialog) {
                if (mListView.getAdapter() == null) {
                    mListView.setAdapter(new APDialogListAdapter(mContext, param, R.layout.scan_dialog_listitem));
                } else {
                    ((APDialogListAdapter)mListView.getAdapter()).UpdateData(param);
                    ((APDialogListAdapter)mListView.getAdapter()).notifyDataSetChanged();
                }
            } else {
            }
        }
    }
}
