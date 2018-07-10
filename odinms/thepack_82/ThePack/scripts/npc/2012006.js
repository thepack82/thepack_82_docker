importPackage(net.sf.odinms.client);

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("See you next time.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {        // first interaction with NPC
			cm.sendNext("#wWelcome #h #, I am #p2012006#.\r\nDid you remember to buy a ticket?");
		} else if (status == 1) {
			cm.sendSimple("#wWhat would you like to do?\r\n\r\n#L0#Take the boat to Ellinia#l\r\n#L1#Take the train to Ludibrium#l\r\n#L2#Go to be warped to Mu Lung#l\r\n#L3#Take the flight to Leafre#l");
			status=1;
		} else if (status == 2) {
				if (selection == 0){
				        if (cm.haveItem(4031047)){	
				        	cm.sendNext("Ok #h #, I will send you to the platform for Ellinia");
				        	cm.gainItem(4031047,-1);
					status = 9;
				         }else{
					cm.sendOk("You do not have a ticket for Ellinia!");
					cm.dispose();
					return;
				         }		
				} 
				if (selection == 1){
				         if (cm.haveItem(4031074)){
					cm.sendNext("Ok #h #, I will send you to the platform for Ludibrium");
				        	cm.gainItem(4031074,-1);
					status = 19;
				          }else{
					cm.sendOk("You do not have a ticket for Ludibrium!");
					cm.dispose();	 
					return;
				          }	
				}
				if (selection == 2){
				          if (cm.haveItem(4031044)){	
				        	 cm.sendNext("Ok #h #, I will send you to the platform for Mu Lung");
					 cm.gainItem(4031044,-1);
				       	 status = 29;
				          }else{
					  cm.sendOk("You do not have a ticket for Mu Lung!");
					  cm.dispose();
					  return;
				          }
				}	
				if (selection == 3){
				          if (cm.haveItem(4031331)){	
				        	 cm.sendNext("Ok #h #, I will send you to the platform for Leafre");
					 cm.gainItem(4031331,-1);
				       	 status = 39;
				          }else{
					  cm.sendOk("You do not have a ticket for Leafre!");
					  cm.dispose();
					  return;
				          }	
				}
		} else if(status == 10){
			cm.warp(200000110, 0);// Ellinia walkway
			cm.dispose();
		} else if (status==20){
			cm.warp(200000120, 0);// Ludi Walkway
			cm.dispose();
		} else if (status==30){
			cm.warp(200000140, 0);// Mu Lung Walkway
			cm.dispose();
		} else if (status==40){
			cm.warp(200000130, 0);// Leafre Walkway
			cm.dispose();
		}
	}
}