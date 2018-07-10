var status;  

function start() {  
    status = -1;  
    action(1, 0, 0);  
}  

function action(mode, type, selection) {  
    if (mode == -1 || mode == 0) {
        cm.sendOk("Goodbye then"); 
            cm.dispose();
			return;
    } else if (mode == 1) {
            status++;
        } else {
            status--;
        }
		
        if (status == 0) { 
		cm.sendSimple("Hello were would you like to go?\r\n#L0#Untamed Hearts Hunting Ground#l\r\n#L1#I have 7 keys Bring me to smash boxes#l\r\n#L2#Please warp me out#l");
        } else if (status == 1) {
		if (selection == 0) {
			cm.warp(680000400); 
        	} else if (selection == 1) {
            		if (cm.haveItem(4031217,7))
                		cm.gainItem(4031217, -7);
            		else 
                		cm.sendOk("Seems like you don't have 7 Keys. Kill the cakes and candles in the Untamed Heart Hunting Ground to get keys. "); 
		} else if (selection == 2) {
			cm.warp(680000500);
			cm.sendOk("Goodbye, I hope you enjoyed the wedding!");
		}
		cm.dispose();
	}
}
