package com.vrem.wifianalyzer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.vrem.wifianalyzer.wifi.common.BaseUtils;
import com.vrem.wifianalyzer.wifi.model.DeviceInfo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class DeviceListActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private RelativeLayout mLeftContainer;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	

	private CharSequence mDrawerTitle;
	private CharSequence mPageTitles;
	private String[] mDrawerItems;
	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();

	private ListView listview;
	
	private ProgressBar progressBar;
	
	public static int flag = -1;

	private TextView refresh;
	private TextView noData;
	private RelativeLayout bottomLayout;
	
	private TextView mdosView;
	
	public static int isChoosing = -1;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);

		mPageTitles 	= mDrawerTitle = getTitle();
		mDrawerItems 	= getResources().getStringArray(R.array.drawer_item_array);
		mDrawerLayout 	= findViewById(R.id.drawer_layout);
		mLeftContainer 	= findViewById(R.id.left_container);
		mDrawerList 	= findViewById(R.id.left_drawer);
		
		progressBar 	= findViewById(R.id.progressbar);

//		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);

		mData = BaseUtils.setDrawerItems(this);

		SimpleAdapter drawerAdapter = new SimpleAdapter(this, mData,
				R.layout.drawer_listitem, new String[] { "icon", "item" },
				new int[] { R.id.icon, R.id.item });
		mDrawerList.setAdapter(drawerAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(this,
		mDrawerLayout,  R.drawable.ic_drawer,  R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mPageTitles);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		listview = (ListView) findViewById(R.id.devicelist);
		
		refresh = (TextView)findViewById(R.id.clickrefresh);
		noData = (TextView)findViewById(R.id.nodata);
		bottomLayout = (RelativeLayout)findViewById(R.id.bottomlayout);
		mdosView = (TextView)findViewById(R.id.mdos);
		
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				refresh.setVisibility(View.GONE);
				noData.setVisibility(View.GONE);
				try {
					DeviceInfo.setDeviceInfo(DeviceListActivity.this, listview, progressBar, refresh, noData, bottomLayout);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		try {
			DeviceInfo.setDeviceInfo(this, listview, progressBar, refresh, noData, bottomLayout);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		MenuItem item = menu.findItem(R.id.refresh);
		if(flag == 1){
			item.setActionView(null);
	        item.expandActionView();

		}else if(flag == 0){
			item.setActionView(R.layout.actionbar_refresh_progress);
	        item.expandActionView();
		}
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh.setVisibility(View.GONE);
			noData.setVisibility(View.GONE);
			isChoosing = -1;
			try {
				DeviceInfo.setDeviceInfo(DeviceListActivity.this, listview, progressBar, refresh, noData, bottomLayout);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bottomLayout.setVisibility(View.GONE);
			return true;

		
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
		switch (position) {
		case 0:

			break;
		case 1:
//			BaseUtils.userDialog(DeviceListActivity.this);
			break;
		case 2:
			BaseUtils.settingDialog(DeviceListActivity.this);
			break;
		case 3:
			BaseUtils.helpDialog(DeviceListActivity.this);
			break;
		case 4:
//			BaseUtils.exitDialog(DeviceListActivity.this);
			break;
		case 5:
			BaseUtils.aboutDialog(DeviceListActivity.this);
			break;
		default:
			break;
		}
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItems[position]);
		mDrawerLayout.closeDrawer(mLeftContainer);

	}
	@SuppressLint("WrongConstant")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	    	if(bottomLayout.getVisibility() == 0){
	    		isChoosing = 1;
	    		DeviceInfo.updateView();
	    	}
	    	else{
		    	finish();
				overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
	    	}
	        return false;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("LongLogTag")
	@Override
	protected void onPause() {
		Log.d("DeviceListActivity status:","Pause");
		super.onPause();
	}

	@SuppressLint("LongLogTag")
	@Override
	protected void onResume() {
		Log.d("DeviceListActivity status:","Resume");
		super.onResume();
	}

	@SuppressLint("LongLogTag")
	@Override
	protected void onStop() {
		Log.d("DeviceListActivity status:","Stop");
		super.onStop();
	}

	@SuppressLint("LongLogTag")
	@Override
	protected void onDestroy() {
		Log.d("DeviceListActivity status:","Destroy");
		super.onDestroy();
	}
}
