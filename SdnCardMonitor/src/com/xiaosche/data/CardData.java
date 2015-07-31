package com.xiaosche.data;


public class CardData {
    public Card[] card = new Card[10];
    public int status;
    public void CardDataInit() {
    	for (int i = 0; i < card.length; i++) {
    	    card[i] = new Card();
    		card[i].cardinit();
    	}
    	status = CardStatus.CHASSIS_NOT_CONNECTION;
    }
    public static int cardnum2index (int cardnum) {
    	if (cardnum == 4 || cardnum == 5)
    		return (cardnum - 4);
    	if (cardnum >= 6 && cardnum <= 9)
    		return (cardnum - 2);
		return cardnum;
    }
    
    public static int index2cardnum (int index, boolean isLinecard) {
    	if (isLinecard == false) { //sup
    	    if (index == 0 || index == 1)
    	    	return (index + 4);
    	} else {                   //LC
    		if (index >= 4 && index <= 7)
    			return (index + 2);
    	}
    	return index;
    }
    
    public String tostring() {
    	String output = "";
    	for (int i = 0; i < card.length; i++) {
    		output = output + "card: " + i + " isExisting: " + card[i].isExisting + " mode: " + card[i].mode +
    				" role: " + card[i].role + " cylon status: " + card[i].cylon_status + " cylon fail time: " +
    				card[i].cylon_fail_time + " pic status: " + card[i].pic_status + " pic fail time: " + 
    				card[i].pic_fail_time + " description: " + card[i].description + "\n";
    	}
    	return output;
    }
}
