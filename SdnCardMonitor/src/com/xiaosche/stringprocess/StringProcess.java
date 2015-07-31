package com.xiaosche.stringprocess;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.example.sdncardmonitor.R;
import com.xiaosche.data.CardData;
import com.xiaosche.data.CardMode;
import com.xiaosche.data.CardRole;
import com.xiaosche.data.CardStatus;
import com.xiaosche.data.ErrorType;
import com.xiaosche.data.GlobalMacro;
import com.xiaosche.sdncardmonitor.ChassisFragment;

public class StringProcess {
	private final static String SYLONE = "CBR-CCAP-LC-40G";
	private final static String STEALSTARPIC = "CBR-RF-PROT-PIC";
	private final static String SOLARIAPIC = "CBR-RF-PIC";
	private final static String SUP = "CBR-CCAP-SUP-160G";
	private final static String SUP1 = "CBR8-RP-ESP";
	private final static String ACTIVE = "active";
	private final static String STANDBY = "standby";
	private CardData mCardData;
	private Context mContext;
	private int mId;
	private String mChassisName;
	 
	
	public StringProcess(Context context, CardData cd, int id, String name) {
		mContext = context;
		mCardData = cd;
		mId = id;
		mChassisName = name;
	}
	
    public String[] preProcess(String input) {
    	String output = input.replace("\\\\r\\\\n", "\n");
    	return output.split("\n");
    }
    
    private void popMsg(int cardnum, int errortype) {
    	//title: chassis num + lc num + errortype
    	//"Cylone Card Error!!", "cylone card " + cardnum + " is failed", ErrorType.LC_FAIL
    	String msg = null;
    	String msgTitle = "Chassis " + mChassisName + " LC " + cardnum + " " + ErrorType.ErrorList[errortype];
    	int notificationid = mId * 100 + cardnum * 10 + errortype;
    	
    	switch (errortype) {
    	case ErrorType.LC_FAIL:
    		msg = "LC " + cardnum + " cannot boot up";
    		break;
    	case ErrorType.SUP_FAIL:
    		msg = "SUP " + (cardnum - 4) + " cannot boot up";
    		break;
    	case ErrorType.MODEM_OFFLINE:
    		msg = "Too many offline modems on LC " + cardnum;
    		break;
    	case ErrorType.MODEM_FLAPING:
    		msg = "Too many flapping modems on LC " + cardnum;
    		break;
    	case ErrorType.TRACE_BACK:
    		msg = "Click LC to check the trace back";
    		break;
    	case ErrorType.LCCRASH:
    		msg = "LC " + cardnum + " crash";
    		break;
    	case ErrorType.SUPCRASH:
    		msg = "SUP " + (cardnum - 4) + " crash";
    		break;
    	default:
    		break;
    	}
    	mCardData.card[cardnum].errorArr[errortype] = msgTitle + " " + msg;
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification.Builder(mContext).setAutoCancel(true).setSmallIcon(R.drawable.error_msg).
				setTicker(msgTitle).setContentTitle(msgTitle).setContentText(msg).build();
		notificationManager.notify(notificationid, notification);
	}
    
    private void clrMsg(int cardnum, int errortype) {
    	mCardData.card[cardnum].errorArr[errortype] = "";
	}
    
