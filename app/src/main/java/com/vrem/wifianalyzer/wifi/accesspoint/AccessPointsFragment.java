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

package com.vrem.wifianalyzer.wifi.accesspoint;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.WifiInfoActivity;
import com.vrem.wifianalyzer.wifi.fragmentDos.DosFragment;
import com.vrem.wifianalyzer.wifi.fragmentSniffer.SnifferFragment;
import com.vrem.wifianalyzer.wifi.fragmentWpsCrack.WpsCrackFragment;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.ArrayList;

public class AccessPointsFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private AccessPointsAdapter accessPointsAdapter;

    private Vibrator vibrator;
    private FragmentManager manager;//Fragment管理器

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        manager = this.getFragmentManager();
        View view = inflater.inflate(R.layout.access_points_content, container, false);//绑定页面

        swipeRefreshLayout = view.findViewById(R.id.accessPointsRefresh);//绑定控件
        swipeRefreshLayout.setOnRefreshListener(new ListViewOnRefreshListener());//设置监听事件


        accessPointsAdapter = new AccessPointsAdapter(getActivity());//获取AccessPointsAdapter的全局数据
        final ExpandableListView expandableListView = view.findViewById(R.id.accessPointsView);//绑定ExpandableListView控件
        expandableListView.setAdapter(accessPointsAdapter);//给expandableListView绑定适配器
        accessPointsAdapter.setExpandableListView(expandableListView);//将数据填充到ExpandableListView
        expandableListView.setVisibility(View.VISIBLE);

//        manager = this.getFragmentManager();

        //点击事件
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                WiFiDetail detail = (WiFiDetail) accessPointsAdapter.getGroup(groupPosition);//获取某条数据
                Intent intent = new Intent(getActivity(),WifiInfoActivity.class);//实例化Intent对象，用于页面跳转，数据传递
                intent.putExtra("wifiDetail",new Gson().toJson(detail));//将数据添加到Intent，对象类不能直接传递，需要用Gson对它进行转换
                startActivity(intent);//启动
                return true;
            }
        });

        //长按事件
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final WiFiDetail detail = (WiFiDetail) accessPointsAdapter.getGroup(position);//获取某条数据
                vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(30);//设置震动
                View view1 = getActivity().getLayoutInflater().inflate(R.layout.context_menu, null);
                ArrayList<String> list = new ArrayList<String>();
                list.add("热点定向阻断");
                list.add("握手包截获");
                list.add("Wps破解");
                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(view1);
                Window dialogWindow = dialog.getWindow();
                WindowManager wm = getActivity().getWindowManager();
                Display display = wm.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.width = (display.getWidth() * 2 / 3);
                dialogWindow.setAttributes(lp);
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                RelativeLayout contextLayout = view1.findViewById(R.id.context_menu);
//                contextLayout.setBackgroundResource(R.drawable.contextbg3);
                final ListView listView = view1.findViewById(R.id.listview);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.context_menu_listitem, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {
                            switch (arg2) {
                                case 0:
                                    DosFragment dosFragment = new DosFragment(); //声明Fragment对象
                                    Bundle dosBundle = new Bundle();//声明bundle类，用于传参
                                    dosBundle.putString("ssid",detail.getSSID());
                                    dosBundle.putString("bssid",detail.getBSSID());
                                    dosBundle.putString("channel",detail.getWiFiSignal().getChannel());
                                    dosFragment.setArguments(dosBundle);//给dosFragment设置参数
                                    FragmentTransaction transaction;//Fragment事务
                                    transaction = manager.beginTransaction();//开启事务
                                    transaction.replace(R.id.main_fragment,dosFragment);//添加事务这是错的，因为已经有一个事务存在了，要替换才对replace
                                    transaction.commit();//提交事务
                                    dialog.dismiss();
                                    break;
                                case 1:
                                    SnifferFragment snifferFragment = new SnifferFragment();
                                    Bundle snifBundle = new Bundle();
                                    snifBundle.putString("ssid",detail.getSSID());
                                    snifBundle.putString("bssid",detail.getBSSID());
                                    snifBundle.putInt("channel",Integer.parseInt(detail.getWiFiSignal().getChannel()));
                                    snifBundle.putDouble("rate",detail.getRate());
                                    snifferFragment.setArguments(snifBundle);
                                    FragmentTransaction snifferFT;
                                    snifferFT = manager.beginTransaction();
                                    snifferFT.replace(R.id.main_fragment,snifferFragment);//添加事务这是错的，因为已经有一个事务存在了，要替换才对replace
                                    snifferFT.commit();//提交事务
                                    dialog.dismiss();
                                    break;
                                case 2:
                                    WpsCrackFragment wpsCrackFragment = new WpsCrackFragment();
                                    FragmentTransaction wpsFT;
                                    Bundle wpsBundle = new Bundle();
                                    wpsBundle.putString("ssid",detail.getSSID());
                                    wpsBundle.putString("bssid",detail.getBSSID());
                                    wpsBundle.putString("channel",detail.getWiFiSignal().getChannel());
                                    wpsCrackFragment.setArguments(wpsBundle);
                                    wpsFT = manager.beginTransaction();
                                    wpsFT.replace(R.id.main_fragment,wpsCrackFragment);//添加事务这是错的，因为已经有一个事务存在了，要替s换才对replace
                                    wpsFT.commit();//提交事务
                                    dialog.dismiss();
                                    break;
                                default:
                                    break;
                            }
                    }
                });
                return true;
            }
        });

        MainContext.INSTANCE.getScannerService().register(accessPointsAdapter);
        if (MainContext.INSTANCE.getScannerService().isRunning() == false){
            MainContext.INSTANCE.getScannerService().resume();
        }
        return view;
    }


    //下拉刷新
    private void refresh() {

        swipeRefreshLayout.setRefreshing(true);
        MainContext.INSTANCE.getScannerService().update();//执行刷新服务
        swipeRefreshLayout.setRefreshing(false);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onResume() {
        Log.w("AccessPointsFragment status","Resume");
        super.onResume();
        refresh();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onPause() {
        Log.w("AccessPointsFragment status","Pause");
        super.onPause();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onDestroy() {
        Log.w("AccessPointsFragment status","Destroy");
        MainContext.INSTANCE.getScannerService().unregister(accessPointsAdapter);
        super.onDestroy();
    }

    AccessPointsAdapter getAccessPointsAdapter() {
        return accessPointsAdapter;
    }



    //设置SwipeRefreshLayout监听事件具体操作 下拉刷新
    private class ListViewOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }
}
