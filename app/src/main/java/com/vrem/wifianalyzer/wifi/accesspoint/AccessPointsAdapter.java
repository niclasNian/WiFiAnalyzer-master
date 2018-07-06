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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier;

//ExpandableListAdapter数据源
class AccessPointsAdapter extends BaseExpandableListAdapter implements UpdateNotifier {
    private final Context context;
    private AccessPointsAdapterData accessPointsAdapterData;
    private AccessPointDetail accessPointDetail;
    private AccessPointPopup accessPointPopup;
    private ExpandableListView expandableListView;

    AccessPointsAdapter(@NonNull Context context) {
        super();
        this.context = context;
        setAccessPointsAdapterData(new AccessPointsAdapterData());
        setAccessPointDetail(new AccessPointDetail());
        setAccessPointPopup(new AccessPointPopup());
        expandableListView = null;
    }

    //【重要】填充一级列表
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        WiFiDetail wiFiDetail = (WiFiDetail) getGroup(groupPosition);//获取单条wifi数据详情对象
        View view = accessPointDetail.makeView(convertView, parent, wiFiDetail, false);//返回AccessPointDetail制作的页面
        attachPopup(view, wiFiDetail);

        ImageView groupIndicator = view.findViewById(R.id.groupIndicator);//获取图片控件
        int childrenCount = getChildrenCount(groupPosition);
        if (childrenCount > 0) {
            groupIndicator.setVisibility(View.VISIBLE);
            groupIndicator.setImageResource(isExpanded
                    ? R.drawable.ic_expand_less_black_24dp
                    : R.drawable.ic_expand_more_black_24dp);
            groupIndicator.setColorFilter(ContextCompat.getColor(context, R.color.icons_color));
        } else {
            groupIndicator.setVisibility(View.GONE);
        }
        return view;
    }
    //【重要】填充二级列表
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        WiFiDetail wiFiDetail = (WiFiDetail) getChild(groupPosition, childPosition);
        View view = accessPointDetail.makeView(convertView, parent, wiFiDetail, true);
        attachPopup(view, wiFiDetail);
        view.findViewById(R.id.groupIndicator).setVisibility(View.GONE);
        return view;
    }

    @Override
    public void update(@NonNull WiFiData wiFiData) {
        accessPointsAdapterData.update(wiFiData, expandableListView);
        notifyDataSetChanged();
    }

    //返回一级列表的wifi的个数
    @Override
    public int getGroupCount() {
        return accessPointsAdapterData.parentsCount();
    }

    //返回每个二级列表的个数
    @Override
    public int getChildrenCount(int groupPosition) {
        return accessPointsAdapterData.childrenCount(groupPosition);
    }

    //返回一级列表的单个item（返回的是对象）
    @Override
    public Object getGroup(int groupPosition) {
       WiFiDetail detail = accessPointsAdapterData.parent(groupPosition);
        return detail;
    }

    //返回二级列表中的单个item（返回的是对象）
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return accessPointsAdapterData.child(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    ////每个item的id是否是固定？一般为true
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //二级列表中的item是否能够被选中？可以改为true
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        accessPointsAdapterData.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        accessPointsAdapterData.onGroupExpanded(groupPosition);
    }

    void setAccessPointsAdapterData(@NonNull AccessPointsAdapterData accessPointsAdapterData) {
        this.accessPointsAdapterData = accessPointsAdapterData;
    }

    void setAccessPointDetail(@NonNull AccessPointDetail accessPointDetail) {
        this.accessPointDetail = accessPointDetail;
    }

    void setAccessPointPopup(@NonNull AccessPointPopup accessPointPopup) {
        this.accessPointPopup = accessPointPopup;
    }

    void setExpandableListView(ExpandableListView expandableListView) {
        this.expandableListView = expandableListView;
    }

    private void attachPopup(@NonNull View view, @NonNull WiFiDetail wiFiDetail) {
        View popupView = view.findViewById(R.id.attachPopup);
        if (popupView != null) {
            accessPointPopup.attach(popupView, wiFiDetail);
            accessPointPopup.attach(view.findViewById(R.id.ssid), wiFiDetail);
        }
    }

}
