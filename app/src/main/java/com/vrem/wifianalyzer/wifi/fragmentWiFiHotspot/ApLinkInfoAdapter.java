package com.vrem.wifianalyzer.wifi.fragmentWiFiHotspot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.wifianalyzer.R;

import java.util.List;

public class ApLinkInfoAdapter extends BaseAdapter{

//    private String[] str;
    private LayoutInflater layoutInflater;
    private TextView textView;
    public Button button;
    private List<String> stringList;

    public ApLinkInfoAdapter(@NonNull Context context, List<String> stringList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.stringList = stringList;
    }

    @Override
    public int getCount() {
        return stringList.size();
    }

    @Override
    public Object getItem(int position) {
        return stringList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.ap_link_list_item,null);
        textView = convertView.findViewById(R.id.tv);
        button = convertView.findViewById(R.id.btn);
        textView.setText(stringList.get(position));
        if (stringList.get(position).contains("ip")){
            button.setVisibility(View.VISIBLE);
            final View finalConvertView = convertView;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(finalConvertView.getContext(),"我动了"+stringList.get(position)+"",Toast.LENGTH_LONG);
                }
            });
        }
        return convertView;
    }
}
