package com.vrem.wifianalyzer.wifi.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.InteractRecordDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InfoUpdater extends AsyncTask<Object, Object, JSONObject> {
    Context mContext;
    boolean mIsFirst;

//    ProgressBar mProgressBar;

    public InfoUpdater(Context context, boolean isFirst) {
        mContext = context;
        mIsFirst = isFirst;
//        mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected JSONObject doInBackground  (Object... params) {
        try {
            JSONObject response = info(mContext);
            return response;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject param) {
        if (mIsFirst == false) {
            return;
        }

//        mProgressBar.setVisibility(View.GONE);

        if (param == null) {
            Toast.makeText(mContext, "出错啦", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int status = param.getInt("status");
            if (status == 0) {
                JSONObject data = param.getJSONObject("data");
                String devID;
                if (data.has("device")) {
                    devID = data.getString("device");
                } else {
                    devID = "HEHE2017";
                }
                PrefSingleton.getInstance().putString("device", devID);
            }
            else if (status == 1) {
                String devID = "HEHE2017";
//                Intent it = new Intent();
//                it.setClass(mContext, DeviceListActivity.class);

                PrefSingleton.getInstance().putString("device", devID);

//                mContext.startActivity(it);
//                ((Activity)mContext).overridePendingTransition(R.anim.slide_right_in,
//                        R.anim.slide_left_out);
            }
            else {
                Toast.makeText(mContext, "出错啦", Toast.LENGTH_SHORT).show();
                return;
            }
            status = param.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject info(final Context context) throws JSONException {
        String url = PrefSingleton.getInstance().getString("url");

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "info");
        //param.put("timestamp", System.currentTimeMillis() / 1000.0);
        obj.put("param", param);

        Log.w("INFO", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);

            new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString());//将请求命令、返回结果存入数据库

            Log.w("INFO", "RESPONSE: " + response.toString());
            return response;
        } catch (TimeoutException e) {
            Log.w("INFO_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
