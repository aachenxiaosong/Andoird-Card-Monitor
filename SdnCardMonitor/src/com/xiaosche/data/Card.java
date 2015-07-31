package com.xiaosche.data;


public class Card {
	public boolean isExisting;
	public int mode;
    public int role;
    public int cylon_status;
    public long cylon_fail_time;
    public int pic_status;
    public long pic_fail_time;
    public String description; //general information
    public String[] warningArr = new String[ErrorType.ErrorList.length]; //warning information
    public String[] errorArr = new String[ErrorType.ErrorList.length];   //error information
    public Modem modem = new Modem();
    public Flapping flapping = new Flapping();
    
    public void cardinit() {
    	isExisting = false;
    	mode = CardMode.NOTINGROUP;
    	role = CardRole.INIT;
    	cylon_status = CardStatus.OK;
    	pic_status = CardStatus.OK;
    	cylon_fail_time = 0;
    	pic_fail_time = 0;
    	description = "";
    	for (int i = 0; i < ErrorType.ErrorList.length; i++) {
    		warningArr[i] = "";
    		errorArr[i] = "";
    	}
    	modem.initModem();
    }
}
