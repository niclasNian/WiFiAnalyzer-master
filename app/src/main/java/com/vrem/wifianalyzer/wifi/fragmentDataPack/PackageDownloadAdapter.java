package com.vrem.wifianalyzer.wifi.fragmentDataPack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;

import java.util.List;

public class PackageDownloadAdapter extends BaseAdapter {

    private Context context;
    private List<PackageInfo> listItems;
    private LayoutInflater listContainer;
    private int itemViewResource;
    private ProgressBar progressBar;
    static class ListItemView{
        public TextView mac;
        public TextView ssid;
        public Button download;
        public ImageView imageView;
    }

    public PackageDownloadAdapter(Context context, List<PackageInfo> data, int resource, ProgressBar progressBar){
        this.context = context;
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = resource;
        this.listItems = data;
        this.progressBar = progressBar;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemView listItemView = null;
        if(convertView == null){

            convertView = listContainer.inflate(this.itemViewResource, null);
            listItemView = new ListItemView();
            listItemView.ssid       = convertView.findViewById(R.id.pack_ssid_tv);
            listItemView.mac        = convertView.findViewById(R.id.pack_mac_tv);
            listItemView.download   = convertView.findViewById(R.id.download_btn);
            listItemView.imageView  = convertView.findViewById(R.id.pack_image);
            convertView.setTag(listItemView);
        }else{
            listItemView = (ListItemView) convertView.getTag();

        }
        final PackageInfo packageInfo = listItems.get(position);
        listItemView.ssid.setTag(packageInfo);
        if(packageInfo.getSsid().equals(""))
            listItemView.ssid.setText("ssid未获取");
        else
            listItemView.ssid.setText(packageInfo.getSsid());

        if(packageInfo.getMac().equals(""))
            listItemView.mac.setText("ssid未获取");
        else
            listItemView.mac.setText(packageInfo.getMac());
            listItemView.imageView.setBackgroundResource(R.drawable.ic_location_on_sniffer_500_48dp);
        listItemView.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://"+packageInfo.getDownloadUrl());
                intent.setData(content_url);
                context.startActivity(intent);

            }
        });
        return convertView;
    }
}
