package com.vrem.wifianalyzer.wifi.model;

/**
 * Created by ZhenShiJie on 2018/3/23.
 */

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;

public class ClientListAdapter extends BaseAdapter{
    private Context context;
    private List<ClientInfo> listItems;
    private LayoutInflater listContainer;
    private int itemViewResource;
    static class ListItemView{
        public TextView mac;
        public TextView probe;
        public TextView company;
    }
//
//    public void UpdateData(List<ClientInfo> data) {
//        this.listItems = data;
//    }

    public ClientListAdapter(Context context, List<ClientInfo> data, int resource){
        this.context = context;
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = resource;
        this.listItems = data;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ListItemView listItemView = null;
        if(convertView == null){

            convertView = listContainer.inflate(this.itemViewResource, null);
            listItemView = new ListItemView();
            listItemView.mac = convertView.findViewById(R.id.mac);
            listItemView.probe = convertView.findViewById(R.id.probe);
            listItemView.company = convertView.findViewById(R.id.company);
            convertView.setTag(listItemView);
        }else{
            listItemView = (ListItemView) convertView.getTag();
        }
        final ClientInfo clientInfo = listItems.get(position);
        listItemView.mac.setTag(clientInfo);
        listItemView.mac.setText(clientInfo.getMac());
        if(clientInfo.getProbe().equals(""))
            listItemView.probe.setText("无");
        else{
            listItemView.probe.setText(clientInfo.getProbe());
        }

        listItemView.company.setText(clientInfo.getCompany());
        return convertView;
    }

}