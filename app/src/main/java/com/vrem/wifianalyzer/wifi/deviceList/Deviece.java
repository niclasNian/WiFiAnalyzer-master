package com.vrem.wifianalyzer.wifi.deviceList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.deviceList.adapter.DeviceAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import static com.vrem.wifianalyzer.wifi.model.DeviceInfo.handlingToWorkType;

public class Deviece {
    private final AlertDialog alertDialog; //声明对话框对象
    private static String[] deviceName;//设备名
//    private static int[] image = {R.drawable.wifigreen,R.drawable.wifigreen,R.drawable.wifigreen};
    private static String[] deviceStatus;//设备状态
    private static int[] deviceBtty;//电量

    public Deviece(@NonNull AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }

    public static Deviece build(){
        return new Deviece(buildAlertDialog());
    }

    private static AlertDialog buildAlertDialog() { //设置过滤器的五个参数
        View view = MainContext.INSTANCE.getLayoutInflater().inflate(R.layout.device_list, null); //获取页面对象
        String deviceInfo = PrefSingleton.getInstance().getString("deviceInfo");//获取存储的数据
        PrefSingleton.getInstance().remove("deviceInfo");//及时移除数据，避免二次获取的时候数据冲突
        JSONObject jsonObject = null;
        int workType = 100;
        try {
            jsonObject = new JSONObject(deviceInfo);
            String data = jsonObject.getString("data");
            JSONObject dataJson = new JSONObject(data);
            int battery = dataJson.getInt("battery");
            deviceBtty = new int[]{battery};

            String device = dataJson.getString("device");
            deviceName = new String[]{device};

            DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(MainContext.INSTANCE.getContext());
            devStatusDBUtils.open();
            devStatusDBUtils.tryInsertNewDev(device);
            String handling = devStatusDBUtils.getHandling(device);
            devStatusDBUtils.close();
            workType = handlingToWorkType(handling);
            String status = reDeviceStatus(workType);
            deviceStatus =new String[]{status};
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final DeviceAdapter myAdapter = new DeviceAdapter(MainContext.INSTANCE.getContext(),/*image,*/deviceName,deviceStatus,deviceBtty,workType);
        return new AlertDialog
                .Builder(view.getContext())
                .setTitle("设备列表")
                .setIcon(R.drawable.ic_location_on_forgery_500_48dp)
                .setAdapter(myAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainContext.INSTANCE.getContext(), "我动了"+myAdapter.getItem(which)+"！",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
    }

    public void show() {
        if (!alertDialog.isShowing()) { //如果对话框没有显示
            alertDialog.show();//显示对话框
        }
    }

    private static String reDeviceStatus(int workType){
        if (workType>0){
            if (workType == 100){
                return "空闲";
            }else if (workType == 103){
                return "正在抓包";
            }else if (workType == 104){
                return "正在破解WPS";
            }else if (workType == 105){
                return "正在伪造热点 4G";
            }else if (workType == 101){
                return "正在攻击热点";
            }else if (workType == 201){
                return "正在伪造热点 WIFI";
            }else if (workType == 102){
                return "正在攻击热点信道";
            }else if (workType == 110){
                return "正在攻击多热点";
            }
        }
        return "error";
    }
}
