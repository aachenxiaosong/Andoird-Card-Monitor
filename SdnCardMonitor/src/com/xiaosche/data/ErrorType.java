package com.xiaosche.data;


public class ErrorType {
    public static final int LC_FAIL = 0;
    public static final int SUP_FAIL = 1;
    public static final int MODEM_OFFLINE = 2;
    public static final int MODEM_FLAPING = 3;
    public static final int TRACE_BACK = 4;
    public static final int LCCRASH = 5;
    public static final int SUPCRASH = 6;
    public static final String[] ErrorList = new String[]{
    	"LC FAIL",
    	"SUP FAIL",
    	"MODEM OFFLINE",
    	"MODEM FLAPING",
    	"TRACEBACK",
    	"LC CRASH",
    	"SUP CRUSH"
    };
}
