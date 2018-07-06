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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.Security;
import com.vrem.wifianalyzer.wifi.model.Strength;
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

//接入点详情
public class AccessPointDetail {
    private static final int VENDOR_SHORT_MAX = 12;
    private static final int VENDOR_LONG_MAX = 30;

    //制作视图，调用下面的makeView方法
    View makeView(View convertView, ViewGroup parent, @NonNull WiFiDetail wiFiDetail, boolean isChild) {
        AccessPointViewType accessPointViewType = MainContext.INSTANCE.getSettings().getAccessPointView();//设置页面
        return makeView(convertView, parent, wiFiDetail, isChild, accessPointViewType);
    }

    //设置布局信息
    View makeView(View convertView, ViewGroup parent, @NonNull WiFiDetail wiFiDetail, boolean isChild, @NonNull AccessPointViewType accessPointViewType) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = MainContext.INSTANCE.getLayoutInflater();//获取LayoutInflater 实例
            view = layoutInflater.inflate(accessPointViewType.getLayout(), parent, false);//通过LayoutInflater加载布局
        }
        setViewCompact(view, wiFiDetail, isChild);//设置压缩视图
        if (view.findViewById(R.id.capabilities) != null) {
            setViewExtra(view, wiFiDetail);
            setViewVendorShort(view, wiFiDetail.getWiFiAdditional());
        }

        return view;
    }

    public View makeViewDetailed(@NonNull WiFiDetail wiFiDetail) {
        View view = MainContext.INSTANCE.getLayoutInflater().inflate(R.layout.access_point_view_popup, null);

        setViewCompact(view, wiFiDetail, false);
        setViewExtra(view, wiFiDetail);
        setViewVendorLong(view, wiFiDetail.getWiFiAdditional());

        return view;
    }

    //设置压缩视图 view传入的页面 WiFiDetail 传入的单条wifi记录 isChild 判断是否是子项
    private void setViewCompact(@NonNull View view, @NonNull WiFiDetail wiFiDetail, boolean isChild) {
        Context context = view.getContext();

        ((TextView) view.findViewById(R.id.ssid)).setText(wiFiDetail.getTitle());//设置SSID and BSSID

        WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();//获取这条wifi信号对象
        Strength strength = wiFiSignal.getStrength();//获取这条wifi的信号强度等级 Strength：枚举类，枚举信号强度等级

        Security security = wiFiDetail.getSecurity();//获取这条wifi的信号安全等级 Security：枚举类，枚举信号安全等级
        ImageView securityImage = view.findViewById(R.id.securityImage);//绑定安全等级图片控件
        securityImage.setImageResource(security.getImageResource());//设置安全等级图片
        securityImage.setColorFilter(ContextCompat.getColor(context, R.color.icons_color));//设置安全等级图片颜色

        TextView textLevel = view.findViewById(R.id.level);//绑定dBm值控件
        textLevel.setText(String.format(Locale.ENGLISH, "%ddBm", wiFiSignal.getLevel()));//设置dBm值
        textLevel.setTextColor(ContextCompat.getColor(context, strength.colorResource()));//设置dBm值颜色

        ((TextView) view.findViewById(R.id.channel)).setText(wiFiSignal.getChannelDisplay());//设置信道
        ((TextView) view.findViewById(R.id.primaryFrequency))
            .setText(String.format(Locale.ENGLISH, "%d%s", wiFiSignal.getPrimaryFrequency(), WiFiSignal.FREQUENCY_UNITS));//设置主频率
        ((TextView) view.findViewById(R.id.distance))
            .setText(String.format(Locale.ENGLISH, "%5.1fm", wiFiSignal.getDistance()));/*设置距离*/
        if (isChild) {
            view.findViewById(R.id.tab).setVisibility(View.VISIBLE);//如果是子项，则设置可见
        } else {
            view.findViewById(R.id.tab).setVisibility(View.GONE);//否则隐藏
        }
    }

    //设置页面信息
    private void setViewExtra(@NonNull View view, @NonNull WiFiDetail wiFiDetail) {
        Context context = view.getContext();

        ImageView configuredImage = view.findViewById(R.id.configuredImage);//笑脸图片
        WiFiAdditional wiFiAdditional = wiFiDetail.getWiFiAdditional();//获取到网络的额外信息
        if (wiFiAdditional.isConfiguredNetwork()) {//网络配置为true
            configuredImage.setVisibility(View.VISIBLE);//笑脸设为可见
            configuredImage.setColorFilter(ContextCompat.getColor(context, R.color.connected));//设置笑脸颜色
        } else {
            configuredImage.setVisibility(View.GONE);//隐藏笑脸
        }

        WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();//获取到wifi信号
        Strength strength = wiFiSignal.getStrength();//获取到wifi信号强度
        ImageView imageView = view.findViewById(R.id.levelImage);//绑定图片控件
        imageView.setImageResource(strength.imageResource());//设置wifi信号强度
        imageView.setColorFilter(ContextCompat.getColor(context, strength.colorResource()));//设置wifi信号强度颜色

        ((TextView) view.findViewById(R.id.channel_frequency_range))
            .setText(Integer.toString(wiFiSignal.getFrequencyStart()) + " - " + Integer.toString(wiFiSignal.getFrequencyEnd()));//设置wifi频率：xxxx-xxxx
        ((TextView) view.findViewById(R.id.width))
            .setText("(" + Integer.toString(wiFiSignal.getWiFiWidth().getFrequencyWidth()) + WiFiSignal.FREQUENCY_UNITS + ")");//设置wifi宽度
        ((TextView) view.findViewById(R.id.capabilities))
            .setText(wiFiDetail.getCapabilities());//设置wifi加密方式
    }

    //设置供应商
    private void setViewVendorShort(@NonNull View view, @NonNull WiFiAdditional wiFiAdditional) {
        TextView textVendorShort = view.findViewById(R.id.vendorShort);
        String vendor = wiFiAdditional.getVendorName();
        if (StringUtils.isBlank(vendor)) {
            textVendorShort.setVisibility(View.GONE);
        } else {
            textVendorShort.setVisibility(View.VISIBLE);
            textVendorShort.setText(vendor.substring(0, Math.min(VENDOR_SHORT_MAX, vendor.length())));
        }
    }

    private void setViewVendorLong(@NonNull View view, @NonNull WiFiAdditional wiFiAdditional) {
        TextView textVendor = view.findViewById(R.id.vendorLong);
        String vendor = wiFiAdditional.getVendorName();
        if (StringUtils.isBlank(vendor)) {
            textVendor.setVisibility(View.GONE);
        } else {
            textVendor.setVisibility(View.VISIBLE);
            textVendor.setText(vendor.substring(0, Math.min(VENDOR_LONG_MAX, vendor.length())));
        }
    }

}
