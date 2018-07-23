package com.vrem.wifianalyzer.wifi.fragmentWpsCrack;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
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
 * Created by ZhenShiJie on 2018/5/7.
 */

public class WpsCrackFragment extends Fragment {

    private Button apchoose;
    private Button startButton;
    private String channel;
    private String bssidText;
    private String inSSID;
    private String devId;
    private Context context;
    private Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view   = inflater.inflate(R.layout.fragment_wpscrack,container,false);
        apchoose    = view.findViewById(R.id.wpsApchoose);//选择热点按钮
        startButton = view.findViewById(R.id.startButton);//开始按钮
        devId       = PrefSingleton.getInstance().getString("device");//获取设备ID
        context     = getContext();
        bundle      = getArguments();
        inSSID      = bundle.getString("ssid");
        if (inSSID != null){
            apchoose.setText(inSSID);
            bssidText   = bundle.getString("bssid");
            channel     = bundle.getString("channel");
        }
        MainContext.INSTANCE.getScannerService().pause();//暂停扫描服务，避免命令冲突
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        apchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apChooseHanble();
            }
        });


        //开始按钮事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!apchoose.getText().toString().equals("选择目标热点")) {
                    try {
                        final JSONObject obj    = new JSONObject();
                        final JSONObject jo     = new JSONObject();
                        PrefSingleton.getInstance().Initialize(getContext());
                        int gId                 = PrefSingleton.getInstance().getInt("id");
                        PrefSingleton.getInstance().putInt("id", gId + 1);
                        obj.put("id", gId); // 1-1
                        JSONObject param        = new JSONObject(); // 2
                        param.put("action", "reaver"); // 2-1
                        param.put("bssid", bssidText);
                        param.put("channel", Integer.parseInt(channel));
                        obj.put("param",param);
                        jo.put("data",obj);
                        final JSONObject jsonObject = jo;

                        DevStatusDBUtils devStatusDBUtils   = new DevStatusDBUtils(context);
                        devStatusDBUtils.open();
                        final String devId                  = PrefSingleton.getInstance().getString("device");//获取设备ID
                        devStatusDBUtils.preHandling(devId);
                        int dosStep1Done = devStatusDBUtils.getCrackstep1done(devId);//查询数据是否存在，存在则更新为0，避免出现异常时数据没有得到更新，导致下次无法破解
                        if (dosStep1Done == 1){
                            devStatusDBUtils.wpscrackStep1Done(devId);
                        }
                        devStatusDBUtils.close();
                        BackgroundTask.clearAll();
                        BackgroundTask.mTimerHandling = new Timer();
                        if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                            return;
                        }
                        BackgroundTask.mTimerTaskHandling = new TimerTask() {
                            @Override
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new WpsCrackUpdater(getContext(), devId, jsonObject).execute();//开始破解
                                    }
                                });
                            }
                        };
                        BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 30000);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }else{
                    Toast.makeText(getContext(), "请选择目标热点！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //选择热点事件
    private void apChooseHanble(){
        View view           = getActivity().getLayoutInflater().inflate(R.layout.scan_dialog_list, null);
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
                    if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                        return;
                    }
                    BackgroundTask.mTimerTaskScan   = new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new GetWpsUpdater(getContext(), listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
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
            BackgroundTask.mTimerScan       = new Timer();
            BackgroundTask.mTimerTaskScan   = new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new GetWpsUpdater(getContext(), listview, devId, 0, progressBar, 1, 0, refresh, noData, true).execute();
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
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                BackgroundTask.clearAll();
                TextView ssid       = arg1.findViewById(R.id.ssid);
                WiFiDetail apInfo   = (WiFiDetail) ssid.getTag();
                if (apInfo.getSSID().length() >= 13) {
                    apchoose.setText(apInfo.getSSID().substring(0, 13));
                }
                else {
                    apchoose.setText(apInfo.getSSID());
                }
                channel     = apInfo.getWiFiSignal().getChannel();
                bssidText   = apInfo.getBSSID();
                dialog.dismiss();
            }
        });
    }
}
