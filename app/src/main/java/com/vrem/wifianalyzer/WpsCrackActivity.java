package com.vrem.wifianalyzer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.GetWpsUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.WpsCrackUpdater;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ZhenShiJie on 2018/4/4.
 */

public class WpsCrackActivity extends Activity {

    private Button apchoose;//选择热点按钮
    private Button cancelButton;//取消按钮
    private Button startButton;//开始按钮

    private String channel;
    private String bssidText;
    private String inSSID;

    private final String devId = PrefSingleton.getInstance().getString("device");//获取设备ID

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wpscrack);

        apchoose = findViewById(R.id.wpsApchoose);
        cancelButton = findViewById(R.id.cancelButton);
        startButton = findViewById(R.id.startButton);

        final Context context = this;

        Intent intent = getIntent();
        inSSID =intent.getStringExtra("ssid");
        if (inSSID != null){
            channel = intent.getStringExtra("channel");
            bssidText =intent.getStringExtra("bssid");
            apchoose.setText(inSSID);
        }
        apchoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    View view = WpsCrackActivity.this.getLayoutInflater().inflate(R.layout.scan_dialog_list, null);
                    final Dialog dialog = new Dialog(WpsCrackActivity.this);
                    dialog.setContentView(view);
                    dialog.setTitle("热点列表");
                    dialog.show();
                    final ListView listview = view.findViewById(R.id.scanlist);
                    final ProgressBar progressBar = view.findViewById(R.id.progressbar);
                    final TextView refresh = view.findViewById(R.id.clickrefresh);
                    final TextView noData = view.findViewById(R.id.nodata);
                    refresh.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            refresh.setVisibility(View.GONE);
                            try {
                                DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                                devStatusDBUtils.open();
                                devStatusDBUtils.preScan(devId);
                                devStatusDBUtils.close();

                                BackgroundTask.clearAll();
                                BackgroundTask.mTimerScan = new Timer();
                                BackgroundTask.mTimerTaskScan = new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new GetWpsUpdater(WpsCrackActivity.this, listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
                                            }
                                        });
                                    }
                                };
                                BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);

                            } catch (/*JSON*/Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                    try {
                        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                        devStatusDBUtils.open();
                        devStatusDBUtils.preScan(devId);
                        devStatusDBUtils.close();

                        BackgroundTask.clearAll();
                        BackgroundTask.mTimerScan = new Timer();
                        BackgroundTask.mTimerTaskScan = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new GetWpsUpdater(WpsCrackActivity.this, listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
                                    }
                                });
                            }
                        };
                        BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
                    } catch (/*JSON*/Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            BackgroundTask.clearAll();

                            // TODO Auto-generated method stub
                            TextView ssid = (TextView) arg1.findViewById(R.id.ssid);
                            WiFiDetail apInfo = (WiFiDetail) ssid.getTag();
                            if (apInfo.getSSID().length() >= 13) {
                                apchoose.setText(apInfo.getSSID().substring(0, 13));
                            }
                            else {
                                apchoose.setText(apInfo.getSSID());
                            }
                            channel = apInfo.getWiFiSignal().getChannel();
                            bssidText = apInfo.getBSSID();
                            dialog.dismiss();
                        }
                    });

                }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!apchoose.getText().toString().equals("选择目标热点")) {
                    try {
                        final JSONObject obj = new JSONObject();
                        final JSONObject jo = new JSONObject();
                        PrefSingleton.getInstance().Initialize(getApplicationContext());
                        int gId = PrefSingleton.getInstance().getInt("id");
                        PrefSingleton.getInstance().putInt("id", gId + 1);
                        obj.put("id", gId); // 1-1
                        JSONObject param = new JSONObject(); // 2
                        JSONArray channels = new JSONArray();
                        JSONArray wlist = new JSONArray();
                        JSONArray blist = new JSONArray();
                        param.put("action", "reaver"); // 2-1
                        param.put("bssid", bssidText);
                        param.put("channel", Integer.parseInt(channel));
                        obj.put("param",param);
                        jo.put("data",obj);
                        final JSONObject jsonObject = jo;

                        DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                        devStatusDBUtils.open();
                        final String devId = PrefSingleton.getInstance().getString("device");//获取设备ID
                        devStatusDBUtils.preHandling(devId);
                        devStatusDBUtils.close();
                        BackgroundTask.clearAll();
                        BackgroundTask.mTimerHandling = new Timer();
                        BackgroundTask.mTimerTaskHandling = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new WpsCrackUpdater(WpsCrackActivity.this, devId, jsonObject).execute();//开始破解
                                    }
                                });
                            }
                        };
                        BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 30000);


                    } catch (JSONException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }else{
                    Toast.makeText(WpsCrackActivity.this, "请选择目标热点！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
