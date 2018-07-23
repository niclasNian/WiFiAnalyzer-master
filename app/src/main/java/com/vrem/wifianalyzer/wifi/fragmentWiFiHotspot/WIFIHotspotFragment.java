package com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot.httpServer.HTTPServer;
import com.vrem.wifianalyzer.wifi.model.WiFiData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WIFIHotspotFragment extends Fragment {

    private Button openHotspotBtn;
    private Button closeHotspotBtn;
    private EditText hotspotNname;
    private EditText password;
    private WifiManager wifiManager;
    private String strHotspotName;
    private String strPassword;
    private ListView linkInfoLv;//ap连接信息列表
    private List<String> promptList;//提示信息
    private ApLinkInfoAdapter listAdapter; //ap连接适配器
//    public static ServerSocket serverSocket = null;
//    private String buffer = "";

    private HTTPServer server;//web服务
//    private JSONObject infoJson;//设备回传信息

    //负责与子线程通信，原因是子线不能更新UI
    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what==0x11) {
//                Bundle bundle = msg.getData();
//                mTextView.append("client"+bundle.getString("msg")+"\n");
//                Toast.makeText(MainContext.INSTANCE.getContext(),"client"+bundle.getString("msg"),Toast.LENGTH_LONG).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view        = inflater.inflate(R.layout.fragment_wifi_hotspot,container,false);
        openHotspotBtn   = view.findViewById(R.id.open_hotspot_btn);
        closeHotspotBtn  = view.findViewById(R.id.close_hotspot_btn);
        hotspotNname     = view.findViewById(R.id.hotspot_name);
        password         = view.findViewById(R.id.hotspot_password);
        linkInfoLv       = view.findViewById(R.id.link_info_list_view);
        promptList       = new ArrayList<>();
        listAdapter      = new ApLinkInfoAdapter(getContext(),promptList);
        wifiManager      = (WifiManager) MainContext.INSTANCE.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        server           = new HTTPServer(mHandler,listAdapter,promptList);
        server.asset_mgr = MainContext.INSTANCE.getContext().getAssets();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //打开热点事件
        openHotspotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strHotspotName = hotspotNname.getText().toString();
                strPassword     = password.getText().toString();
                try {
                    server.start();//启动web服务
                    Log.d("httpServer","start");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                WiFiData data = MainContext.INSTANCE.getScannerService().getWiFiData();
                if (data.getWiFiDetails().size() == 0){ //如果用户第一次启动app,直接启动ap模式的话
//                    createWifiHotspot(strHotspotName,strPassword);
                    createWifiHotspot8(getContext(),true);
                }else {
                    new InfoUpdater(getContext(),true,true,"the","123456789").execute();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                promptList.add("热点已开启，等待客户端连接");

                linkInfoLv.setAdapter(listAdapter);
            }
        });

        //关闭热点事件
        closeHotspotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                closeWifiHotspot();
                createWifiHotspot8(getContext(),false);
                MainContext.INSTANCE.getScannerService().resume();
                Log.d("wifiScanStatus","resume");
                server.stop();
                Log.d("hotspotStatus","Close");
                promptList.add("热点已关闭");
                listAdapter.notifyDataSetChanged();
            }
        });

        listAdapter.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * android 6.0 ap create
     */
    public void createWifiHotspot(String name,String psw) {
        MainContext.INSTANCE.getScannerService().pause();//打开热点时暂停wifi扫描
        WifiManager wifiManager = (WifiManager) MainContext.INSTANCE.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi,
            wifiManager.setWifiEnabled(false);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = name;
        config.preSharedKey = psw;
        config.hiddenSSID = false;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);//公认的IEEE 802.11认证算法 用来判断加密方法。OPEN 开发系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);//公认的组密码 获取使用GroupCipher 的方法来进行加密。
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);//公认的密钥管理方案
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);//公认的WPA配对密码 获取使用WPA 方式的加密。
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;// 网络配置的可能状态  获取当前网络的状态。
        //通过反射调用设置热点
        try {
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(wifiManager, config, true);
            if (enable) {
//                Toast.makeText(getContext(),"热点已开启 SSID:" +name+ " password:"+psw+"",Toast.LENGTH_LONG).show();
                Log.d("","热点已开启 SSID:" +name+ " password:"+psw+"");
            } else {
//                Toast.makeText(getContext(),"创建热点失败",Toast.LENGTH_LONG).show();
                Log.d("","创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(getContext(),"创建热点失败",Toast.LENGTH_LONG).show();
            Log.d("","创建热点失败");
        }
    }

    /**
     * 关闭WiFi热点 android 6.0
     */
    public void closeWifiHotspot() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        Toast.makeText(getContext(),"热点已关闭",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null){
            // 在程序退出时关闭web服务器
            server.stop();
        }
        Log.w("Httpd", "The server stopped.");
    }

    /*暂用*/
    @TargetApi(Build.VERSION_CODES.O)
    public void createWifiHotspot8(Context context,boolean isEnable){
        MainContext.INSTANCE.getScannerService().pause();//暂停wifi扫描
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        Field iConnmgrField = null;
        try {
            iConnmgrField = connectivityManager.getClass().getDeclaredField("mService");
            iConnmgrField.setAccessible(true);
            Object iConMgr = iConnmgrField.get(connectivityManager);
            Class<?> iConnMgrClass = Class.forName(iConMgr.getClass().getName());
            if (isEnable){
                Method startTethering = iConnMgrClass.getMethod("startTethering",int.class, ResultReceiver.class,boolean.class);
                startTethering.invoke(iConMgr,0,null,true);
            }else {
                Method stop = iConnMgrClass.getMethod("stopTethering",int.class);
                stop.invoke(iConMgr,0);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
