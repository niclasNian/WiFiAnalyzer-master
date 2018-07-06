package com.vrem.wifianalyzer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.HandshakeUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.common.CommonUpdater;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ZhenShiJie on 2018/4/4.
 */

public class SnifferActivity extends Activity {
    private ListView listview;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mLeftContainer;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mPageTitles;
    private String[] mDrawerItems;
    private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
    private Button apChooseButton;
    private RelativeLayout sendLayout;
    private RelativeLayout methodLayout;
    private ImageButton gButton;
    private ImageButton localButton;
    private Button startButton;
    private Button cancelButton;
    private String ssidText;
    private String bssidText;
    private int channelId;
    private double rate;
    private Spinner spinner1;
    private Spinner spinner2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sniffer);

        final Context context = this;
        final String devId = PrefSingleton.getInstance().getString("device");//获取设备ID
        apChooseButton = findViewById(R.id.apchoose);
        mPageTitles = mDrawerTitle = getTitle();
        mDrawerItems = getResources().getStringArray(R.array.drawer_item_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftContainer = (RelativeLayout) findViewById(R.id.left_container);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        sendLayout = (RelativeLayout) findViewById(R.id.sendlayout);
        methodLayout = (RelativeLayout) findViewById(R.id.methodlayout);
        startButton = (Button) findViewById(R.id.startButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        ssidText =intent.getStringExtra("ssid");
        if (ssidText != null){
            apChooseButton.setText(ssidText);
        }
        bssidText = intent.getStringExtra("bssid");
        channelId = intent.getIntExtra("channel", 0);
        rate = intent.getDoubleExtra("rate", 0.0);

        apChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                View view = SnifferActivity.this.getLayoutInflater().inflate(R.layout.scan_dialog_list, null);
                final Dialog dialog = new Dialog(SnifferActivity.this);
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
                                            new CommonUpdater(SnifferActivity.this, listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
                                        }
                                    });
                                }
                            };
                            BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
                        } catch (/*JSON*/Exception e) {
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
                                    new CommonUpdater(SnifferActivity.this, listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
                } catch (/*JSON*/Exception e) {
                    e.printStackTrace();
                }
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        BackgroundTask.clearAll();
                        TextView ssid = (TextView) arg1.findViewById(R.id.ssid);
                        WiFiDetail apInfo = (WiFiDetail) ssid.getTag();
                        if (apInfo.getSSID().length() >= 13)
                            apChooseButton.setText(apInfo.getSSID().substring(
                                    0, 13));
                        else
                            apChooseButton.setText(apInfo.getSSID());
                        dialog.dismiss();
                        ssidText = apInfo.getSSID();
                        bssidText = apInfo.getBSSID();
                        channelId = Integer.parseInt(apInfo.getWiFiSignal().getChannel());
                        rate = apInfo.getRate();
                    }
                });
            }
        });

        spinner1 = findViewById(R.id.sendspinner);
        SpinnerAdapter adapter1 = ArrayAdapter.createFromResource(this,R.array.send_spinner, R.layout.dropdown_listitem);
        spinner1.setAdapter(adapter1);//绑定保存方式数据

        spinner2 = findViewById(R.id.methodspinner);
        SpinnerAdapter adapter2 = ArrayAdapter.createFromResource(this,R.array.method_spinner, R.layout.dropdown_listitem);
        spinner2.setAdapter(adapter2);//绑定抓取方式数据

        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
                overridePendingTransition(R.anim.slide_left_in,
                        R.anim.slide_right_out);
            }
        });

        //开始截获握手包
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (apChooseButton.getText().toString().equals("选择目标热点") == false) {

                    boolean snifferDos = true;
                    if (spinner2.getSelectedItem().toString().equals("静默模式")) {
                        snifferDos = false;
                    }
                    final boolean fSnifferDos = snifferDos;

                    DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
                    devStatusDBUtils.open();
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
                                    new HandshakeUpdater(SnifferActivity.this, bssidText, channelId, devId, fSnifferDos, rate).execute();
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0);
                }else{
                    Toast.makeText(SnifferActivity.this, "请选择目标热点！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
