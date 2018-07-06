package com.vrem.wifianalyzer.wifi.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;


import java.util.List;

public class APDialogListAdapter extends BaseAdapter {
	private Context context;
	private List<WiFiDetail> listItems;
	private LayoutInflater listContainer;
	private int itemViewResource;
	private Vibrator vibrator;

	static class ListItemView {
		public TextView ssid;
		public ImageView lock;
		public ImageView wps;
		public ImageView wep;
		public ImageView wpa;
		public ImageView channelNumber1;
		public ImageView channelNumber2;
		public ImageView deviceNumber1;
		public ImageView deviceNumber2;
		public ImageView deviceNumber3;
		public RelativeLayout scanlayout;
		public RelativeLayout deviceLayout;
		// public Button clientButton;
		// public Button blockButton;
	}

	public APDialogListAdapter(Context context, List<WiFiDetail> data, int resource) {
		this.context = context;
		this.listContainer = LayoutInflater.from(context);
		this.itemViewResource = resource;
		this.listItems = data;
	}

	public void UpdateData(List<WiFiDetail> data) {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListItemView listItemView = null;
		if (convertView == null) {

			convertView = listContainer.inflate(this.itemViewResource, null);
			listItemView = new ListItemView();
			listItemView.ssid = (TextView) convertView.findViewById(R.id.ssid);
			listItemView.lock = (ImageView) convertView.findViewById(R.id.lock);
			listItemView.wps = (ImageView) convertView.findViewById(R.id.wps);
			listItemView.wep = (ImageView) convertView.findViewById(R.id.wep);
			listItemView.wpa = (ImageView) convertView.findViewById(R.id.wpa);
			listItemView.channelNumber1 = (ImageView) convertView.findViewById(R.id.channelnumber1);
			listItemView.channelNumber2 = (ImageView) convertView
					.findViewById(R.id.channelnumber2);
			listItemView.deviceNumber1 = (ImageView) convertView
					.findViewById(R.id.devicenumber1);
			listItemView.deviceNumber2 = (ImageView) convertView
					.findViewById(R.id.devicenumber2);
			listItemView.deviceNumber3 = (ImageView) convertView
					.findViewById(R.id.devicenumber3);
			listItemView.scanlayout = (RelativeLayout) convertView
					.findViewById(R.id.scanlayout);
			listItemView.deviceLayout = (RelativeLayout) convertView
					.findViewById(R.id.devicelayout);

			// listItemView.clientButton =
			// (Button)convertView.findViewById(R.id.clientbutton);
			// listItemView.blockButton =
			// (Button)convertView.findViewById(R.id.blockbutton);

			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();

		}
		DisplayMetrics metric = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metric);
		int width = metric.widthPixels;
		if (width < 600) {
			listItemView.ssid.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else if (width >= 600 && width < 750) {
			listItemView.ssid.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		} else if (width >= 750) {
			listItemView.ssid.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
		}
		final WiFiDetail apinfo = listItems.get(position);
		listItemView.ssid.setTag(apinfo);
		listItemView.ssid.setText(apinfo.getSSID());
//		if (apinfo.getPrivacy().replaceAll(" ", "").equals("OPN")) {
//			listItemView.lock.setBackgroundResource(R.drawable.greopen);
//			listItemView.wpa.setBackgroundResource(R.drawable.grewpadis);
//			listItemView.wpa.setBackgroundResource(R.drawable.grewepdis);
//			if (apinfo.getPower() <= -90) {
//				// listItemView.signal.setText("10%");
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg1);
//			} else if (apinfo.getPower() > -90 && apinfo.getPower() <= -75) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg2);
//			} else if (apinfo.getPower() > -75 && apinfo.getPower() <= -60) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg3);
//			} else if (apinfo.getPower() > -60 && apinfo.getPower() <= -45) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg4);
//			} else if (apinfo.getPower() > -45) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg5);
//			}
//
//		} else {
//			if (apinfo.getPrivacy().equals("WEP")) {
//				listItemView.wep.setBackgroundResource(R.drawable.grewepenable);
//				listItemView.wpa.setBackgroundResource(R.drawable.grewpadis);
//			} else {
//				listItemView.wep.setBackgroundResource(R.drawable.grewepdis);
//				listItemView.wpa.setBackgroundResource(R.drawable.grewpaenable);
//			}
//			listItemView.lock.setBackgroundResource(R.drawable.greclose);
//			if (apinfo.getPower() <= -90) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg1);
//			} else if (apinfo.getPower() > -90 && apinfo.getPower() <= -75) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg2);
//			} else if (apinfo.getPower() > -75 && apinfo.getPower() <= -60) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg3);
//			} else if (apinfo.getPower() > -60 && apinfo.getPower() <= -45) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg4);
//			} else if (apinfo.getPower() > -45) {
//				listItemView.scanlayout.setBackgroundResource(R.drawable.grebg5);
//			}
//		}
		
