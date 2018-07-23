package com.vrem.wifianalyzer.wifi.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

//数据存储类
public class PrefSingleton {
    private static PrefSingleton mInstance;
    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private PrefSingleton(){}

    public static PrefSingleton getInstance(){
        if (mInstance == null) {
            mInstance = new PrefSingleton();
        }
        return mInstance;
    }

    public void Initialize(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public int getInt(String key) {
        return mSharedPreferences.getInt(key, -1);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public String getString(String key) {
        Map<String, ?> str = mSharedPreferences.getAll();
        return mSharedPreferences.getString(key, "");
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    //移除存储类的中数据
    public void remove(String key){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }
}
