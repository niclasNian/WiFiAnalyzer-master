package com.vrem.wifianalyzer.wifi.common;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.dosClientModel.DosGroupClientModel;

import java.util.List;

/**
 * Created by ZhenShiJie on 2018/4/25.
 */

public class DosClientExpandableAdapter extends BaseExpandableListAdapter {


    private List<DosGroupClientModel> dosGroupClientModels;

    public DosClientExpandableAdapter(List<DosGroupClientModel> dosGroupClientModels){
        this.dosGroupClientModels = dosGroupClientModels;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return dosGroupClientModels.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (!(dosGroupClientModels.get(groupPosition).getDosChildClientModelList() ==null)){ //避免子项为空时报NULL异常
            return dosGroupClientModels.get(groupPosition).getDosChildClientModelList().size();
        }else{
            Toast.makeText(MainContext.INSTANCE.getContext(), "沒有客戶端！", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dosGroupClientModels.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView     = View.inflate(MainContext.INSTANCE.getContext(),R.layout.dos_client_group,null);
        }
        TextView tv_group   = convertView.findViewById(R.id.tv_group);
        TextView tv_count   = convertView.findViewById(R.id.count_data);
        TextView tv_rx      = convertView.findViewById(R.id.rx_data);
        TextView tv_tx      = convertView.findViewById(R.id.tx_data);

        tv_group.setText(dosGroupClientModels.get(groupPosition).getGroup_bssid());
        tv_count.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getGroup_count()));
        tv_rx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getGroup_tx_datas()));//没毛病
        tv_tx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getGroup_rx_datas()));
        notifyDataSetChanged();
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView     = View.inflate(MainContext.INSTANCE.getContext(),R.layout.dos_client_child,null);
        }
        TextView tv_group   = convertView.findViewById(R.id.tv_child_group);
        TextView tv_count   = convertView.findViewById(R.id.child_count_data);
        TextView tv_rx      = convertView.findViewById(R.id.rx_child_data);
        TextView tv_tx      = convertView.findViewById(R.id.tx_child_data);

        tv_group.setText(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_bssid());
        tv_count.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_count()));
        tv_rx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_tx_datas()));//没毛病
        tv_tx.setText(String.valueOf(dosGroupClientModels.get(groupPosition).getDosChildClientModelList().get(childPosition).getChild_rx_datas()));

        notifyDataSetChanged();
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
