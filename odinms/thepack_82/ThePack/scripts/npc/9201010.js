/**
	Assistant Travis
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
---------------------------------------------------------------------------------------------------
**/

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
		if (cm.haveItem(4000313)) {
			cm.sendOk("You are a guest. Please continue with the wedding. I only warp out people who are here by accident.");
			cm.dispose();
		} else {
			cm.sendNext("I warp people out. If you are the newly wed don't click next or you will not be able to collect your prize at the end.");
		}
	} else if (status == 1) {
		cm.warp(680000000);
		cm.sendOk("You do not have the required item to continue through this wedding.");
		cm.dispose();
	}
}