package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;

public class ScanStep1 extends AsyncTask<Object, Object, Void> {
    Context mContext;
    String mDevId;

    public ScanStep1(Context context, String devID) {
        mContext = context;
        mDevId = devID;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground  (Object... params) {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int scanStep1Done = devStatusDBUtils.getScanstep1done(mDevId);
        devStatusDBUtils.close();
        if (scanStep1Done == 0) {
//            try {
//                APInfoUpdater.doScanStep1(mContext, mDevId);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
        BackgroundTask.clearScanStep1();
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
    }
}
