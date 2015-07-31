package com.xiaosche.debug;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.sdncardmonitor.R;
import com.xiaosche.data.ErrorType;
import com.xiaosche.sdncardmonitor.ChassisFragment;

public class DebugDialog extends Dialog {

	private List<String> mCmdArr = new ArrayList<String>();
	private ArrayAdapter<String> mDebugAdapter;
	private Spinner mSpinner;
	private Button mBtAdd;
	private Button mBtDel;
	private Button mBtSendExist;
	private Button mBtSendCustom;
	private EditText mEtCmd;
	private TextView mTvResult;
	private ChassisFragment mChassis;
	public DebugDialog(Context context, ChassisFragment chassis) {
		super(context);
		// TODO Auto-generated constructor stub
		mChassis = chassis;
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug);
		setTitle("Debug");
		mSpinner = (Spinner)findViewById(R.id.debug_cmd_sp);
		
		mBtAdd = (Button)findViewById(R.id.debug_add_customize);
		mBtDel = (Button)findViewById(R.id.debug_delete_customize);
		mBtSendExist = (Button)findViewById(R.id.debug_send_exist);
		mBtSendCustom = (Button)findViewById(R.id.debug_send_customize);
		mEtCmd = (EditText) findViewById(R.id.debug_cmd_customize);
		mTvResult = (TextView) findViewById(R.id.debug_result);
		
		
		mCmdArr.add("show cable modem");
		mCmdArr.add("show cable modem summ");
		mCmdArr.add("show platform");
		mCmdArr.add("show redun line all");
		Collections.sort(mCmdArr, Collator.getInstance(java.util.Locale.ENGLISH));
		
		mDebugAdapter = new ArrayAdapter<String>(this.getContext() ,android.R.layout.simple_spinner_item, mCmdArr);
		mSpinner.setAdapter(mDebugAdapter);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mBtAdd.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String tmp = mEtCmd.getText().toString();
				if (tmp.isEmpty() == false && mCmdArr.contains(tmp) == false) {
					mCmdArr.add(tmp);
					Collections.sort(mCmdArr, Collator.getInstance(java.util.Locale.ENGLISH));
					mDebugAdapter.notifyDataSetChanged();
					int position = mCmdArr.indexOf(tmp);
					mSpinner.setSelection(position);
				}
			}});
		mBtDel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int position = mSpinner.getSelectedItemPosition();
				mCmdArr.remove(position);
				mDebugAdapter.notifyDataSetChanged();
				mSpinner.setSelection(0);
			}
		});
		
		mBtSendExist.setOnClickListener(new SendListener(false));
		mBtSendCustom.setOnClickListener(new SendListener(true));
	}
	
	class SendListener implements View.OnClickListener {

		private boolean mIsCustomizedCmd;
		private String cmd;
		String output;
		public SendListener(boolean isCustomizedCmd) {
			mIsCustomizedCmd = isCustomizedCmd;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mIsCustomizedCmd == false) {
				cmd = mSpinner.getSelectedItem().toString();				
			} else {
				cmd = mEtCmd.getText().toString();
			}
			Log.e("cmd", cmd + " " + mIsCustomizedCmd);
			
			Thread th = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					output = "";
					try {
						output = mChassis.postRequest(cmd);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
			th.start();
			while(th.isAlive());
					
			if (output.isEmpty() == false) {
				String tmp = output.substring(25, output.length() - 14);
				mTvResult.setText(tmp.replace("\\\\r\\\\n", "\n"));
			} 
		}
	}
	
	
}