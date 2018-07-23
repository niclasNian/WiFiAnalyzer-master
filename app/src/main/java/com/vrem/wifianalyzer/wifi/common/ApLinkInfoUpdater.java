package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot.WIFIHotspotFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Ap模式的连接，获取前置信息：电量%、4G信号强度%
 * */
public class ApLinkInfoUpdater extends AsyncTask<Object, Object, JSONObject> {
    private Context mContext;
    private boolean mIsFirst;

    public ApLinkInfoUpdater(Context context, boolean isFirst) {
        mContext = context;
        mIsFirst = isFirst;
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
                PrefSingleton.getInstance().putString("device", devID);
            }
            else {
                Toast.makeText(mContext, "出错啦", Toast.LENGTH_SHORT).show();
                return;
            }
//            status = param.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject info(final Context context) throws JSONException {
        String url = PrefSingleton.getInstance().getString("ApUrl");

        JSONObject obj = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("action", "info");
        obj.put("param", param);

        Log.w("AP_INFO", "REQUEST: " + obj.toString());

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  obj, requestFuture, requestFuture);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);

        try {
            JSONObject response = requestFuture.get(10 - 1, TimeUnit.SECONDS);
            if (null != response ){
                PrefSingleton.getInstance().putString("deviceInfo",response.toString());//将数据存入数据存储类中
            }

            new InteractRecordDBUtils(mContext).easy_insert(obj.toString(), response.toString());//将请求命令、返回结果存入数据库

            Log.w("AP_INFO", "RESPONSE: " + response.toString());
            return response;
        } catch (TimeoutException e) {
            Log.w("AP_INFO_STEP_2", "TIMEOUT");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
