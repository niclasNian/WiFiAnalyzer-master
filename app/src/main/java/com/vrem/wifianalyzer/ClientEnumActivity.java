package com.vrem.wifianalyzer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vrem.wifianalyzer.wifi.common.PrefSingleton;
import com.vrem.wifianalyzer.wifi.model.ClientInfo;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientEnumActivity extends Activity {

	private DrawerLayout mDrawerLayout;
	private RelativeLayout mLeftContainer;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mPageTitles;
	private String[] mDrawerItems;

	private Button cancelButton;
	private Button apEnumButton;
	private Button allEnumButton;

	private String clientSSID;

	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clientenum);

		cancelButton = (Button) findViewById(R.id.cancelButton);//取消按钮
		apEnumButton = (Button) findViewById(R.id.apenum);//指定枚举热点按钮
		allEnumButton = (Button) findViewById(R.id.allenum);//枚举所有热点按钮
		final String devId = PrefSingleton.getInstance().getString("device");//获取设备ID
		final List<WiFiDetail> wiFiDetailList =MainContext.INSTANCE.getScannerService().getWiFiData().getWiFiDetails();

		apEnumButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				apenumButtonHandle(apEnumButton,wiFiDetailList);//热点列表
			}
		});
		allEnumButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				allEnumButtonHandle(allEnumButton,wiFiDetailList);
			}
		});
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_right_out);
			}
		});

	}

	//枚举所有热点客户端
	private void allEnumButtonHandle(Button allEnumButton, List<WiFiDetail> wiFiDetailList) {
		View view = ClientEnumActivity.this.getLayoutInflater().inflate(R.layout.client_list_dialog, null);
		Dialog dialog = new Dialog(ClientEnumActivity.this);
		dialog.setContentView(view);
		dialog.setTitle("客户端列表");
		dialog.show();
		ListView listview = (ListView)view.findViewById(R.id.client_list);
		TextView noData = (TextView )view.findViewById(R.id.nodata);
		try {
			ClientInfo.setAllClientInfo(ClientEnumActivity.this, wiFiDetailList, listview, noData);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
}
	}

	//枚举单条热点客户端
	private void apenumButtonHandle(final Button apEnumButton, final List<WiFiDetail> wiFiDetails) {
		final String[] strings = new String[wiFiDetails.size()];
		for (int i = 0;i<wiFiDetails.size();i++){
			strings[i] = wiFiDetails.get(i).getSSID();
		}
		new AlertDialog.Builder(ClientEnumActivity.this)
				.setTitle("点击热点")
				.setSingleChoiceItems(strings, 0,new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						WiFiDetail wiFiDetail = wiFiDetails.get(arg1);//获取某条wifi
						View view = ClientEnumActivity.this.getLayoutInflater().inflate(R.layout.client_list_dialog, null);
						Dialog dialog = new Dialog(ClientEnumActivity.this);
						dialog.setContentView(view);
						dialog.setTitle("客户端列表");
						dialog.show();
						ListView listview = (ListView)view.findViewById(R.id.client_list);
						TextView noData = (TextView )view.findViewById(R.id.nodata);
						try {
							ClientInfo.setClientInfo(ClientEnumActivity.this, wiFiDetail, listview, noData);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).show();
	}

	
}
