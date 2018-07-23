package com.vrem.wifianalyzer.wifi.deviceList.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class DeviceAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private String deviceName[];
//    private int image[];
    private String deviceStatus[];
    private int deviceBtty[];
    private int workType;

    public DeviceAdapter(Context context, /*int[] image_Objects, */String[] deviceName, String[] deviceStatus, int[] deviceBtty,int workType){
        this.layoutInflater = LayoutInflater.from(context);
        this.deviceName     = deviceName;
        this.deviceStatus   = deviceStatus;
        this.deviceBtty     = deviceBtty;
        this.workType       = workType;
    }

    @Override
    public int getCount() {
        return deviceName.length;
    }

    @Override
    public Object getItem(int position) {
        return deviceName[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.device_list,null);
        MyClass myClass = new MyClass();
        myClass.imageView = convertView.findViewById(R.id.image_id);
        myClass.nameView  = convertView.findViewById(R.id.name);
        myClass.moodView  = convertView.findViewById(R.id.mood);
        myClass.battyView = convertView.findViewById(R.id.batty);

//        myClass.imageView.setBackgroundResource(image[position]);
        myClass.nameView.setText(deviceName[position]);
        myClass.moodView.setText(deviceStatus[position]);
        myClass.battyView.setText(Integer.toString(deviceBtty[position]));
        myClass.nameView.setTextColor(Color.BLACK);
        myClass.moodView.setTextColor(Color.RED);

        if (deviceBtty[position] <70 && deviceBtty[position]>40){
            myClass.battyView.setTextColor(Color.GRAY);
        }else if (deviceBtty[position]<=40&& deviceBtty[position]>0){
            myClass.battyView.setTextColor(Color.RED);
        }else {
            myClass.battyView.setTextColor(Color.GREEN);
        }
        String deviceStatusFlag = deviceStatus[position];
//        Log.v("deviceStatusFlag:",deviceStatusFlag);

        if (workType == 100){
            myClass.imageView.setBackgroundResource(R.drawable.wifigreen);
        }else{
            myClass.imageView.setBackgroundResource(R.drawable.wifiblue);
        }
        return convertView;
    }

    class MyClass{
        ImageView imageView;
        TextView nameView;
        TextView moodView;
        TextView battyView;
    }
}