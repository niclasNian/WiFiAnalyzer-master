/*
 * WiFiAnalyzer
 * Copyright (C) 2018  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.vrem.util.ConfigurationUtils;
import com.vrem.util.EnumUtils;
import com.vrem.wifianalyzer.menu.OptionMenu;
import com.vrem.wifianalyzer.navigation.NavigationMenu;
import com.vrem.wifianalyzer.navigation.NavigationMenuView;
import com.vrem.wifianalyzer.settings.Repository;
import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionView;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.band.WiFiChannel;
import com.vrem.wifianalyzer.wifi.common.InfoUpdater;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;

import java.util.List;
import java.util.Locale;

import static android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;

public class MainActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener, OnNavigationItemSelectedListener {
    private MainReload mainReload;
    private NavigationMenuView navigationMenuView;
    private NavigationMenu startNavigationMenu;
    private OptionMenu optionMenu;
    private String currentCountryCode;

//    private Timer mTimer = new Timer();//定时任务

    //附加基础上下文背景
    @Override
    protected void attachBaseContext(Context newBase) {
        Locale newLocale = new Settings(new Repository(newBase)).getLanguageLocale();//获取语言
        Context context = ConfigurationUtils.createContext(newBase, newLocale);//获取系统配置信息
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity status","Create");
        MainContext mainContext = MainContext.INSTANCE;
        mainContext.initialize(this, isLargeScreen());//调用mainContext 初始化数据

        Settings settings = mainContext.getSettings();//获取设置信息
        settings.initializeDefaultValues();//获取编好设置

        setTheme(settings.getThemeStyle().themeAppCompatStyle());//设置主题风格
        setWiFiChannelPairs(mainContext);//设置WiFi信道组

        mainReload = new MainReload(settings);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

//        new InfoUpdater(this,true);
        PrefSingleton.getInstance().Initialize(getApplicationContext());//初始化参数
        PrefSingleton.getInstance().putString("url", "http://192.168.100.1:9494");
        if (PrefSingleton.getInstance().getInt("id") < 0) {
            PrefSingleton.getInstance().putInt("id", 0);
        }
        new InfoUpdater(this,true).execute();//获取前置信息

        settings.registerOnSharedPreferenceChangeListener(this);

        setOptionMenu(new OptionMenu());//设置菜单

        Toolbar toolbar = findViewById(R.id.toolbar);//获取工具栏控件
        toolbar.setOnClickListener(new WiFiBandToggle());//设置点击事件：切换WiFi频道
        setSupportActionBar(toolbar);//设置操作栏

        DrawerLayout drawer = findViewById(R.id.drawer_layout);//抽屉式布局
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(  //操作栏抽屉式切换
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        startNavigationMenu = settings.getStartMenu();
        navigationMenuView = new NavigationMenuView(this, startNavigationMenu);
        onNavigationItemSelected(navigationMenuView.getCurrentMenuItem());

        ConnectionView connectionView = new ConnectionView(this);//获取连接视图对象
        mainContext.getScannerService().register(connectionView);

    }

    //设置WiFi信道组
    private void setWiFiChannelPairs(MainContext mainContext) {
        Settings settings = mainContext.getSettings();//获取设置信息
        String countryCode = settings.getCountryCode();//获取国家代码
        if (!countryCode.equals(currentCountryCode)) {//当前国家代码不为空
            Pair<WiFiChannel, WiFiChannel> pair = WiFiBand.GHZ5.getWiFiChannels().getWiFiChannelPairFirst(countryCode);//设置第一个WiFi信道组
            mainContext.getConfiguration().setWiFiChannelPair(pair);//将WiFi信道组传给mainContext
            currentCountryCode = countryCode;//将国家编码传递给当前国家编码currentCountryCode
        }
    }

    private boolean isLargeScreen() {
        Configuration configuration = getResources().getConfiguration();//声明Configuration对象，用于获取设备信息
        int screenLayoutSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
            screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    //共享偏好改变
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MainContext mainContext = MainContext.INSTANCE;
        if (mainReload.shouldReload(mainContext.getSettings())) {
            reloadActivity();
        } else {
            setWiFiChannelPairs(mainContext);
            update();
        }
    }

    public void update() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        if (info != null && info.size()>0){
            ComponentName componentName = info.get(0).topActivity;
            String className = componentName.getClassName();
            Log.d("className:" , className);
            if ("com.vrem.wifianalyzer.SnifferActivity".equals(className)){//判断SnifferActivity是否处于打开状态
                Log.w("SnifferActivity:","处于打开状态，不执行更新");
            }else if ("com.vrem.wifianalyzer.FakeAPActivity".equals(className)){
                Log.w("FakeAPActivity:","处于打开状态，不执行更新");
            }
        }else {
            MainContext.INSTANCE.getScannerService().update();
            updateActionBar();
            Log.w("系统:","执行更新");
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    //重新加载页面
    private void reloadActivity() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP |
            Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawer()) {
            if (startNavigationMenu.equals(navigationMenuView.getCurrentNavigationMenu())) {
                super.onBackPressed();
            } else {
                navigationMenuView.setCurrentNavigationMenu(startNavigationMenu);
                onNavigationItemSelected(navigationMenuView.getCurrentMenuItem());
            }
        }
    }

    //导航条动作选择
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        try {
            closeDrawer();//关闭正在打开的页面，为新动作做准备
            NavigationMenu navigationMenu = EnumUtils.find(NavigationMenu.class, menuItem.getItemId(), NavigationMenu.ACCESS_POINTS);
            navigationMenu.activateNavigationMenu(this, menuItem);
        } catch (Exception e) {
            reloadActivity();
        }
        return true;
    }

    //关闭当前页面
    private boolean closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        optionMenu.pause();
        MainContext.INSTANCE.getScannerService().pause();//暂停扫描
        updateActionBar();
        Log.d("MainActivity status","Pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("MainActivity status","Resume");
        super.onResume();
        optionMenu.resume();
        updateActionBar();
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity status","Stop");
        MainContext.INSTANCE.getScannerService().setWiFiOnExit();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionMenu.create(this, menu);
        updateActionBar();
        return true;
    }

    //获取操作栏上的动作选项
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        optionMenu.select(item);
        updateActionBar();//更新操作栏
        return true;
    }

    //更新操作栏
    public void updateActionBar() {
        navigationMenuView.getCurrentNavigationMenu().activateOptions(this);
    }

    //导航菜单视图 每5秒刷新
    public NavigationMenuView getNavigationMenuView() {
        return navigationMenuView;
    }

    public OptionMenu getOptionMenu() {
        return optionMenu;
    }

    void setOptionMenu(@NonNull OptionMenu optionMenu) {
        this.optionMenu = optionMenu;
    }

    //WIFI频道切换
    private class WiFiBandToggle implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (navigationMenuView.getCurrentNavigationMenu().isWiFiBandSwitchable()) {
                MainContext.INSTANCE.getSettings().toggleWiFiBand();
            }
        }
    }

}
