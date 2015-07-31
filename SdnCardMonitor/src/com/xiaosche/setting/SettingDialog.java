package com.xiaosche.setting;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sdncardmonitor.R;
import com.xiaosche.data.ErrorType;

public class SettingDialog extends Dialog {

	List<String> mWarningList;
	DragListAdapter mWarningAdapter;
	List<String> mErrorList;
	DragListAdapter mErrorAdapter;
	
	ListView mWarningLV;
	ListView mErrorLV;
	public SettingDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);	
		setTitle("      Warning                         Error");
		mWarningList = new ArrayList<String>();
		for (int i = 0; i < ErrorType.ErrorList.length; i++) {
			mWarningList.add(ErrorType.ErrorList[i]);
			
		}
		
		
		mWarningAdapter = new DragListAdapter(this.getContext(), mWarningList, true);
		mWarningLV = (ListView)findViewById(R.id.lv_warning_left);
		mWarningLV.setAdapter(mWarningAdapter);
		
		mErrorList = new ArrayList<String>();
		mErrorAdapter = new DragListAdapter(this.getContext(), mErrorList, false);
		mErrorLV = (ListView)findViewById(R.id.lv_error_right);
		mErrorLV.setAdapter(mErrorAdapter);
	}
	
	
	public class DragListAdapter extends BaseAdapter{
		private LayoutInflater mInflater;// ∂ØÃ¨≤ºæ÷”≥…‰  
		private List<String> mList;
		private boolean mIsWarning;
		//private Context mContext;
		public DragListAdapter(Context c, List<String> l, boolean isWarning) {
			mList = l;
			mIsWarning = isWarning;
			this.mInflater = LayoutInflater.from(c);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = mInflater.inflate(R.layout.error_list_item, null);
			TextView tv = (TextView)convertView.findViewById(R.id.tv_errorwarningitem);
			tv.setText(mList.get(position));
			final int index = position;
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (mIsWarning == true) {
						String tmp = mWarningList.get(index);
						mWarningList.remove(index);
						mErrorList.add(tmp);
						mWarningAdapter.notifyDataSetChanged();
						mErrorAdapter.notifyDataSetChanged();
					} else {
						String tmp = mErrorList.get(index);
						mErrorList.remove(index);
						mWarningList.add(tmp);
						mWarningAdapter.notifyDataSetChanged();
						mErrorAdapter.notifyDataSetChanged();
					}
				}
			});
			return convertView;
		}
		
	}
	
}