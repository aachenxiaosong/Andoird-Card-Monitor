package com.xiaosche.sdncardmonitor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sdncardmonitor.R;
import com.xiaosche.data.CardData;
import com.xiaosche.data.ErrorType;
import com.xiaosche.stringprocess.StringProcess;

public class ChassisFragment extends Fragment {

	/************** global variables definition *************/
	boolean mDebug = false;
	
	public RelativeLayout mCard[] = new RelativeLayout[10];	
	public TextView mCardText[] = new TextView[10];

	public CardData mCardData;
	public StringProcess mStringprocess;
	
	
	//private String mChassisName;
	private Context mContext;
	private String mChassisIp;
	private String mChassisName;
	private int mId;
	private boolean mIsDeleted;
	private boolean mHasWaterMark;
	
	private RelativeLayout mOverallView;
	
	public ChassisFragment(Context context, String name, String ip, int id) {
		mContext = context;
		mChassisName = name;
		mChassisIp = ip;
		mId = id;
		mIsDeleted = false;
		mHasWaterMark = false;
		mCardData = new CardData();
		mCardData.CardDataInit();//first time initialization mCard data
	}

	public String getChassisIp() {
		return mChassisIp;
	}
	public String getChassisName() {
		return mChassisName;
	}
	public boolean isDeleted() {
		return mIsDeleted;
	}
	public void setDeleted(boolean isdelete) {
		mIsDeleted = isdelete;
	}
	
	public boolean hasWaterMark() {
		return mHasWaterMark;
	}
	
	public void setWaterMark(boolean isdelete) {
		mHasWaterMark = isdelete;
	}
	
	/******************** Methods definition ******************/
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment, container, false);
		return view;
		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		
		
		mStringprocess = new StringProcess(mContext, mCardData, mId, mChassisName);
		
		mCardText[0] = (TextView)view.findViewById(R.id.lctext0);
		mCardText[1] = (TextView)view.findViewById(R.id.lctext1);
		mCardText[2] = (TextView)view.findViewById(R.id.lctext2);
		mCardText[3] = (TextView)view.findViewById(R.id.lctext3);
		mCardText[4] = (TextView)view.findViewById(R.id.suptext0);
		mCardText[5] = (TextView)view.findViewById(R.id.suptext1);
		mCardText[6] = (TextView)view.findViewById(R.id.lctext6);
		mCardText[7] = (TextView)view.findViewById(R.id.lctext7);
		mCardText[8] = (TextView)view.findViewById(R.id.lctext8);
		mCardText[9] = (TextView)view.findViewById(R.id.lctext9);
		
		mCard[0] = (RelativeLayout)view.findViewById(R.id.lc0);
		mCard[1] = (RelativeLayout)view.findViewById(R.id.lc1);
		mCard[2] = (RelativeLayout)view.findViewById(R.id.lc2);
		mCard[3] = (RelativeLayout)view.findViewById(R.id.lc3);
		mCard[4] = (RelativeLayout)view.findViewById(R.id.sup0);
		mCard[5] = (RelativeLayout)view.findViewById(R.id.sup1);
		mCard[6] = (RelativeLayout)view.findViewById(R.id.lc6);
		mCard[7] = (RelativeLayout)view.findViewById(R.id.lc7);
		mCard[8] = (RelativeLayout)view.findViewById(R.id.lc8);
		mCard[9] = (RelativeLayout)view.findViewById(R.id.lc9);
		
		for (int i = 0; i < mCard.length; i++) {
			mCard[i].setOnClickListener(new CardClickListener(i));
		}
		
		mOverallView = (RelativeLayout) view.findViewById(R.id.overalllayout);
		mIsDeleted = false;
		mHasWaterMark = false;
	}
	
	public void drawWaterMark(){  
		mOverallView.addView(new WaterMarkView(mContext));  
    }  
  
    public void clearWaterMark(){  
        if(mOverallView.getChildCount() != 1){  
        	mOverallView.removeViewAt(mOverallView.getChildCount()-1);  
        }  
    }
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void getCardInfo(int cardnum) {
		String result = null;
		if (cardnum == 4 || cardnum == 5) {//sup
			try {
				result = postRequest("show version | i image");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			
		}
		
		if (result != null) {
		String[] tmp = mStringprocess.preProcess(result);
			for (int i = 0; i < tmp.length; i++)
				if (tmp[i].contains("bootflash")) {
					tmp[i] = tmp[i].replace("\\", "");
					mCardData.card[cardnum].description = tmp[i];
					break;
				}
		}
	}
	
	public String postRequest(String cmd)
			throws Exception {
		// 创建HttpPost对象。
		HttpPost post = new HttpPost(getResources().getString(R.string.url));
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonObject2 = new JSONObject();  
		jsonObject.put("cmts-ip-address", mChassisIp);
		jsonObject.put("cmts-cli-cmd", cmd);
		jsonObject2.put("input", jsonObject);
		StringEntity entity = new StringEntity(jsonObject2.toString());
		post.setEntity(entity);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");

		// 发送POST请求
		HttpResponse httpResponse = new DefaultHttpClient().execute(post);
		
		// 如果服务器成功地返回响应
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			// 获取服务器响应字符串
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		}
		return null;
	}
	
	class CardClickListener implements OnClickListener {

		private int CardNum;
		public CardClickListener(int cardnum) {
			CardNum = cardnum;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					getCardInfo(CardNum);
				}
				
			}).start();
			
			// display using toast
			/*new AlertDialog.Builder(mContext)
					.setTitle("Add CMTS Device")
					.setMessage(mCardData.card[CardNum].description)
					.setPositiveButton("OK", null).show();*/
			String text = "";
			text += mCardData.card[CardNum].description;
			for (int i = 0; i < ErrorType.ErrorList.length; i++) {
				if (mCardData.card[CardNum].errorArr[i] != "") {
					text += "\n" + "* ";
					text += mCardData.card[CardNum].errorArr[i];
				}
			}
			
			for (int i = 0; i < ErrorType.ErrorList.length; i++) {
				if (mCardData.card[CardNum].warningArr[i] != "") {
					text += "\n" + "O ";
					text += mCardData.card[CardNum].warningArr[i];
				}
			}
			Toast.makeText(v.getContext(), text,
					Toast.LENGTH_LONG).show();
		}
	};
	
}