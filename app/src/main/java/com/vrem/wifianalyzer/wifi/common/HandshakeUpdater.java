package com.vrem.wifianalyzer.wifi.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.wifi.common.SnifferFilesDBUtils;

import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HandshakeUpdater extends AsyncTask<Object, Object, Void> {
    Context mContext;
    String mBssid;
    int mChannelID;

    String mDevId;

    boolean mStep1Needed;
    boolean mStep2Needed;

    boolean mExit;
    boolean mStep1Run; // dos
    boolean mStep2Run; // common hdsk step1


    boolean mSnifferDos;

    double mRate;

    public HandshakeUpdater(Context context, String bssid, int channelID, String devID, boolean snifferDos, double rate) {
        mContext = context;
        mBssid = bssid;
        mChannelID = channelID;
        mDevId = devID;

        mExit = false;
        mStep1Run = false;
        mStep2Run = false;

        mSnifferDos = snifferDos;

        mRate = rate;
    }

    @Override
    protected void onPreExecute() {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int handshakeStep1Done = devStatusDBUtils.getHandshakestep1done(mDevId);
        int handshakeStep2Done = devStatusDBUtils.getHandshakestep2done(mDevId);
        devStatusDBUtils.close();

        if (handshakeStep1Done == 0) {
            mStep1Needed = true;
        } else {
            mStep1Needed = false;
        }
        if (handshakeStep2Done == 0) {
            mStep2Needed = true;
        } else {
            mStep2Needed = false;
        }
    }

    @Override
    protected Void doInBackground  (Object... params) {
        try {
            if (mSnifferDos && mStep1Needed) { // 极速模式且未dos, mStep1对应dos
                JSONObject jo = new JSONObject();

                JSONObject obj = new JSONObject();
                int gId = PrefSingleton.getInstance().getInt("id");
                PrefSingleton.getInstance().putInt("id", gId + 1);
                obj.put("id", gId); // 1-1
                JSONObject param = new JSONObject(); // 2
                JSONArray channels = new JSONArray();
                JSONArray wlist = new JSONArray();
                JSONArray blist = new JSONArray();
                param.put("action", "mdk"); // 2-1
                jo.put("type", "ap");
                jo.put("detail", mBssid);
                blist.put(mBssid);
                channels.put(mChannelID);

                param.put("channels", channels); // 2-3
                param.put("wlist", wlist); // 2-4
                param.put("blist", blist); // 2-5
                param.put("interval", 1.5);
                obj.put("param", param);
                jo.put("data", obj);
                String url = PrefSingleton.getInstance().getString("url");

                jo = jo.getJSONObject("data");
                Log.w("DOS_STEP_1_REQUEST", jo.toString());

                RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  jo, requestFuture, requestFuture);
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(mContext).getRequestQueue().add(jsonObjectRequest);

                try {
                    JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);

                    new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString());//将请求命令、返回结果存入数据库

                    int status = response.getInt("status");
                    if (status == 0) {
                        Log.w("HDSK_DOS_STEP", "RESPONSE:" + response.toString());
                        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                        devStatusDBUtils.open();
                        devStatusDBUtils.handshakeStep1Done(mDevId, new Integer(mChannelID).toString() + "-" + new Integer(gId + 1).toString() + "-" + mBssid + ".cap" + "-" + new Double(mRate).toString());
                        devStatusDBUtils.close();
                        mStep1Run = true;
                        return null;
                    } else {
                        Log.w("HDSK_DOS_STEP", "UNEXPECTED RESPONSE: " + response.toString());
                    }
                } catch (TimeoutException e) {
                    Log.w("HDSK_DOS_STEP", "TIMEOUT");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(mContext, "阻断出错啦", Toast.LENGTH_SHORT).show();
                Log.w("HDSK_DOS_STEP", "阻断出错啦");
                return null;
            }

            else if (mSnifferDos && (!mStep1Needed) && mStep2Needed) { // dos过但未step2（即未common hdsk第一步）
                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                devStatusDBUtils.open();
                int handshakePrepareCount = devStatusDBUtils.getHandshakepreparecount(mDevId);

                if (handshakePrepareCount < 8) {
                    Log.w("HDSK", "DOS COUNTING" + new Integer(handshakePrepareCount).toString());
                    devStatusDBUtils.handshakePrepareCountUpdate(mDevId);
                    devStatusDBUtils.close();
                    return null;
                }
                int r = handshakeStep2(mContext);
                if (r < 0) {
                    devStatusDBUtils.close();
                    return null;
                }
                devStatusDBUtils.handshakeStep2DoneDos(mDevId);
                devStatusDBUtils.close();
                mStep2Run = true;
                return null;
            }

            else if ((!mSnifferDos) && mStep2Needed) {
                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                devStatusDBUtils.open();
                int r = handshakeStep2(mContext);
                if (r < 0) {
                    devStatusDBUtils.close();
                    return null;
                }
                devStatusDBUtils.handshakeStep2Done(mDevId, new Integer(mChannelID).toString() + "-" + new Integer(r).toString() + "-" + mBssid + ".cap");
                devStatusDBUtils.close();
                mStep2Run = true;
                return null;
            }

            else { // (mSnifferDos && (!mStep1Needed) && (!mStep2Needed)) || ((!mSnifferDos) && (!mStep2Needed))
                JSONObject response = handshakeStep3(mContext);
                if (response == null) {
                    return null;
                }
                JSONObject jo = response.getJSONObject("data");
                boolean handshakeComplete = jo.getBoolean("handshake_complete");
                if (!handshakeComplete) {
                    Log.w("HDSK STATUS", "not completed");
                } else {
                    String handshakeFile = jo.getString("handshake_file");
                    String mac = handshakeFile.split("-")[1].split("\\.")[0];
                    MacSsidDBUtils macSsidDBUtils = new MacSsidDBUtils(mContext);
                    macSsidDBUtils.open();
                    String essid = macSsidDBUtils.getSSID(mDevId, mac);
                    macSsidDBUtils.close();


                    SnifferFilesDBUtils snifferFilesDBUtils = new SnifferFilesDBUtils(mContext);
                    snifferFilesDBUtils.open();
                    snifferFilesDBUtils.insertNewFile(mDevId, handshakeFile, essid);
                    snifferFilesDBUtils.close();
                    Log.w("截获", "成功, " + handshakeFile);
                    mExit = true;
                }
            }
        } catch (JSONException e) {
            mExit = true;
            e.printStackTrace();

            Log.w("HDSK_ERROR", "STEP3 INVALID RESPONS");
            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
            devStatusDBUtils.open();
            devStatusDBUtils.handshakeCancel(mDevId);
            devStatusDBUtils.close();

            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        if (mStep1Run == true || mStep2Run == true) {
            Intent intent = new Intent();
            intent.setClass(mContext,DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
//            ((Activity) mContext).finish();
            return;
        }
        if (mExit == true) {
            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
            devStatusDBUtils.open();
            devStatusDBUtils.preScan(mDevId);
            devStatusDBUtils.close();

            BackgroundTask.clearAll();

            new AlertDialog.Builder(mContext).setTitle("状态").setMessage("截获成功！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
                    BackgroundTask.clearAll();
                    ((Activity) mContext).finish();
                }
            }).show();
        }
    }

    public int handshakeStep2(final Context context) throws JSONException { // 对应数据表step2, 数据表step1对应dos, mStep1对应dos
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();

        int gId = PrefSingleton.getInstance().getInt("id");
        PrefSingleton.getInstance().putInt("id", gId + 1);
        obj.put("id", gId);
        JSONObject param = new JSONObject();
        param.put("action", "handshake"); // 2-1
        param.put("bssid", mBssid); //2-2
        JSONArray channels = new JSONArray();
        channels.put(mChannelID);
        param.put("channels", channels); // 2-3
        param.put("rate", mRate * 1024.0 * 1024.0);
        obj.put("param", param);

        Log.w("HDSK_STEP_1_REQUEST", obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(5 - 1, TimeUnit.SECONDS);

            new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString());//将请求命令、返回结果存入数据库

            int status = response.getInt("status");
            if (status == 0) {
                Log.w("HDSK_STEP_1", "RESPONSE:" + response.toString());
                return gId;
            } else {
                Log.w("HDSK_STEP_1", "UNEXPECTED RESPONSE: " + response.toString());
                return -2;
            }
        } catch (TimeoutException e) {
            Log.w("HDSK_STEP_1", "TIMEOUT");
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return -3;
    }

    public JSONObject handshakeStep3(final Context context) throws JSONException {
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "action");
        obj.put("param", param);

        Log.w("HDSK_STEP_2", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(5 - 1, TimeUnit.SECONDS);
            int status = response.getInt("status");
            if (status == 0) {
                Log.w("HDSK_STEP_2", "RESPONSE:" + response.toString());
                return response;
            } else {
                Log.w("HDSK_STEP_2", "UNEXPECTED RESPONSE: " + response.toString());
                return null;
            }
        } catch (TimeoutException e) {
            Log.w("HDSK_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
