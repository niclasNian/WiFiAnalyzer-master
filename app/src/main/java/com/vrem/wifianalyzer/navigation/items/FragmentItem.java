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

package com.vrem.wifianalyzer.navigation.items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.navigation.NavigationMenu;
import com.vrem.wifianalyzer.navigation.NavigationMenuView;

import java.io.Serializable;

public class FragmentItem implements NavigationItem {
    private final Fragment fragment;
    private final boolean registered;

    public FragmentItem(@NonNull Fragment fragment, boolean registered) {
        this.fragment = fragment;
        this.registered = registered;
    }

    FragmentItem(@NonNull Fragment fragment) {
        this(fragment, false);
    }

    @Override
    public void activate(@NonNull MainActivity mainActivity, @NonNull MenuItem menuItem, @NonNull NavigationMenu navigationMenu) {
        Bundle bundle = new Bundle();
        NavigationMenuView navigationMenuView = mainActivity.getNavigationMenuView();
        navigationMenuView.setCurrentNavigationMenu(navigationMenu);
        bundle.putInt("id",2);//用来标识，因为在导航菜单也有数据传过去
//        bundle.putSerializable("wifiDetails",new Gson().toJson(MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails()));
        fragment.setArguments(bundle);
        startFragment(mainActivity);//启动用户选择的fragment
        mainActivity.setTitle(menuItem.getTitle());
        mainActivity.updateActionBar();
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    private void startFragment(@NonNull MainActivity mainActivity) {
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        if (fragment.equals("WIFIHotspotFragment")){
//            fragmentTransaction.add(R.id.main_fragment, fragment).commit();
//        }
        fragmentTransaction.replace(R.id.main_fragment, fragment).commit();
    }

    Fragment getFragment() {
        return fragment;
    }
}
