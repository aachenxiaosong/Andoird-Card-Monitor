package com.xiaosche.sdncardmonitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sdncardmonitor.R;
import com.xiaosche.data.CardMode;
import com.xiaosche.data.CardRole;
import com.xiaosche.data.CardStatus;
import com.xiaosche.data.ErrorType;
import com.xiaosche.data.GlobalMacro;
import com.xiaosche.debug.DebugDialog;
import com.xiaosche.setting.SettingDialog;
import com.xiaosong.adapter.FragmentAdapter;


@SuppressLint("InflateParams")
public class MainActivity extends FragmentActivity {

	//private String urlPath="http://10.79.41.44:8388/restconf/operations/generalcli:get-generalcli";
	private static final int ITEM1 = Menu.FIRST;  
	private static final int ITEM2 = Menu.FIRST + 1;  
	private static final int ITEM3 = Menu.FIRST + 2; 
	private static final int ITEM4 = Menu.FIRST + 3; 
	
	private Timer timer;
	private Handler handler;
	private TimerTask task;
	
	private ViewPager mViewPagers; 
	List<Fragment> mFragments = new ArrayList<Fragment>();
	private ShowDeviceAdapter mSpinnerAdapter; 
	
	private Spinner mSpinner;
	private TextView mTotalText;
	private TextView mWarningText;
	private TextView mErrorText;
	private TextView mNoconnText;
	private Button mLink;
	private int mTotal;
	private int mWarning;
	private int mError;
	private int mNoconn;
	
	private PagerAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		setTitle("CMTS Card Status Monitor");
		
		initWidget();
		
		initViewPager();
		addDevices("CRDC-NH-06", "20.5.32.16");
		addDevices("A", "20.5.1.5");
		addDevices("B", "20.5.32.16");
		
