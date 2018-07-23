package com.vrem.wifianalyzer.wifi.fragmentSniffer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.CommonUpdater;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.HandshakeUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ZhenShiJie on 2018/5/7.
 */

public class SnifferFragment extends Fragment {

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
    private String ssidText;
    private String bssidText;
    private int channelId;
    private double rate;
    private Spinner spinner1;
    private Spinner spinner2;
    private Context context;
    private String devId;
    private Bundle bundle;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view               = inflater.inflate(R.layout.fragment_sniffer,container,false);
        context                 = getContext();
        devId                   = PrefSingleton.getInstance().getString("device");//获取设备ID
        apChooseButton          = view.findViewById(R.id.apchoose);//选择热点按钮
        mPageTitles             = mDrawerTitle = getActivity().getTitle();
        mDrawerItems            = getResources().getStringArray(R.array.drawer_item_array);
        mDrawerLayout           = view.findViewById(R.id.drawer_layout);
        mLeftContainer          = view.findViewById(R.id.left_container);
        mDrawerList             = view.findViewById(R.id.left_drawer);
        sendLayout              = view.findViewById(R.id.sendlayout);
        methodLayout            = view.findViewById(R.id.methodlayout);
        startButton             = view.findViewById(R.id.startButton);
        spinner1                = view.findViewById(R.id.sendspinner);
        spinner2                = view.findViewById(R.id.methodspinner);
        bundle                  = getArguments();//获取传递参数
        ssidText      = bundle.getString("ssid");
        if (ssidText != null){ //ssid为空，需要设置
            apChooseButton.setText(ssidText);
            bssidText = bundle.getString("bssid");
            channelId = bundle.getInt("channel",0);
            rate      = bundle.getDouble("rate",0.0);
        }
        SpinnerAdapter adapter1 = ArrayAdapter.createFromResource(getContext(),R.array.send_spinner, R.layout.dropdown_listitem);
        spinner1.setAdapter(adapter1);//绑定保存方式数据

        SpinnerAdapter adapter2 = ArrayAdapter.createFromResource(getContext(),R.array.method_spinner, R.layout.dropdown_listitem);
        spinner2.setAdapter(adapter2);//绑定抓取方式数据
        MainContext.INSTANCE.getScannerService().pause();//暂停扫描服务，避免命令冲突
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!= null){
            //FragmentActivity的onSaveInstanceState方法可以看到，fragment保存时的标签是android:support:fragments
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
        apChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apChooseButtonHanble();
            }
        });

        //开始截获握手包
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
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
                            if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new HandshakeUpdater(getContext(), bssidText, channelId, devId, fSnifferDos, rate).execute();
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0);
                }else{
                    Toast.makeText(getContext(), "请选择目标热点！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //选择热点按钮事件
    private void apChooseButtonHanble(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.scan_dialog_list, null);
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(view);
        dialog.setTitle("热点列表");
        dialog.show();
        final ListView listview         = view.findViewById(R.id.scanlist);
        final ProgressBar progressBar   = view.findViewById(R.id.progressbar);
        final TextView refresh          = view.findViewById(R.id.clickrefresh);
        final TextView noData           = view.findViewById(R.id.nodata);
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
                    BackgroundTask.mTimerScan       = new Timer();
                    BackgroundTask.mTimerTaskScan   = new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new CommonUpdater(getContext(), listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
                } catch (Exception e) {
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
            BackgroundTask.mTimerScan       = new Timer();
            BackgroundTask.mTimerTaskScan   = new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() == null){
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new CommonUpdater(getContext(), listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
                        }
                    });
                }
            };
            BackgroundTask.mTimerScan.schedule(BackgroundTask.mTimerTaskScan, 0, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
                BackgroundTask.clearAll();
                TextView ssid       = arg1.findViewById(R.id.ssid);
                WiFiDetail apInfo = (WiFiDetail) ssid.getTag();
                if (apInfo.getSSID().length() >= 13)
                    apChooseButton.setText(apInfo.getSSID().substring(0, 13));
                else
                    apChooseButton.setText(apInfo.getSSID());
                dialog.dismiss();
                ssidText    = apInfo.getSSID();
                bssidText   = apInfo.getBSSID();
                channelId   = Integer.parseInt(apInfo.getWiFiSignal().getChannel());
                rate        = apInfo.getRate();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
