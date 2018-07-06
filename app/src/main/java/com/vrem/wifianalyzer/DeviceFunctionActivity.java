package com.vrem.wifianalyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class DeviceFunctionActivity extends Activity {
	private ListView listview;
	private TextView descView;

	private DrawerLayout mDrawerLayout;
	private RelativeLayout mLeftContainer;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mPageTitles;
	private String[] mDrawerItems;
	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	
	private Serializable deviceInfo;

	private ProgressBar progressBar;
	
	private TextView refresh;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_functions);
//		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setLogo(R.drawable.backlayout);
//		getActionBar().setHomeButtonEnabled(true);
		
		Intent intent = getIntent();
		deviceInfo = intent.getSerializableExtra("deviceinfo");
		mPageTitles = mDrawerTitle = getTitle();
//		mDrawerItems = getResources().getStringArray(R.array.drawer_item_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftContainer = (RelativeLayout) findViewById(R.id.left_container);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

//		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
//				GravityCompat.START);

//		mData = BaseUtils.setDrawerItems(this);

		SimpleAdapter drawerAdapter = new SimpleAdapter(this, mData,
				R.layout.drawer_listitem, new String[] { "icon", "item" },
				new int[] { R.id.icon, R.id.item });
		mDrawerList.setAdapter(drawerAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
//		mDrawerToggle = new ActionBarDrawerToggle(this,
//		mDrawerLayout,  R.drawable.ic_drawer,  R.string.drawer_open,
//		R.string.drawer_close
//		) {
//			public void onDrawerClosed(View view) {
//				getActionBar().setTitle(mPageTitles);
//				invalidateOptionsMenu();
//			}
//
//			public void onDrawerOpened(View drawerView) {
//				getActionBar().setTitle(mDrawerTitle);
//				invalidateOptionsMenu();
//			}
//		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		listview = (ListView) findViewById(R.id.devicelist);
		
		refresh = (TextView)findViewById(R.id.clickrefresh);
		
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				refresh.setVisibility(View.GONE);
//				try {
//					DeviceFunction.getDeviceType(DeviceFunctionActivity.this, listview, (DeviceInfo)deviceInfo, progressBar, refresh);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		});
		
		progressBar = (ProgressBar)findViewById(R.id.progressbar);
		
//		try {
//			DeviceFunction.getDeviceType(this, listview, (DeviceInfo)deviceInfo, progressBar, refresh);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent();
	    	intent.setClass(DeviceFunctionActivity.this, DeviceListActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
			overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
//		switch (position) {
//		case 0:
//			BaseUtils.activityJumping(DeviceFunctionActivity.this,
//					DeviceListActivity.class);
//			break;
//		case 1:
//			BaseUtils.userDialog(DeviceFunctionActivity.this);
//			break;
//		case 2:
//			BaseUtils.settingDialog(DeviceFunctionActivity.this);
//			break;
//		case 3:
//			BaseUtils.helpDialog(DeviceFunctionActivity.this);
//			break;
//		case 4:
//			BaseUtils.exitDialog(DeviceFunctionActivity.this);
//			break;
//		case 5:
//			BaseUtils.aboutDialog(DeviceFunctionActivity.this);
//			break;
//		default:
//			break;
//		}
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItems[position]);
		mDrawerLayout.closeDrawer(mLeftContainer);

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	    	DeviceListActivity.isChoosing = -1;
	    	Intent intent = new Intent();
	    	intent.setClass(DeviceFunctionActivity.this, DeviceListActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
			overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
	        return false;
	    } 
	    return super.onKeyDown(keyCode, event);
	}

}
