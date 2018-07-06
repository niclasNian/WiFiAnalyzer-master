package com.vrem.wifianalyzer.wifi.common;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundTask {
    public static Timer mTimerHandling;
    public static TimerTask mTimerTaskHandling;
    public static Timer mTimerScan;
    public static TimerTask mTimerTaskScan;
    public static ScanStep1 mScanStep1;

    public static Timer mTimerInfo;
    public static TimerTask mTimerTaskInfo;

    public static void timerInfoStart(final Context context) {
        if(mTimerInfo == null) {
            mTimerInfo = new Timer();
        }
        mTimerTaskInfo   = new TimerTask() {
            @Override
            public void run() {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new InfoUpdater(context, false).execute();
                    }
                });
            }
        };
        BackgroundTask.mTimerInfo.schedule(BackgroundTask.mTimerTaskInfo, 0, 1000001);
    }

    public static void clearHandling() {
        if (mTimerTaskHandling != null) { //不为空
            mTimerTaskHandling.cancel();
            mTimerTaskHandling = null;
        }
        if (mTimerHandling != null) { //不为空
            mTimerHandling.cancel();
            mTimerHandling = null;
        }
    }

    public static void clearScan() {
        if (mTimerTaskScan != null) {
            mTimerTaskScan.cancel();
            mTimerTaskScan = null;
        }
        if (mTimerScan != null) {
            mTimerScan.cancel();
            mTimerScan = null;
        }
    }

    public static void clearScanStep1() {
        if (mScanStep1 != null) {
            mScanStep1.cancel(true);
            mScanStep1 = null;
        }
    }

    public static void clearAll() {
        clearScan();
        clearHandling();
        clearScanStep1();
    }
}