		initTimer();
		timer.schedule(task, 1000, GlobalMacro.SIMPLE_TIME * 1000);
	}
	
	private void initWidget() {
		mTotalText = (TextView)findViewById(R.id.tv_overall_total);
		mTotal = 0;
		mWarningText = (TextView)findViewById(R.id.tv_overall_warning);
		mWarning = 0;
		mErrorText = (TextView)findViewById(R.id.tv_overall_error);
		mError = 0;
		mNoconnText = (TextView)findViewById(R.id.tv_overall_unconnect);
		mNoconn = 0;
		mLink = (Button)findViewById(R.id.bt_link);
		mLink.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri uri = Uri.parse("http://10.79.41.59:8181/cmts/sdn/lcha/index.html");  
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
                
                startActivity(intent);
				/*
				View linkview = LayoutInflater.from(MainActivity.this).inflate(R.layout.link, null);
				WebView wv = (WebView) linkview.findViewById(R.id.webView01);
				
				wv.loadUrl("http://10.79.41.59:8181/cmts/sdn/lcha/index.html");
				wv.setScrollContainer(true);
	            new AlertDialog.Builder(MainActivity.this).setView(linkview).setTitle("LCHA").
	                           setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stu
					}
				}).setNegativeButton("Cancel", null).show();*/
			}
		});
		
		mSpinner = (Spinner)findViewById(R.id.sp_name);
		mSpinnerAdapter = new ShowDeviceAdapter(this.getApplicationContext());
		mSpinner.setAdapter(mSpinnerAdapter);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mViewPagers.setCurrentItem(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	private void initViewPager() {
		mViewPagers = (ViewPager) findViewById(R.id.viewPager);
		mAdapter = new FragmentAdapter(getSupportFragmentManager(), mFragments);
		mViewPagers.setAdapter(mAdapter);
		mViewPagers.setOffscreenPageLimit(GlobalMacro.MAX_DEVICES_TO_MONITOR);
		mViewPagers.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				mSpinner.setSelection(arg0);				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		mViewPagers.setCurrentItem(0);
	}
	
	
	private void initTimer() {
		timer = new Timer();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {	
				/* update display */
				switch (msg.what) {
				case 100:
					updateViewPagerDisplay();
					updateMainPanelDisplay();
					break;
				}
				super.handleMessage(msg);
			}
		};
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				mWarning = mError = mNoconn = 0;
				mTotal = mFragments.size();
				String result = null;
				//Log.e("TimeStart", "starttime");//
				/* 1。 get platform information, process of "show platform" is a little different from others, 
				 * because it will help to check carddata status */
				for (int i = 0; i < mTotal; i++) {
					result = null;
					try {
						result = ((ChassisFragment) (mFragments.get(i)))
								.postRequest("show platform");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					((ChassisFragment) (mFragments.get(i))).mStringprocess
					.platformInfoProcess(result);
				}

				/* 2. get redun line mCard all information */
				for (int i = 0; i < mTotal; i++) {
					try {
						/* whatever you do here, don't take more than 5s */
						result = ((ChassisFragment) (mFragments.get(i)))
								.postRequest("show redun line all");
						if (result != null) {
							((ChassisFragment) (mFragments.get(i))).mStringprocess
									.redunLCAllInfonProcess(result);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				/* 3. get redun line mCard all information */
				for (int i = 0; i < mTotal; i++) {
					try {
						/* whatever you do here, don't take more than 5s */
						result = ((ChassisFragment) (mFragments.get(i)))
								.postRequest("show redun line all");
						if (result != null) {
							((ChassisFragment) (mFragments.get(i))).mStringprocess
									.redunLCAllInfonProcess(result);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				/* 4. calculate the status of each card */
				
				int status = CardStatus.CHASSIS_OK;
				for (int i = 0; i < mTotal; i++) {
					try {
						status = ((ChassisFragment) (mFragments.get(i))).mStringprocess
								.getCardStatus();
						if (status == CardStatus.CHASSIS_WARNING) {
							mWarning++;
						} else if (status == CardStatus.CHASSIS_ERROR) {
							mError++;
						} else if (status == CardStatus.CHASSIS_NOT_CONNECTION) {
							mNoconn++;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				/* 5. show cable modem */
				for (int i = 0; i < mTotal; i++) {
					try {
						/* whatever you do here, don't take more than 5s */
						result = ((ChassisFragment) (mFragments.get(i)))
								.postRequest("show cable modem summary");
						if (result != null) {
							((ChassisFragment) (mFragments.get(i))).mStringprocess.
							showModemProcess(result);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				/* 6. show cable flap-list */
				for (int i = 0; i < mTotal; i++) {
					try {
						/* whatever you do here, don't take more than 5s */
						result = ((ChassisFragment) (mFragments.get(i)))
								.postRequest("show cable flap-list");
						if (result != null) {
							((ChassisFragment) (mFragments.get(i))).mStringprocess.
							showCableFlapProcess(result);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				//Log.e("TimeEnd", "endtime");
				message.what = 100;
				handler.sendMessage(message);
			}
		};
	}
	
	private void addDevices(String name, String ip) {
		mFragments.add(new ChassisFragment(this.getApplicationContext(), name, ip, mFragments.size()));
		//如果想让adapter自适应动态增长的数组，则一定要调用这一句
		mSpinnerAdapter.notifyDataSetChanged();
		mAdapter.notifyDataSetChanged();
		mViewPagers.setCurrentItem(mFragments.size() - 1);
		mTotal = mFragments.size();
	}
	
	/* after remove operation, recalculate all the device */
	private void updateDevices() {
		mAdapter.notifyDataSetChanged();
		mSpinnerAdapter.notifyDataSetChanged();
		mViewPagers.setCurrentItem(0);
		
		mTotal = mFragments.size();
		mSpinner.setSelection(0);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM1:	
			final View adddeviceview = LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.adddevicedialog, null);
            new AlertDialog.Builder(this).setView(adddeviceview).setTitle("Add CMTS Device").
                           setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText deviceip = (EditText) adddeviceview.findViewById(R.id.ev_deviceip);
					EditText devicename = (EditText) adddeviceview.findViewById(R.id.ev_devicename);
					String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." + 
					               "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + 
							       "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + 
					               "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
					if (deviceip.getText().toString().matches(regex) && devicename.getText().toString() != null) {
					//把页面添加到ViewPager里
						addDevices(devicename.getText().toString(), deviceip.getText().toString());
					} else {
						Toast.makeText(MainActivity.this, "Wrong format, please input again!!", Toast.LENGTH_SHORT).show();
					}
				}
			}).setNegativeButton("Cancel", null).show();
			break;
		case ITEM2:
			final View deldeviceview = LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.deletedevicedialog, null);
		
			ListView bc_list = (ListView)deldeviceview.findViewById(R.id.lv_deletedevice);
			bc_list.setAdapter(new DeleteDeviceAdapter(this.getApplicationContext()));
			
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("Delete CMTS Device")
					.setView(deldeviceview)
					.setPositiveButton("Delete",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Iterator<Fragment> i = mFragments.iterator();
									while (i.hasNext()) {
										ChassisFragment f = (ChassisFragment)(i.next());
										if (f.isDeleted() == true) {
											i.remove();
										}
									}
									updateDevices();
								}
							}).setNegativeButton("Cancel", null).show();
			break;
		case ITEM3:
			SettingDialog setting = new SettingDialog(this);
			setting.show();
			break;
		case ITEM4:
			DebugDialog debug = new DebugDialog(this, (ChassisFragment)(mFragments.get(mViewPagers.getCurrentItem())));
			debug.show();
			break;
		}
		return true;
	}
	
	public class DeleteDeviceAdapter extends BaseAdapter {

		private LayoutInflater mInflater;// 动态布局映射  

		public DeleteDeviceAdapter(Context c) {
			this.mInflater = LayoutInflater.from(c);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mFragments.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = mInflater.inflate(R.layout.delete_device_list_item, null);
			TextView devicename;
			devicename = (TextView)convertView.findViewById(R.id.tv_deletedevice);
			devicename.setText(((ChassisFragment)(mFragments.get(position))).getChassisName() + " | " + 
			                   ((ChassisFragment)(mFragments.get(position))).getChassisIp());
			CheckBox selectdevice = (CheckBox)convertView.findViewById(R.id.cb_deletedevice);
			
			selectdevice.setOnCheckedChangeListener(new DeleteDeviceOnCheckedChangeListener(position));
			return convertView;
		}
		
		public class DeleteDeviceOnCheckedChangeListener implements OnCheckedChangeListener{

			private int position;
			public DeleteDeviceOnCheckedChangeListener(int p) {
				position = p;
			}
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked == true) {
					((ChassisFragment)(mFragments.get(position))).setDeleted(true);
				} else {
					((ChassisFragment)(mFragments.get(position))).setDeleted(false);
				}
			}
			
		}
		
	}
	
	public class ShowDeviceAdapter extends BaseAdapter {

		private LayoutInflater mInflater;// 动态布局映射  

		public ShowDeviceAdapter(Context c) {
			this.mInflater = LayoutInflater.from(c);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mFragments.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mFragments.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = mInflater.inflate(R.layout.show_device_list_item, null);
			TextView devicename;
			TextView status;
			devicename = (TextView)convertView.findViewById(R.id.tv_showdevice);
			devicename.setText(" " + ((ChassisFragment)(mFragments.get(position))).getChassisName() + " | " + 
			                   ((ChassisFragment)(mFragments.get(position))).getChassisIp());
			status = (TextView)convertView.findViewById(R.id.tv_showdevicestatus);
			try {
				switch (((ChassisFragment) (mFragments.get(position))).mCardData.status) {
				case CardStatus.CHASSIS_OK:
					status.setBackgroundResource(R.drawable.circle_green);
					break;
				case CardStatus.CHASSIS_WARNING:
					status.setBackgroundResource(R.drawable.circle_yellow);
					break;
				case CardStatus.CHASSIS_ERROR:
					status.setBackgroundResource(R.drawable.circle_red);
					break;
				case CardStatus.CHASSIS_NOT_CONNECTION:
					status.setBackgroundResource(R.drawable.circle_gray);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//devicename.setText("" + a[position]);
			return convertView;
		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0, ITEM1, 0, "Add Device");
		menu.add(0, ITEM2, 0,"Delete Device");
		menu.add(0, ITEM3, 0, "Setting");
		menu.add(0, ITEM4, 0, "Debug");
		/* set prompt level */
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	private void updateViewPagerDisplay() {
		
		int position = mViewPagers.getCurrentItem();
		
	    for (int i = 0; i < ((ChassisFragment)(mFragments.get(position))).mCardData.card.length; i++) {
	    	if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].isExisting == false) {
	    		((ChassisFragment)(mFragments.get(position))).mCard[i].setBackgroundResource(R.drawable.content_bg_nonexsiting);
	    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setBackgroundResource(R.drawable.circle_white);
	    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setTextColor(getResources().getColor(R.color.gray));
	    		if (i != 4 && i != 5) {//just for LC
	    			((ChassisFragment)(mFragments.get(position))).mCardText[i].setText("L" + i);
	    		}
	    	} else {//要修改边框线条颜色吗？
	    		((ChassisFragment)(mFragments.get(position))).mCard[i].setBackgroundResource(R.drawable.content_bg);
	    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setBackgroundResource(R.drawable.circle_green);
	    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setTextColor(getResources().getColor(R.color.white));
	    		
	    		/* mode */
		    	if (i != 4 && i != 5) {//just for LC
		    		if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].mode == CardMode.PRIMARY) {
		    			((ChassisFragment)(mFragments.get(position))).mCardText[i].setText(i+"P");
		    		} else if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].mode == CardMode.SECONDARY) {
		    			((ChassisFragment)(mFragments.get(position))).mCardText[i].setText(i+"S");
		    		} else {
		    			((ChassisFragment)(mFragments.get(position))).mCardText[i].setText(i+"-");
		    		}
		    	}
		    	/* role */
		    	if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].role == CardRole.ACTIVE) {
		    		((ChassisFragment)(mFragments.get(position))).mCard[i].setBackgroundResource(R.drawable.content_bg_active);
		    	} else if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].role == CardRole.STANDBY) {
		    		((ChassisFragment)(mFragments.get(position))).mCard[i].setBackgroundResource(R.drawable.content_bg_stby);
		    	} else {
		    		((ChassisFragment)(mFragments.get(position))).mCard[i].setBackgroundResource(R.drawable.content_bg_init);
		    	}
		    	/* status */
		    	/*if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].cylon_status == CardStatus.ERROR || ((ChassisFragment)(mFragments.get(position))).mCardData.card[i].pic_status == CardStatus.ERROR) {
		    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setBackgroundResource(R.drawable.circle_red);
		    	} else if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].cylon_status == CardStatus.WARNING || ((ChassisFragment)(mFragments.get(position))).mCardData.card[i].pic_status == CardStatus.WARNING) {
		    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setBackgroundResource(R.drawable.circle_yellow);
		    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setTextColor(getResources().getColor(R.color.black));
		    	} else {
		    		((ChassisFragment)(mFragments.get(position))).mCardText[i].setBackgroundResource(R.drawable.circle_green);
		    	}*/
		    	int j = 0;
		    	for (j = 0; j < ErrorType.ErrorList.length; j++) {
		    		if (((ChassisFragment)(mFragments.get(position))).mCardData.card[i].errorArr[j].isEmpty() == false) {
		    			((ChassisFragment)(mFragments.get(position))).mCardText[i].setBackgroundResource(R.drawable.circle_red);
		    			break;
		    		} 
		    	}
				if (j >= ErrorType.ErrorList.length) {// not error
					for (j = 0; j < ErrorType.ErrorList.length; j++) {
						if (((ChassisFragment) (mFragments.get(position))).mCardData.card[i].warningArr[j]
								.isEmpty() == false) {
							((ChassisFragment) (mFragments.get(position))).mCardText[i]
									.setBackgroundResource(R.drawable.circle_yellow);
							((ChassisFragment) (mFragments.get(position))).mCardText[i]
									.setTextColor(getResources().getColor(
											R.color.black));
							break;
						}
					}

					if (j >= ErrorType.ErrorList.length) // not error
						((ChassisFragment) (mFragments.get(position))).mCardText[i]
								.setBackgroundResource(R.drawable.circle_green);
				}
	    	}
	    }
	    
	    /* Add/Delete watermark for unconnection attribute */
	    if (((ChassisFragment) (mFragments.get(position))).mStringprocess
								.getCardStatus() == CardStatus.CHASSIS_NOT_CONNECTION &&
								((ChassisFragment) (mFragments.get(position))).hasWaterMark() == false) {
	    	((ChassisFragment) (mFragments.get(position))).drawWaterMark();
	    	((ChassisFragment) (mFragments.get(position))).setWaterMark(true);
	    } else if (((ChassisFragment) (mFragments.get(position))).mStringprocess
								.getCardStatus() != CardStatus.CHASSIS_NOT_CONNECTION &&
								((ChassisFragment) (mFragments.get(position))).hasWaterMark() == true) {
	    	((ChassisFragment) (mFragments.get(position))).clearWaterMark();
	    	((ChassisFragment) (mFragments.get(position))).setWaterMark(false);
	    }
	    
	}
	
	private void updateMainPanelDisplay() {
		mTotalText.setText(getResources().getString(R.string.total) + mTotal);
		mWarningText.setText(getResources().getString(R.string.warning) + mWarning);
		mErrorText.setText(getResources().getString(R.string.error) + mError);
		mNoconnText.setText(getResources().getString(R.string.unconnect) + mNoconn);
		int i = mSpinner.getSelectedItemPosition();
		mSpinner.setAdapter(mSpinnerAdapter);
		mSpinner.setSelection(i);
	}
	
	public void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (task != null) {
			task.cancel();
			task = null;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		stopTimer();
		super.onDestroy();
	}
}