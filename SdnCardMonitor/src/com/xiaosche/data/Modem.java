package com.xiaosche.data;

public class Modem {
	public int total;
	public int online;
	public int lasttotal;
	public int lastonline;
	public Modem () {
		initModem();
	}
	public void initModem () {
		total = online = lasttotal = lastonline = 0;
	}
}