		if(width >= 750){
			LayoutParams params = listItemView.lock.getLayoutParams();
		    params.height=16;  
		    params.width =16; 
			listItemView.lock.setLayoutParams(params);;
		}
		else{
			LayoutParams params = listItemView.lock.getLayoutParams();
		    params.height=16;  
		    params.width =0; 
			listItemView.lock.setLayoutParams(params);;
		}
//		switch (apinfo.getWiFiSignal().getChannel()) {
//		case 1:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum1);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 2:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum2);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 3:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum3);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 4:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum4);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 5:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum5);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 6:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum6);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 7:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum7);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 8:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum8);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//		case 9:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum9);
//			listItemView.channelNumber2.setVisibility(View.GONE);
//			break;
//		case 10:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum1);
//			listItemView.channelNumber2.setBackgroundResource(R.drawable.grenum0);
//			listItemView.channelNumber2.setVisibility(View.VISIBLE);
//			break;
//		case 11:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum1);
//			listItemView.channelNumber2.setBackgroundResource(R.drawable.grenum1);
//			listItemView.channelNumber2.setVisibility(View.VISIBLE);
//			break;
//		case 12:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum1);
//			listItemView.channelNumber2.setBackgroundResource(R.drawable.grenum2);
//			listItemView.channelNumber2.setVisibility(View.VISIBLE);
//			break;
//		case 13:
//			listItemView.channelNumber1.setBackgroundResource(R.drawable.grenum1);
//			listItemView.channelNumber2.setBackgroundResource(R.drawable.grenum3);
//			listItemView.channelNumber2.setVisibility(View.VISIBLE);
//			break;
//		default:
//			break;
//		}
//		if (apinfo.getWps().equals("true"))
//			listItemView.wps.setBackgroundResource(R.drawable.grewpsdis);
//		else
//			listItemView.wps.setBackgroundResource(R.drawable.grewpsenable);
		
//			int clientNum = 0;
//			try {
//				clientNum = (new JSONArray(apinfo.getClient())).length();
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			int h = clientNum / 100;
//			int d = (clientNum % 100) / 10;
//			int u = (clientNum % 100) % 10;
//			switch (h) {
//			case 0:
//				listItemView.deviceNumber3.setVisibility(View.GONE);
//				break;
//			case 1:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum1);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 2:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum2);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 3:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum3);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 4:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum4);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 5:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum5);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 6:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum6);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 7:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum7);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 8:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum8);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			case 9:
//				listItemView.deviceNumber3.setBackgroundResource(R.drawable.grenum9);
//				listItemView.deviceNumber3.setVisibility(View.VISIBLE);
//				break;
//			default:
//				break;
//			}

//			switch (d) {
//			case 0:
//				if (h != 0) {
//					listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum0);
//					listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				} else {
//					listItemView.deviceNumber2.setVisibility(View.GONE);
//				}
//				break;
//			case 1:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum1);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 2:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum2);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 3:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum3);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 4:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum4);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 5:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum5);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 6:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum6);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 7:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum7);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 8:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum8);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			case 9:
//				listItemView.deviceNumber2.setBackgroundResource(R.drawable.grenum9);
//				listItemView.deviceNumber2.setVisibility(View.VISIBLE);
//				break;
//			default:
//				break;
//			}

//			switch (u) {
//			case 0:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum0);
//				break;
//			case 1:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum1);
//				break;
//			case 2:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum2);
//				break;
//			case 3:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum3);
//				break;
//			case 4:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum4);
//				break;
//			case 5:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum5);
//				break;
//			case 6:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum6);
//				break;
//			case 7:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum7);
//				break;
//			case 8:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum8);
//				break;
//			case 9:
//				listItemView.deviceNumber1.setBackgroundResource(R.drawable.grenum9);
//				break;
//			default:
//				break;
//			}
		
		return convertView;
	}

}