	public void platformInfoProcess(String input) {
    	String[] output;
    	int cardnum;
    	boolean arrofExisting[] = new boolean[10];
    	/* one place to check if connection complete */
    	if (input == null || (input.contains(SUP) || input.contains(SUP1)) == false) {
        	if (mCardData.status != CardStatus.CHASSIS_NOT_CONNECTION) {
        		mCardData.CardDataInit();
        		mCardData.status = CardStatus.CHASSIS_NOT_CONNECTION;
        	}
        	return;
        } else {
        	mCardData.status = CardStatus.CHASSIS_OK;
        }
    	
    	for (int i = 0; i < arrofExisting.length; i++) {
    		arrofExisting[i] = false;
    	}
    	
        output = preProcess(input);
        
    	for (int i = 0; i < output.length; i++) {
    		
    		if (output[i].contains(SYLONE)) {
    			
    			cardnum = output[i].charAt(0) - '0';
    			arrofExisting[cardnum] = true;
    			if (output[i].contains("ok")) {
    			    mCardData.card[cardnum].cylon_status = CardStatus.OK;
    			    mCardData.card[cardnum].cylon_fail_time = 0;
    			    //clrMsg(cardnum, ErrorType.LC_FAIL);
    			} else {
    				if (mCardData.card[cardnum].cylon_status == CardStatus.OK) {
    					mCardData.card[cardnum].cylon_status = CardStatus.WARNING;
    					mCardData.card[cardnum].cylon_fail_time = System.currentTimeMillis()/1000;
    					mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "Chassis " + mChassisName + " LC " + cardnum + " may faild";
    				} else if(mCardData.card[cardnum].cylon_status == CardStatus.WARNING) {
    					/* LC not up more than FAIL_TIME s, treat it as failed */
    					if (System.currentTimeMillis()/1000 > mCardData.card[cardnum].cylon_fail_time + GlobalMacro.CARD_FAIL_WAIT_TIME) {
    						mCardData.card[cardnum].cylon_status = CardStatus.ERROR;
    						popMsg(cardnum, ErrorType.LC_FAIL);
    						mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "";
    					}
    				} else if (mCardData.card[cardnum].cylon_status == CardStatus.ERROR) {
    					
    				}
    				mCardData.card[cardnum].role = CardRole.INIT;///
    				
    			}
    		} else if (output[i].contains(STEALSTARPIC) || output[i].contains(SOLARIAPIC)) {
    			
    			cardnum = output[i].charAt(0) - '0';
    			arrofExisting[cardnum] = true;
    			//mCardData.card[cardnum].isExisting = true;
    			if (output[i].contains("ok")) {
    			    mCardData.card[cardnum].pic_status = CardStatus.OK;
    			    mCardData.card[cardnum].pic_fail_time = 0;
    			    if (mCardData.card[cardnum].cylon_status == CardStatus.OK) {
    			    	clrMsg(cardnum, ErrorType.LC_FAIL);
    			    	mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "";
    			    }
    			} else {
    				if (mCardData.card[cardnum].pic_status == CardStatus.OK) {
    					mCardData.card[cardnum].pic_status = CardStatus.WARNING;
    					mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "Chassis " + mChassisName + " LC " + cardnum + " may faild";
    					mCardData.card[cardnum].pic_fail_time = System.currentTimeMillis()/1000;
    				} else if(mCardData.card[cardnum].pic_status == CardStatus.WARNING) {
    					/* LC not up more than FAIL_TIME s, treat it as failed */
    					if (System.currentTimeMillis()/1000 > mCardData.card[cardnum].pic_fail_time + GlobalMacro.CARD_FAIL_WAIT_TIME) {
    						mCardData.card[cardnum].pic_status = CardStatus.ERROR;
    						popMsg(cardnum, ErrorType.LC_FAIL);
    						mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "";
    					}
    				} else if (mCardData.card[cardnum].pic_status == CardStatus.ERROR) {
    					
    				}
    				mCardData.card[cardnum].role = CardRole.INIT;///
    			}
    			
    		} else if ((output[i].contains(SUP) || output[i].contains(SUP1)) && (output[i].contains("SUP"))) {
    			
    			cardnum = output[i + 3].charAt(1) - '0';
    			//Log.e("SUPNUM", output[i+3] + " cardnum=" + cardnum + "");
    			arrofExisting[cardnum] = true;
    			//mCardData.card[cardnum].isExisting = true;
    			if (output[i + 3].contains("ok")) {
    				mCardData.card[cardnum].cylon_status = CardStatus.OK;
    			    mCardData.card[cardnum].cylon_fail_time = 0;
    			    clrMsg(cardnum, ErrorType.LC_FAIL);
    			    mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "";
    			} else {
    				if (mCardData.card[cardnum].cylon_status == CardStatus.OK) {
    					mCardData.card[cardnum].cylon_status = CardStatus.WARNING;
    					mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "Chassis " + mChassisName + " SUP " + (cardnum - 4) + " may faild";
    					mCardData.card[cardnum].cylon_fail_time = System.currentTimeMillis()/1000;
    				} else if(mCardData.card[cardnum].cylon_status == CardStatus.WARNING) {
    					/* LC not up more than FAIL_TIME s, treat it as failed */
    					if (System.currentTimeMillis()/1000 > mCardData.card[cardnum].cylon_fail_time + GlobalMacro.CARD_FAIL_WAIT_TIME) {
    						mCardData.card[cardnum].cylon_status = CardStatus.ERROR;
    						popMsg(cardnum, ErrorType.SUP_FAIL);
    						mCardData.card[cardnum].warningArr[ErrorType.LC_FAIL] = "";
    					}
    				} else if (mCardData.card[cardnum].cylon_status == CardStatus.ERROR) {
    					
    				}
    			}
    			
    			if (output[i + 4].contains("ok")) {
    				mCardData.card[cardnum].pic_status = CardStatus.OK;
    			    mCardData.card[cardnum].pic_fail_time = 0;
    			} else {
    				if (mCardData.card[cardnum].pic_status == CardStatus.OK) {
    					mCardData.card[cardnum].pic_status = CardStatus.WARNING;
    					mCardData.card[cardnum].pic_fail_time = System.currentTimeMillis()/1000;
    				} else if(mCardData.card[cardnum].pic_status == CardStatus.WARNING) {
    					if (System.currentTimeMillis()/1000 > mCardData.card[cardnum].pic_fail_time + GlobalMacro.CARD_FAIL_WAIT_TIME) {
    						mCardData.card[cardnum].pic_status = CardStatus.ERROR;
    					}
    				} else if (mCardData.card[cardnum].pic_status == CardStatus.ERROR) {
    					
    				}
    			}
    			
    			if (output[i + 3].contains(ACTIVE)) {
    				mCardData.card[cardnum].role = CardRole.ACTIVE;
    			} else if (output[i + 3].contains(STANDBY)) {
    				mCardData.card[cardnum].role = CardRole.STANDBY;
    			} else {
    				mCardData.card[cardnum].role = CardRole.INIT;
    			}
    		} 
    	}
    	for (int i = 0; i < arrofExisting.length; i++) {
    		if (arrofExisting[i] == true) {
    			mCardData.card[i].isExisting = true;
    		} else {
    			mCardData.card[i].isExisting = false;
    			mCardData.card[i].cardinit();//card does not exist, clean related data
    		}
    	}
    }
	
