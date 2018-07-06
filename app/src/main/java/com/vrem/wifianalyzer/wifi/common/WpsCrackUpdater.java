package com.vrem.wifianalyzer.wifi.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WpsCrackUpdater extends AsyncTask<Object, Object, Void> {
    Context mContext;
    String mDevId;
    JSONObject mJo;

    boolean mStep1Needed;

    boolean mError;

    boolean mExit;
    boolean mStep1Run;

    public WpsCrackUpdater(Context context, String devID, JSONObject jo) {
        mContext = context;
        mDevId = devID;
        mJo = jo;

        mError = false;
        mExit = false;
        mStep1Run = false;
    }

    @Override  //1
    protected void onPreExecute() {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int dosStep1Done = devStatusDBUtils.getCrackstep1done(mDevId);//第一次进来就等于1了，所以无法执行true操作
        devStatusDBUtils.close();

        if (dosStep1Done == 0) {
            mStep1Needed = true;
        }
        else {
            mStep1Needed = false;
        }
    }

    @Override  //2
    protected Void doInBackground  (Object... params) {
        try {
            if (mStep1Needed) { //mStep1Needed=false 说明数据库的值为1
                int r1 = dosStep1(mContext, mJo.getJSONObject("data"));
                if (r1 < 0) {
                    mError = true;
                    return null;
                }
                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                devStatusDBUtils.open();
                devStatusDBUtils.crackStep1Done(mDevId);
                devStatusDBUtils.close();
                mStep1Run = true;
                return null;
            }

            JSONObject response = dosStep2(mContext);
            if (response == null) {
                return null;
            }
            Log.w("CRACK_STEP_2", response.toString());
            //return apData;
        } catch (JSONException e) {
            //mError = true;
            mExit = true;
            e.printStackTrace();

            Log.w("CRACK_ERROR", "STEP2 INVALID RESPONS");
            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
            devStatusDBUtils.open();
            devStatusDBUtils.dosCancel(mDevId);
            devStatusDBUtils.close();

            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        if (mError == true) {
            Toast.makeText(mContext, "出错啦",Toast.LENGTH_SHORT).show();
            return;
        }
        if (mStep1Run == true) {
            Intent intent = new Intent();
            intent.setClass(mContext,DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            ((Activity) mContext).finish();
            return;
        }
        if (mExit == true) {

            DevStatusDBUtils devStatusDBUtils1 = new DevStatusDBUtils(mContext);
            devStatusDBUtils1.open();
            devStatusDBUtils1.wpscrackStep1Done(mDevId);//扫描结束时的sql语句
            devStatusDBUtils1.close();

            BackgroundTask.clearAll();

            Intent intent = new Intent();
            intent.setClass(mContext,
                    DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            ((Activity) mContext).finish();
        }
    }

    public int dosStep1(final Context context, JSONObject jo) throws JSONException {
        String url ="http://192.168.100.1:9494";

        JSONObject obj = jo;
        Log.w("DOS_STEP_1_REQUEST", obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);

            new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString());//将请求命令、返回结果存入数据库

            int status = response.getInt("status");
            if (status == 0) {
                Log.w("CRACK_STEP_1", "RESPONSE:" + response.toString());
                return 0;
            } else {
                Log.w("CRACK_STEP_1", "UNEXPECTED RESPONSE: " + response.toString());
                return -2;
            }
        } catch (TimeoutException e) {
            Log.w("CRACK_STEP_1", "TIMEOUT");
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return -3;
    }

    public JSONObject dosStep2(final Context context) throws JSONException {
        String url ="http://192.168.100.1:9494";

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "action");
        obj.put("param", param);

        Log.w("CRACK_STEP_2", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(5 - 1, TimeUnit.SECONDS);
            int status = response.getInt("status");
            if (status == 0) {
                Log.w("CRACK_STEP_2", "RESPONSE:" + response.toString());
                return response;
            } else {
                Log.w("CRACK_STEP_2", "UNEXPECTED RESPONSE: " + response.toString());
                return null;
            }
        } catch (TimeoutException e) {
            Log.w("CRACK_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}