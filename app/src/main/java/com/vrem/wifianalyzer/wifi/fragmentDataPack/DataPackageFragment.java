package com.vrem.wifianalyzer.wifi.fragmentDataPack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.PrefSingleton;

import org.json.JSONException;

import java.util.List;

public class DataPackageFragment extends Fragment {

    private View view;
    private ProgressBar progressBar;
    private TextView noData;
    private TextView clickRefresh;
    private ListView dataPackListView;
    private PackageDownloadAdapter downloadAdapter;
    private List<PackageInfo> listItems;
    private PackageInfo packageInfo;
    private Button dataPackRefreshBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (view == null){
            view = inflater.inflate(R.layout.fragment_data_pack_info,container,false);
            progressBar = view.findViewById(R.id.progressbar);
            noData      = view.findViewById(R.id.nodata);
            clickRefresh = view.findViewById(R.id.data_clickrefresh);
            dataPackListView = view.findViewById(R.id.data_pack_listview);
            dataPackRefreshBtn = view.findViewById(R.id.data_pack_refresh_btn);
            try {
                PackageInfo.setPackageInfo(getContext(),PrefSingleton.getInstance().getString("device"),dataPackListView,progressBar,clickRefresh,noData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            downloadAdapter = new PackageDownloadAdapter(getContext(),listItems,R.layout.data_pack_listitem,progressBar);
        }else {
            return view;
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataPackRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PackageInfo.setPackageInfo(getContext(),PrefSingleton.getInstance().getString("device"),dataPackListView,progressBar,clickRefresh,noData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