	public void redunLCAllInfonProcess(String input) {
		int cardnum;
		String output[] = preProcess(input);
		for (int i = 0; i < output.length; i++) {
			if (output[i].contains("Active") ||
					output[i].contains("Standby") ||
					output[i].contains("None") ||
					output[i].contains("Init")) {
				cardnum = output[i].charAt(1) - '0';
				mCardData.card[cardnum].isExisting = true;
				
				String tmp[] = output[i].split("\\s{1,}"); //split by space
				int tmplen = tmp.length;
				if (tmp[tmplen - 1].contains("Primary")) {
					//primary
					mCardData.card[cardnum].mode = CardMode.PRIMARY;
				} else if (tmp[tmplen - 1].contains("Secondary")) {
					//secondary
					mCardData.card[cardnum].mode = CardMode.SECONDARY;
				} else {
					//not in group
					mCardData.card[cardnum].mode = CardMode.NOTINGROUP;
				}
				
				if (tmp[tmplen - 2].contains("Standby")) {
					//stby
					mCardData.card[cardnum].role = CardRole.STANDBY;
				} else if (tmp[tmplen - 2].contains("Active")) {
					//active
					mCardData.card[cardnum].role = CardRole.ACTIVE;
				} else {
					//init
					mCardData.card[cardnum].role = CardRole.INIT;
				}
			}
		}
	}
	
