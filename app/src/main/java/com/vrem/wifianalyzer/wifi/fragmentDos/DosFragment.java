package com.vrem.wifianalyzer.wifi.fragmentDos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.DosUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ZhenShiJie on 2018/5/7.
 */

public class DosFragment extends Fragment {

    //立flag来判断显示的数据 未完成
    private boolean flag = false;

    private String ssid;
    private String bssid;
    private int channel;
    private Button apDosButton;
    private Button startButton;
    private  Button channelDosButton;
    private Button mChannelDosButton;
    private String channelSelected = ""; //多频段
    private int channelId = 1;//单频段

//    private String intentId="";
    private List<WiFiDetail> wiFiDetails;
    private String dosSsid;

    private View view;
    private Bundle bundle;

    //onCreateView为fragment的初始化页面方法
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view                = inflater.inflate(R.layout.fragment_dos, container, false);
        startButton 		= view.findViewById(R.id.startButton);//开始dos攻击按钮
//        cancelButton 		= view.findViewById(R.id.cancelButton);//取消攻击按钮
        channelDosButton 	= view.findViewById(R.id.channeldos);//单频段按钮
        mChannelDosButton 	= view.findViewById(R.id.mchanneldos);//多频段按钮
        apDosButton         = view.findViewById(R.id.apdos);//选择热点按钮
        bundle              = getArguments();
        if (bundle.getString("ssid") != null){
            bssid   = bundle.getString("bssid");
            ssid    = bundle.getString("ssid");
            channel = Integer.parseInt(bundle.getString("channel"));
            apDosButton.setText(ssid);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context 	= getContext();

        String wifiDetailJson 	= String.valueOf(new Gson().toJson(MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails()));
        Type type 				= new TypeToken<List<WiFiDetail>>(){}.getType();
        Gson gson 				= new Gson();
        wiFiDetails 			= gson.fromJson(wifiDetailJson,type);//将JSON数组转为对象
        apDosButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    apDosButtonHandle(apDosButton,wiFiDetails);
                }
        });

        //单频段按钮事件
        channelDosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                channelButtonHandle(channelDosButton);
            }
        });

        //多频段按钮事件
        mChannelDosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mChannelButtonHandle(mChannelDosButton);
            }
        });

        //开始按钮事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    JSONObject jo 		= new JSONObject();
                    JSONObject obj 		= new JSONObject();
                    PrefSingleton.getInstance().Initialize(getContext());
                    int gId 			= PrefSingleton.getInstance().getInt("id");
                    PrefSingleton.getInstance().putInt("id", gId + 1);
                    obj.put("id", gId); // 1-1
                    JSONObject param	= new JSONObject(); // 2
                    JSONArray channels	= new JSONArray();
                    JSONArray wlist		= new JSONArray();
                    JSONArray blist 	= new JSONArray();
                    param.put("action", "mdk"); // 2-1

                    if (channelDosButton.getText().toString().equals("选择目标频段") == false) {
                        jo.put("type", "single");
                        jo.put("detail", new Integer(channelId).toString());
                        channels.put(channelId);
                    } else if (mChannelDosButton.getText().toString().equals("选择多目标频段") == false) {
                        jo.put("type", "multi");
                        jo.put("detail", channelSelected);
                        String[] channelIDArr = channelSelected.split(",");
                        for (int i = 0; i < channelIDArr.length; i++) {
                            channels.put(Integer.parseInt(channelIDArr[i]));
                        }
                    } else if (apDosButton.getText().toString().equals("选择目标热点") == false) {
                        jo.put("type", "ap");
                        jo.put("detail", bssid);
                        blist.put(bssid);
                        channels.put(channel);
                        flag = true;
                        writeFile("DosFlag.txt",String.valueOf(flag),bssid);
                    }
                    else {
                        Toast.makeText(getContext(), "请选择目标热点或频段！", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    param.put("channels", channels); // 2-3
                    param.put("wlist", wlist); // 2-4
                    param.put("blist", blist); // 2-5
                    param.put("interval", 1.5);
                    obj.put("param", param);
                    jo.put("data", obj);

                    final JSONObject jof = jo;

                    DevStatusDBUtils devStatusDBUtils 	= new DevStatusDBUtils(context);
                    devStatusDBUtils.open();
                    final String devId 					= PrefSingleton.getInstance().getString("device");//获取设备ID
                    devStatusDBUtils.preHandling(devId);
                    devStatusDBUtils.close();
                    BackgroundTask.clearAll();
                    BackgroundTask.mTimerHandling       = new Timer();
                    if (getActivity() == null){ //由于当线程结束时activity变得不可见,getActivity()有可能为空，需要提前判断
                        return;
                    }
                    BackgroundTask.mTimerTaskHandling   = new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {  /*在fragment中，不能直接用runOnUiThread，需要加getActivity，因为它是activity中的方法*/
                                @Override
                                public void run() {
                                    new DosUpdater(getContext(), devId, jof,null,false,null).execute();//开始阻断
                                }
                            });
                        }
                    };
                    BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 30000);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    //写文件 用于区分显示攻击的客户端
    private void writeFile(String fileName, String flag, String bssid){
        try {
            FileOutputStream fileOutputStream = getActivity().openFileOutput(fileName,MODE_PRIVATE);/*在fragment中没有openFileOutput，因为它是activity中的方法*/
            fileOutputStream.write(flag.getBytes());
            fileOutputStream.write("\n".getBytes());//写入换行
            fileOutputStream.write(bssid.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //选择热点事件
    private void apDosButtonHandle(final Button apDosButton, final List<WiFiDetail> wiFiDetails) {
        final String[] strings = new String[wiFiDetails.size()];
        for (int i = 0;i<wiFiDetails.size();i++){
            strings[i] = wiFiDetails.get(i).getSSID() + " 信道："+ wiFiDetails.get(i).getWiFiSignal().getChannel();
        }
        new AlertDialog.Builder(getContext())
                .setTitle("选择热点")
                .setSingleChoiceItems(strings, -1,new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        dosSsid = wiFiDetails.get(arg1).getSSID();
                        bssid = wiFiDetails.get(arg1).getBSSID();
                        channel = Integer.parseInt(wiFiDetails.get(arg1).getWiFiSignal().getChannel());
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    private int index; // 表示选项的索引
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        index = which;
                        dialog.dismiss();
                        apDosButton.setText(dosSsid);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }

    //单频段事件
    private void channelButtonHandle(final Button tmpButton) {
        final String[] channelString = { "频道1","频道2","频道3","频道4","频道5","频道6","频道7","频道8","频道9","频道10","频道11","频道12","频道13","频道14",
                "频道36","频道38","频道40","频道42","频道44","频道46","频道48","频道52","频道56","频道60","频道64","频道149","频道153","频道157","频道161","频道165"};
        new AlertDialog.Builder(getContext())
                .setTitle("选择频道")
                .setSingleChoiceItems(channelString, 0,
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                if (arg1 >= 0) {
                                    channelId = Integer.parseInt(channelString[arg1].replace("频道", ""));//arg1 + 1;
                                }
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        tmpButton.setText("所选频段为：" + channelId);
                        mChannelDosButton.setText("选择多目标频段");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }
    //多频段事件
    private void mChannelButtonHandle(final Button button) {
        //final String[] channelString = { "频道1", "频道2", "频道3", "频道4", "频道5",
        //		"频道6", "频道7", "频道8", "频道9", "频道10", "频道11" };
        final String[] channelString    = { "频道1","频道2","频道3","频道4","频道5","频道6","频道7","频道8","频道9","频道10","频道11","频道12","频道13","频道14",
                "频道36","频道38","频道40","频道42","频道44","频道46","频道48","频道52","频道56","频道60","频道64","频道149","频道153","频道157","频道161","频道165"};
        final List<Integer> listInteger = new ArrayList();
        new AlertDialog.Builder(getContext())
                .setTitle("选择多频道")
                .setMultiChoiceItems(channelString, null,
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1,
                                                boolean arg2) {
                                // TODO Auto-generated method stub
                                if (arg2) {
                                    listInteger.add(Integer.parseInt(channelString[arg1].replace("频道","")));
                                }
                                else {
                                    Iterator<Integer> ii = listInteger.iterator();
                                    while(ii.hasNext()){
                                        Integer e = ii.next();
                                        if(e.equals(Integer.parseInt(channelString[arg1].replace("频道","")))){
                                            ii.remove();
                                        }
                                    }
                                }
                            }
                        })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        channelSelected = "";
                        Collections.sort(listInteger);
                        int count       = listInteger.size();
                        for (int i = 0; i < count; i++) {
                            if (channelSelected.equals("")) {
                                channelSelected = channelSelected + listInteger.get(i).toString();
                            } else {
                                channelSelected = channelSelected + "," + listInteger.get(i).toString();
                            }
                        }
                        if (count == 0) {
                            dialog.dismiss();
                            button.setText("选择多目标频段");
                            apDosButton.setEnabled(true);
                            channelDosButton.setEnabled(true);
                            Toast.makeText(getContext(), "未选择频段！",
                                    Toast.LENGTH_SHORT).show();
                        } else if(count <= 3){
                            dialog.dismiss();
                            button.setText("所选频段为：" + channelSelected);
                            channelDosButton.setText("选择目标频段");
                        }else {
                            Toast.makeText(getContext(), "请选择少于等于三个频段！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }

}
