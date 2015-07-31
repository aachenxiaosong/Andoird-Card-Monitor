package com.xiaosche.data;



public class FlappingModem {
	public String mac;
	public String ip;
	public int lcnum;
	public String md;
	public int miss[] = new int[3];
	public int crc[] = new int[3]; 
	public int padj[] = new int[3];
	public int flap[] = new int[3];
	
	public int simplenum;
	public int isIncrease;
	
	
	public void initFlapModem() {
		mac = "";
		ip = "";
		lcnum = 0;
		md = "";
		for (int i = 0; i < 3; i++) {
			miss[i] = crc[i] = padj[i] = flap[i] = 0;
		}
	}
	
	public boolean isIncrease(int newmiss, int newcrc, int newpadj, int newflap) {
		boolean ret = false;
		if (simplenum < 4) {
			miss[simplenum] = newmiss;
			crc[simplenum] = newcrc;
			padj[simplenum] = newpadj;
			flap[simplenum] = newflap;
			simplenum++;
			return false;
		} else {
			if (newmiss > miss[0] ||
					newcrc > crc[0] ||
					newpadj > padj[0] ||
					newflap > flap[0]) {
				ret = true;
			} 
			miss[0] = miss[1]; miss[1] = miss[2]; miss[2] = newmiss;
			crc[0] = crc[1]; crc[1] = crc[2]; crc[2] = newcrc;
			padj[0] = padj[1]; padj[1] = padj[2]; padj[2] = newpadj;
			flap[0] = flap[1]; flap[1] = flap[2]; flap[2] = newflap;
		}
		return ret;
	}
}