	public void showModemProcess(String input) {
		int cardnum;
		String output[] = preProcess(input);
		for (int i = 0; i < 10; i++) {
			if (i == 4 || i == 5)
				continue;
			mCardData.card[i].modem.lasttotal = mCardData.card[i].modem.total;
			mCardData.card[i].modem.lastonline = mCardData.card[i].modem.online;
			mCardData.card[i].modem.total = 0;
			mCardData.card[i].modem.online = 0;
		}
		for (int i = 0; i < output.length; i++) {
			if (output[i].matches("C[0-9]/0.*")) {
				cardnum = output[i].charAt(1) - '0';
				String tmp[] = output[i].split("\\s{1,}"); //split by space
				mCardData.card[cardnum].modem.total += Integer.parseInt(tmp[1]);
				mCardData.card[cardnum].modem.online += Integer.parseInt(tmp[2]);
			}
		}
		
		
		for (int i = 0; i < 10; i++) {
			if (i == 4 || i == 5)
				continue;
			//Log.e("modem num", "card " + i + " : total " + mCardData.card[i].modem.total + " online " + mCardData.card[i].modem.online);
			if (mCardData.card[i].modem.total != 0) {
				if ((float) (mCardData.card[i].modem.online)
						/ (float) (mCardData.card[i].modem.total) < 0.2) {
					Log.e("online total", "online " + (float) (mCardData.card[i].modem.online) + " total " + (float) (mCardData.card[i].modem.total));
					if (mCardData.card[i].errorArr[ErrorType.MODEM_OFFLINE] == "") {//already poped
						popMsg(i, ErrorType.MODEM_OFFLINE);
					}
				} else {
					clrMsg(i, ErrorType.MODEM_OFFLINE);
				}
			}
			
			
		}
	}
	
	public void showCableFlapProcess(String input) {
		String output[] = preProcess(input);
		for (int i = 0; i < output.length; i++) {
			if (output[i].matches("C[0-9]/0.*")) {
				String tmp[] = output[i].split("\\s{1,}");
				
			}
		}
	}
	
	
	public int getCardStatus() {
		
		if (mCardData.status == CardStatus.CHASSIS_NOT_CONNECTION) {
			return mCardData.status;
		}
		mCardData.status = CardStatus.CHASSIS_OK;
		/*
		for (int i = 0; i < mCardData.card.length; i++) {
			if (mCardData.card[i].cylon_status == CardStatus.ERROR ||
				mCardData.card[i].pic_status == CardStatus.ERROR) {
				// chassis is error as long as there is one LC error
				mCardData.status = CardStatus.CHASSIS_ERROR;
				return mCardData.status;
			}
			if (mCardData.card[i].cylon_status == CardStatus.WARNING ||
				mCardData.card[i].pic_status == CardStatus.WARNING) {
				// chassis may be(if not error) warning if one LC is warning
				mCardData.status = CardStatus.CHASSIS_WARNING;
				continue;
			}
		}*/

		for (int i = 0; i < mCardData.card.length; i++) {

			for (int j = 0; j < ErrorType.ErrorList.length; j++) {
				if (mCardData.card[i].errorArr[j].isEmpty() == false) {
					mCardData.status = CardStatus.CHASSIS_ERROR;
					return mCardData.status;
				}
			}

			for (int j = 0; j < ErrorType.ErrorList.length; j++) {
				if (mCardData.card[i].warningArr[j].isEmpty() == false) {
					mCardData.status = CardStatus.CHASSIS_WARNING;
					return mCardData.status;
				}
			}
		}
		return mCardData.status;
	}
	
	/*public static void cardInforLogout() {
		
	}*/
	
}
