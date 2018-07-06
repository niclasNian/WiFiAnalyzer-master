package com.vrem.wifianalyzer.wifi.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vrem.util.TextUtils;
import com.vrem.wifianalyzer.DeviceFunctionActivity;
import com.vrem.wifianalyzer.DeviceListActivity;
import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.common.BackgroundTask;
import com.vrem.wifianalyzer.wifi.common.DevStatusDBUtils;
import com.vrem.wifianalyzer.wifi.common.DosUpdater;
import com.vrem.wifianalyzer.wifi.common.FakeAPUpdater;
import com.vrem.wifianalyzer.wifi.common.HandshakeUpdater;
import com.vrem.wifianalyzer.wifi.common.MacSsidDBUtils;
import com.vrem.wifianalyzer.wifi.common.ScanStep1;
import com.vrem.wifianalyzer.wifi.common.WpsCrackUpdater;
import com.vrem.wifianalyzer.wifi.model.DeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceListAdapter extends BaseAdapter {
	private Context context;
	private List<DeviceInfo> listItems;
	private LayoutInflater listContainer;
	private int itemViewResource;
	private RelativeLayout bottomLayout;
	private ListView listview;
	private ProgressBar progressBar;
	private List<DeviceInfo> devInfo;

	private ListItemView listItemView;

	private TextView refresh;
	private TextView noData;

	private Vibrator vibrator;

	private List<Boolean> mChecked;
	private HashMap<Integer, View> map = new HashMap<Integer, View>();

	private int visibleCount = 0;
	private boolean isAvaliable = true;

	private boolean flag = false;
	private boolean classFlag = false; //用于来判断当前是Activity还是Fragment,他俩停止的方法不一样
	private String bssid = "";

	static class ListItemView {
		public TextView commit;
		public TextView devStatus;
		public ImageView devIcon;
		public RelativeLayout deviceLayout;
		public ImageButton stopButton;
		public CheckBox choose;
		public ExpandableListView expandableListView;
	}

	public DeviceListAdapter(Context context, List<DeviceInfo> data,
                             int resource, ListView listview, ProgressBar progressBar,
                             TextView refresh, TextView noData, RelativeLayout bottomLayout) {
		this.context = context;
		this.listContainer = LayoutInflater.from(context);
		this.itemViewResource = resource;
		this.bottomLayout = bottomLayout;
		this.listItems = data;
		this.listview = listview;
		this.progressBar = progressBar;
		this.devInfo = data;
		this.refresh = refresh;
		this.noData = noData;
		mChecked = new ArrayList<Boolean>();
		for (int i = 0; i < listItems.size(); i++) {
			mChecked.add(false);
		}
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCount() {
		return listItems.size();
	}

	public long getItemId(int arg0) {
		return 0;
	}

	private String getHandlingDetail(String devID) {
		DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
		devStatusDBUtils.open();
		String handlingDetail = devStatusDBUtils.getHandlingDetail(devID);
		devStatusDBUtils.close();
		return handlingDetail;
	}

	public View getView(final int position, View convertView,final ViewGroup parent) {
		View view;
		if (map.get(position) == null) {

			LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = mInflater.inflate(this.itemViewResource, null);
			listItemView = new ListItemView();
			listItemView.commit = view.findViewById(R.id.commit);
			listItemView.devStatus = view.findViewById(R.id.status);
			listItemView.devIcon = view.findViewById(R.id.deviceicon);
			listItemView.deviceLayout = view.findViewById(R.id.devicelayout);
			listItemView.expandableListView = view.findViewById(R.id.clientExpandableLV);
			listItemView.stopButton = view.findViewById(R.id.stop);

			listItemView.choose = view.findViewById(R.id.choose);

			map.put(position, view);
			listItemView.choose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					// listItemView.ok.getY());
					CheckBox cb = (CheckBox) arg0;
					mChecked.set(position, cb.isChecked());

				}
			});
			view.setTag(listItemView);
		} else {
			view = map.get(position);
			listItemView = (ListItemView) view.getTag();

		}

		final DeviceInfo device = listItems.get(position);
		listItemView.commit.setTag(device);

		listItemView.choose.setChecked(mChecked.get(position));
		final String id;
		id = device.getDevId();
		if (device.getCommit().equals(""))
			listItemView.commit.setText(id);
		else
			listItemView.commit.setText(device.getCommit());
		if (!(device.getAlive())) {
			listItemView.deviceLayout.setEnabled(true);
			listItemView.devStatus.setText("离线");
			// listItemView.stateView.setBackgroundColor(Color
			// .parseColor("#B5B5B5"));
			listItemView.stopButton.setVisibility(View.GONE);//设置隐藏按钮
			listItemView.devIcon.setBackgroundResource(R.drawable.wifiwhite);

		} else {
			// 设备活着，启动周期info
//			BackgroundTask.timerInfoStart(context);

			DevStatusDBUtils devStatusDBUtils = null;

			switch (device.getWorkType()) {
				case 100:
					listItemView.devStatus.setText("空闲");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.GONE);//设置隐藏按钮
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#00EE00"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifigreen);

//					devStatusDBUtils = new DevStatusDBUtils(context);
//					devStatusDBUtils.open();
//					devStatusDBUtils.preScan(device.getDevId());
//					devStatusDBUtils.close();

					DevStatusDBUtils devStatusDBUtils1 = new DevStatusDBUtils(context);
					devStatusDBUtils1.open();
					devStatusDBUtils1.scanStep1Done(device.getDevId());//扫描结束时的sql语句
					devStatusDBUtils1.close();

					BackgroundTask.clearAll();
					BackgroundTask.mScanStep1 = new ScanStep1(context, device.getDevId());
					BackgroundTask.mScanStep1.execute();

					break;
				case 101:
					classFlag = true;
					listItemView.devStatus.setText("正在进行热点定向阻断");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);//设置按钮可见
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);

					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
					devStatusDBUtils.close();

					List<String> strlist = new ArrayList<>();
					try {
						//打开文件输入流
						FileInputStream input = MainContext.INSTANCE.getContext().openFileInput("DosFlag.txt");

						DataInputStream dataInputStream = new DataInputStream(input);
						String strLine = null;
						while ((strLine = dataInputStream.readLine())!= null){
							StringBuilder sb = new StringBuilder();
//							Log.d("数据：", String.valueOf(sb.append(strLine)));
							strlist.add(String.valueOf(sb.append(strLine)));
						}
						//关闭输入流
						input.close();

					}catch (Exception e){
						e.printStackTrace();
					}

					if (strlist.size()>0){
						flag = Boolean.valueOf(strlist.get(0).toString());
						bssid = strlist.get(1).toString();
					}
					Log.d("数据：", String.valueOf(flag));
					Log.d("数据：", bssid);
					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new DosUpdater(context, device.getDevId(), null,listItemView.expandableListView,flag,bssid).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 102:
					listItemView.devStatus.setText("正在进行单频段定向阻断");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);//设置按钮可见
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);

					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
					devStatusDBUtils.close();

					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new DosUpdater(context, device.getDevId(), null,listItemView.expandableListView,false,null).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 103:
					listItemView.devStatus.setText("正在进行握手包截获");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);
					listItemView.expandableListView.setVisibility(View.VISIBLE);
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);
					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
                    int handshakeStep1Done = devStatusDBUtils.getHandshakestep1done(device.getDevId());
                    int handshakeStep2Done = devStatusDBUtils.getHandshakestep2done(device.getDevId());
                    String handshakeFile = devStatusDBUtils.getHandlingDetail(device.getDevId());
                    final String handshakeMac = handshakeFile.split("-")[2].split("\\.")[0];
					final int handshakeChannel = Integer.parseInt(handshakeFile.split("-")[0]);
					int l = handshakeFile.split("-").length;
					double rateTmp = -1.0;
					if (l >= 4) {
						rateTmp = Double.parseDouble(handshakeFile.split("-")[3]);
					}
					final double rate = rateTmp;

                    devStatusDBUtils.close();

                    boolean handshakeDos;
                    if (handshakeStep1Done == 1) {
                        handshakeDos = true;
                    }
                    else {
                        handshakeDos = false;
                    }
                    final boolean fhandshakeDos = handshakeDos;

					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new HandshakeUpdater(context, handshakeMac, handshakeChannel, device.getDevId(), fhandshakeDos, rate).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 104:
					listItemView.devStatus.setText("正在进行Wps破解");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);

					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
					devStatusDBUtils.close();

					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new WpsCrackUpdater(context, device.getDevId(), null).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 105:
					classFlag = true;
					listItemView.devStatus.setText("正在进行3G热点伪造");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);

					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
					devStatusDBUtils.close();

					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new FakeAPUpdater(context, device.getDevId(), null).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 201:
					listItemView.devStatus.setText("正在进行WIFI热点伪造");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);

					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
					devStatusDBUtils.close();

					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new FakeAPUpdater(context, device.getDevId(), null).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 110:
					listItemView.devStatus.setText("正在进行多频段定向阻断");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.VISIBLE);
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);

					devStatusDBUtils = new DevStatusDBUtils(context);
					devStatusDBUtils.open();
					devStatusDBUtils.preHandling(device.getDevId());
					devStatusDBUtils.close();

					BackgroundTask.clearAll();
					BackgroundTask.mTimerHandling = new Timer();
					BackgroundTask.mTimerTaskHandling = new TimerTask() {
						@Override
						public void run() {
							((Activity) context).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new DosUpdater(context, device.getDevId(), null,listItemView.expandableListView,false,null).execute();
								}
							});
						}
					};
					BackgroundTask.mTimerHandling.schedule(BackgroundTask.mTimerTaskHandling, 0, 3000);

					break;
				case 200:
					listItemView.devStatus.setText("指令发送中");
					listItemView.deviceLayout.setEnabled(true);
					listItemView.stopButton.setVisibility(View.GONE);
					// listItemView.stateView.setBackgroundColor(Color
					// .parseColor("#EE0000"));
					listItemView.devIcon.setBackgroundResource(R.drawable.wifiblue);
					break;
				default:
					break;
			}

		}

		// final ImageView okImage = (ImageView)
		// convertView.findViewById(R.id.ok);

		final TextView moreView = (TextView) view.findViewById(R.id.moreview);
		listItemView.deviceLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!(device.getAlive())) {
					Intent intent = new Intent();
					intent.setClass(context, DeviceFunctionActivity.class);
					intent.putExtra("deviceinfo", (Serializable) device);
					context.startActivity(intent);
					((Activity) context).overridePendingTransition(
							R.anim.slide_right_in, R.anim.slide_left_out);
				} else {
					switch (device.getWorkType()) {
					case 100:
						Intent intent = new Intent();
						intent.setClass(context, DeviceFunctionActivity.class);
						intent.putExtra("deviceinfo", (Serializable) device);
						context.startActivity(intent);
						((Activity) context).overridePendingTransition(
								R.anim.slide_right_in, R.anim.slide_left_out);
						break;
					case 101:
						try {
							String handlingDetail = getHandlingDetail(device.getDevId());
							moreView.setText("定向阻断热点为:" + handlingDetail);
									/*+ (new JSONObject(device.getParams()))
											.getString("essid"));*/
						} catch (/*JSON*/Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (moreView.getVisibility() == View.VISIBLE)
							moreView.setVisibility(View.GONE);
						else
							moreView.setVisibility(View.VISIBLE);
						break;
					case 102:
						try {
							String handlingDetail = getHandlingDetail(device.getDevId());
							moreView.setText("定向阻断频段为:" + handlingDetail);
									/*+ (new JSONObject(device.getParams()))
											.getString("channeltext"));*/
						} catch (/*JSON*/Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (moreView.getVisibility() == View.VISIBLE)
							moreView.setVisibility(View.GONE);
						else
							moreView.setVisibility(View.VISIBLE);
						break;
					case 103:
						try {
							DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
							devStatusDBUtils.open();
							int handshakeStep1Done = devStatusDBUtils.getHandshakestep1done(device.getDevId());
							int handshakeStep2Done = devStatusDBUtils.getHandshakestep2done(device.getDevId());
							String handlingDetail = devStatusDBUtils.getHandlingDetail(device.getDevId());
							devStatusDBUtils.close();

							boolean handshakeDos;
							if (handshakeStep1Done == 1 && handshakeStep2Done == 0) {
								moreView.setText("阻断中");
							}
							else {
								String macTmp = handlingDetail.split("-")[2];
								MacSsidDBUtils macSsidDBUtils = new MacSsidDBUtils(context);
								macSsidDBUtils.open();
								String ssidTmp = macSsidDBUtils.getSSID(device.getDevId(), macTmp);
								macSsidDBUtils.close();
								moreView.setText("截获握手包AP为:" + handlingDetail.split("-")[1] + "-" + ssidTmp + ".cap");
							}
						} catch (/*JSON*/Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (moreView.getVisibility() == View.VISIBLE)
							moreView.setVisibility(View.GONE);
						else
							moreView.setVisibility(View.VISIBLE);
						break;
					case 104:
							try {
								String handlingDetail = getHandlingDetail(device.getDevId());
								moreView.setText("wps破解为:" + handlingDetail);
							} catch (/*JSON*/Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (moreView.getVisibility() == View.VISIBLE)
								moreView.setVisibility(View.GONE);
							else
								moreView.setVisibility(View.VISIBLE);
							break;
					case 110:

						try {
							moreView.setText("定向阻断频段为:" + getHandlingDetail(device.getDevId()));
									/*+ (new JSONObject(device.getParams()))
											.getInt("channeltext"));*/
						} catch (/*JSON*/Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (moreView.getVisibility() == View.VISIBLE)
							moreView.setVisibility(View.GONE);
						else
							moreView.setVisibility(View.VISIBLE);
						break;
					case 105:
						try {
							moreView.setText("模拟热点ssid为:" + getHandlingDetail(device.getDevId()));
									/*+ (new JSONObject(device.getParams()))
											.getString("essid"));*/
						} catch (/*JSON*/Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (moreView.getVisibility() == View.VISIBLE)
							moreView.setVisibility(View.GONE);
						else
							moreView.setVisibility(View.VISIBLE);
						break;
                    case 201:
                        try {
                            moreView.setText("模拟热点ssid为:" + getHandlingDetail(device.getDevId()));
                                    /*+ (new JSONObject(device.getParams()))
                                    .getString("essid"));*/
                        } catch (/*JSON*/Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (moreView.getVisibility() == View.VISIBLE)
                            moreView.setVisibility(View.GONE);
                        else
                            moreView.setVisibility(View.VISIBLE);
                        break;
					default:
						break;

					}
				}
			}
		});

		listItemView.deviceLayout.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View arg0) {
						// TODO Auto-generated method stub
						vibrator = (Vibrator) context
								.getSystemService(Context.VIBRATOR_SERVICE);
						vibrator.vibrate(50);
						View view = ((Activity) context).getLayoutInflater()
								.inflate(R.layout.context_menu, null);
						ArrayList<String> list = new ArrayList<String>();
						list.add("修改备注名称");
						list.add("重启该设备");
						//list.add("更多");
						final Dialog dialog = new Dialog(context);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setContentView(view);
						Window dialogWindow = dialog.getWindow();
						WindowManager wm = ((Activity) context)
								.getWindowManager();
						Display display = wm.getDefaultDisplay();
						WindowManager.LayoutParams lp = dialogWindow
								.getAttributes();
						lp.height = LayoutParams.WRAP_CONTENT;
						lp.width = (int) (display.getWidth() * 2 / 3);
						dialogWindow.setAttributes(lp);
						dialog.show();
						dialog.getWindow().setBackgroundDrawable(
								new ColorDrawable(Color.TRANSPARENT));
						RelativeLayout contextLayout = (RelativeLayout) view
								.findViewById(R.id.context_menu);
//						contextLayout.setBackgroundResource(R.drawable.contextbg3);
						final ListView listView = (ListView) view
								.findViewById(R.id.listview);
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(
								context, R.layout.context_menu_listitem, list);
						listView.setAdapter(adapter);
						listView.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0,
                                                    View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								switch (arg2) {
								// TextView ssid = (TextView)
								// arg1.findViewById(R.id.ssid);
								// PackageInfo packageInfo = (PackageInfo)
								// ssid.getTag();
								case 0:
                                    dialog.dismiss();
									View commitView = ((Activity) context)
											.getLayoutInflater()
											.inflate(
													R.layout.context_commit_dialog,
													null);
									final Dialog commitDialog = new Dialog(
											context);
									commitDialog
											.requestWindowFeature(Window.FEATURE_NO_TITLE);
									commitDialog.setContentView(commitView);
									Window commitDialogWindow = commitDialog
											.getWindow();
									WindowManager commitWm = ((Activity) context)
											.getWindowManager();
									Display commitDisplay = commitWm
											.getDefaultDisplay();
									WindowManager.LayoutParams commitlp = commitDialogWindow
											.getAttributes();
									commitlp.height = LayoutParams.WRAP_CONTENT;
									commitlp.width = (int) (commitDisplay
											.getWidth() * 2 / 3);
									commitDialogWindow.setAttributes(commitlp);
									commitDialog.show();
									commitDialog.getWindow()
											.setBackgroundDrawable(
													new ColorDrawable(Color.TRANSPARENT));
									RelativeLayout contextLayout = (RelativeLayout) commitView.findViewById(R.id.context_menu);
//									contextLayout.setBackgroundResource(R.drawable.contextbg1);
									final EditText commit = (EditText) commitView
											.findViewById(R.id.commit);
									commit.setFocusable(true);
									Button cancelButton = (Button) commitView
											.findViewById(R.id.cancel);
									Button okButton = (Button) commitView
											.findViewById(R.id.ok);
									cancelButton
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View arg0) {
													// TODO Auto-generated
													// method stub
													commitDialog.dismiss();
												}
											});
									okButton.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											// TODO Auto-generated method stub
											try {
												DeviceInfo.setCommit(context,
														device.getDevId(),
														commit.getText()
																.toString(),
														listview, progressBar,
														refresh, noData,
														bottomLayout);
												// listItemView.commit.setText(et.getText().toString());

											} catch (JSONException e) {
												// TODO
												// Auto-generated
												// catch
												// block
												e.printStackTrace();
											}
											commitDialog.dismiss();
											dialog.dismiss();
										}
									});
									break;
								case 1:
									dialog.dismiss();
									break;
								default:
									break;
								}
							}
						});
						return false;
					}

				});

		//停止事件
		listItemView.stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				new AlertDialog.Builder(context)
						.setTitle("确认")
						.setMessage("确定停止吗？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										DevStatusDBUtils devStatusDBUtils = new DevStatusDBUtils(context);
										devStatusDBUtils.open();
										devStatusDBUtils.handshakeCancel(device.getDevId());//将数据改为原始状态
										devStatusDBUtils.fakeAPCancel(device.getDevId());
										devStatusDBUtils.wpscrackStep1Done(device.getDevId());
										devStatusDBUtils.close();

										BackgroundTask.clearAll();//取消异步任务

										String path = "/data/data/com.vrem.wifianalyzer/files/DosFlag.txt";
										File file = new File(path);
										if (file.exists()){
											boolean en = file.delete();
											Log.e("","DELETE FILE "+ en);
										}
//										if (classFlag){ //说明是fragment （自定义的）否则是activity
											Intent intent = new Intent();
											intent.setClass(context, MainActivity.class);
											context.startActivity(intent);
											((Activity) context).finish();
//										}else {
//											((Activity) context).finish();
//										}

//										((Activity) context).finish();
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										arg0.dismiss();
									}

								}).show();

			}
		});

		bottomLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				for (int i = 0; i < getCount(); i++) {
					if (((CheckBox) getView(i, null, null).findViewById(
							R.id.choose)).isChecked()) {
						if (!(listItems.get(i).getAlive()))
							isAvaliable = false;
						if (listItems.get(i).getWorkType() != 100)
							isAvaliable = false;

						visibleCount++;
					}
				}
				if (visibleCount < 2) {
					Toast.makeText(context, "请选择大于等于二个设备！", Toast.LENGTH_SHORT)
							.show();
					visibleCount = 0;
					isAvaliable = true;
				} else if (!isAvaliable) {
					Toast.makeText(context, "请选择空闲在线设备！", Toast.LENGTH_SHORT)
							.show();
					visibleCount = 0;
					isAvaliable = true;
				} else {
					List<Integer> channelList = new ArrayList<Integer>();
					List<String> channelText = new ArrayList<String>();
					/*
					if (visibleCount == 4) {
						channelList.add(14);
						channelList.add(112);
						channelList.add(896);
						channelList.add(3072);
						channelText.add("2,4,8");
						channelText.add("16,32,64");
						channelText.add("128,256,512");
						channelText.add("1024,2048");
					} else if (visibleCount == 5) {
						channelList.add(14);
						channelList.add(48);
						channelList.add(192);
						channelList.add(768);
						channelList.add(3072);
						channelText.add("2,4,8");
						channelText.add("16,32");
						channelText.add("64,128");
						channelText.add("256,512");
						channelText.add("1024,2048");
					} else if (visibleCount == 6) {
						channelList.add(6);
						channelList.add(24);
						channelList.add(96);
						channelList.add(384);
						channelList.add(1536);
						channelList.add(2048);
						channelText.add("2,4");
						channelText.add("8,16");
						channelText.add("32,64");
						channelText.add("128,256");
						channelText.add("512,1024");
						channelText.add("2048");

					} else {
						Toast.makeText(context, "选择设备过多！", Toast.LENGTH_SHORT)
								.show();
						visibleCount = 0;
					}*/
					if (visibleCount == 2) {
						channelList.add(126);
						channelList.add(3968);
						channelText.add("2,4,8,16,32,64");
						channelText.add("128,256,512,1024,2048");
					} else if (visibleCount == 3) {
						channelList.add(30);
						channelList.add(480);
						channelList.add(3584);
						channelText.add("2,4,8,16");
						channelText.add("32,64,128,256");
						channelText.add("512,1024,2048");
					} else if (visibleCount == 4) {
						channelList.add(14);
						channelList.add(112);
						channelList.add(896);
						channelList.add(3072);
						channelText.add("2,4,8");
						channelText.add("16,32,64");
						channelText.add("128,256,512");
						channelText.add("1024,2048");
					}else if (visibleCount == 5) {
						channelList.add(14);
						channelList.add(48);
						channelList.add(192);
						channelList.add(768);
						channelList.add(3072);
						channelText.add("2,4,8");
						channelText.add("16,32");
						channelText.add("64,128");
						channelText.add("256,512");
						channelText.add("1024,2048");
					} else if (visibleCount == 6) {
						channelList.add(6);
						channelList.add(24);
						channelList.add(96);
						channelList.add(384);
						channelList.add(1536);
						channelList.add(2048);
						channelText.add("2,4");
						channelText.add("8,16");
						channelText.add("32,64");
						channelText.add("128,256");
						channelText.add("512,1024");
						channelText.add("2048");

					}else {
						Toast.makeText(context, "选择设备过多！", Toast.LENGTH_SHORT)
								.show();
						visibleCount = 0;
					}

					int mCount = 0;
					for (int i = 0; i < getCount(); i++) {
						if (((CheckBox) getView(i, null, null).findViewById(
								R.id.choose)).isChecked()) {
							mChecked.set(i, true);
							try {
								JSONObject jo = new JSONObject();
								jo.put("channel", channelList.get(mCount));
								DeviceInfo.sendCommand(context,
										listItems.get(i), jo, "wifi_dos_all");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mCount++;
						}
					}
					DeviceListActivity.isChoosing = 1;
					notifyDataSetChanged();
				}
			}
		});

		if (DeviceListActivity.isChoosing == 1) {
			listItemView.choose.setVisibility(View.GONE);

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) listItemView.deviceLayout
					.getLayoutParams();
			lp.setMargins(0, 0, 0, 0);
			listItemView.deviceLayout.setLayoutParams(lp);
			listItemView.deviceLayout.setEnabled(true);
			bottomLayout.setVisibility(View.GONE);
			listItemView.stopButton.setEnabled(true);
			bottomLayout.setVisibility(View.GONE);
		}

		listItemView.devStatus.setTextColor(Color.argb(155, 255, 255, 255));
		RelativeLayout tmpDevice = (RelativeLayout) view
				.findViewById(R.id.devicelayout);
		CheckBox tmpChoose = (CheckBox) view.findViewById(R.id.choose);
		ImageButton tmpButton = (ImageButton) view.findViewById(R.id.stop);
		if (DeviceListActivity.isChoosing == 0) {
			tmpChoose.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tmpDevice
					.getLayoutParams();
			lp.setMargins(0, 0, 80, 0);
			tmpDevice.setLayoutParams(lp);
			tmpDevice.setEnabled(false);
			tmpButton.setEnabled(false);

		}
		return view;
	}

}
