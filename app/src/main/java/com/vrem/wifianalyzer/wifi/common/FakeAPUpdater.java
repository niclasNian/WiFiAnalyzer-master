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
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FakeAPUpdater extends AsyncTask<Object, Object, Void> {
    Context mContext;
    String mDevId;
    JSONObject mJo;

    boolean mStep1Needed;

    boolean mError;

    boolean mExit;
    boolean mStep1Run;

    public FakeAPUpdater(Context context, String devID, JSONObject jo) {
        mContext = context;
        mDevId = devID;
        mJo = jo;

        mError = false;
        mExit = false;
        mStep1Run = false;
    }

    @Override
    protected void onPreExecute() {
        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
        devStatusDBUtils.open();
        int handshakeStep1Done = devStatusDBUtils.getFakeapstep1done(mDevId);
        devStatusDBUtils.close();

        if (handshakeStep1Done == 0) {
            mStep1Needed = true;
        }
        else {
            mStep1Needed = false;
        }
    }

    @Override
    protected Void doInBackground  (Object... params) {
        try {
            if (mStep1Needed) {
                int r1 = fakeAPStep1(mContext, mJo);
                if (r1 < 0) {
                    mError = true;
                    return null;
                }
                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
                devStatusDBUtils.open();
                devStatusDBUtils.fakeAPStep1Done(mDevId, mJo.getString("out"), mJo.getString("essid"));
                devStatusDBUtils.close();
                mStep1Run = true;
                return null;
            }

            JSONObject response = fakeAPStep2(mContext);
            if (response == null) {
                return null;
            }
            JSONObject jo = response.getJSONObject("data");
            JSONArray clients = jo.getJSONArray("mac");
            Log.w("FAKE_STEP_2", "CLIENTS " + clients.toString());
            //return apData;
        } catch (JSONException e) {
            //mError = true;
            mExit = true;
            e.printStackTrace();

            Log.w("FAKE_ERROR", "STEP2 INVALID RESPONS");
            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
            devStatusDBUtils.open();
            devStatusDBUtils.fakeAPCancel(mDevId);
            devStatusDBUtils.close();

            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        if (mError == true) {
            Toast.makeText(mContext, "出错啦",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mStep1Run == true) {
            Intent intent = new Intent();
            intent.setClass(mContext,DeviceListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);  //跳转到功能运行界面
            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            ((Activity) mContext).finish();
            return;
        }
        if (mExit == true) {
            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(mContext);
            devStatusDBUtils.open();
            devStatusDBUtils.preScan(mDevId);
            devStatusDBUtils.close();

            BackgroundTask.clearAll();

            ((Activity) mContext).overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            ((Activity) mContext).finish();
        }
    }

    public int fakeAPStep1(final Context context, JSONObject jo) throws JSONException {
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();
        int gId = PrefSingleton.getInstance().getInt("id");
        PrefSingleton.getInstance().putInt("id", gId + 1);
        obj.put("id", gId); // 1-1
        JSONObject param = new JSONObject(); // 2
        param.put("action", "fakeap"); // 2-1
        param.put("essid", jo.getString("essid")); //2-2
        param.put("channel", jo.getInt("channel")); // 2-3
        JSONObject sec_param = new JSONObject(); // 2-4
        JSONObject out_param = new JSONObject(); // 2-5
        JSONObject sec_param_data = new JSONObject(); // 2-4-x
        JSONObject out_param_data = new JSONObject(); // 2-5-x

        if (jo.getString("net").equals("open")) { // 开放网络
            // 开放网络
            sec_param.put("type", "open");
        }
        else { // 加密网络
            sec_param.put("type", "wpa");

            sec_param_data.put("pass", jo.getString("password"));
            String wpaStr = jo.getString("security");
            int wpaInt;
            if (wpaStr.equals("wpa")) {
                wpaInt = 1;
            } else if (wpaStr.equals("wpa2")) {
                wpaInt = 2;
            }
            else {
                wpaInt = 3;
            }
            sec_param_data.put("wpa", wpaInt);
            sec_param_data.put("auth_algs", 1);
            sec_param_data.put("key_mgmt", "WPA-PSK");

            if (wpaInt == 1 || wpaInt == 3) {
                sec_param_data.put("wpa_pairwise", jo.getString("encryption"));
            }

            if (wpaInt == 2 || wpaInt == 3) {
                sec_param_data.put("rsn_pairwise", jo.getString("encryption"));
            }
        }
        sec_param.put("data", sec_param_data);

        if (jo.getString("out") == "4g") { // out = 4g
            out_param.put("type", "4g");
        } else { // out = wifi
            out_param.put("type", "wifi");

            out_param_data.put("essid", jo.getString("ssid"));
            String ticket = jo.getString("ticket");
            if (!ticket.equals("")) {
                out_param_data.put("pass", ticket);
            }
            out_param_data.put("channel", mJo.getInt("ap_channel"));
        }
        out_param.put("data", out_param_data);

        param.put("sec_param", sec_param);
        param.put("out_param", out_param);
        obj.put("param", param);

        Log.w("FAKE_STEP_1_REQUEST", obj.toString());

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
                Log.w("FAKE_STEP_1", "RESPONSE:" + response.toString());
                return 0;
            } else {
                Log.w("FAKE_STEP_1", "UNEXPECTED RESPONSE: " + response.toString());
                return -2;
            }
        } catch (TimeoutException e) {
            Log.w("FAKE_STEP_1", "TIMEOUT");
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return -3;
    }

    public JSONObject fakeAPStep2(final Context context) throws JSONException {
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "action");
        obj.put("param", param);

        Log.w("FAKE_STEP_2", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(5 - 1, TimeUnit.SECONDS);
            int status = response.getInt("status");
            if (status == 0) {
                Log.w("FAKE_STEP_2", "RESPONSE:" + response.toString());
                return response;
            } else {
                Log.w("FAKE_STEP_2", "UNEXPECTED RESPONSE: " + response.toString());
                return null;
            }
        } catch (TimeoutException e) {
            Log.w("FAKE_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearTask(){
        this.cancel(true);
    }
}
