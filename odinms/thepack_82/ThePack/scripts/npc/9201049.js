/**
	Ames the Wise
-- By ---------------------------------------------------------------------------------------------
	Xelkin
-- Edited by --------------------------------------------------------------------------------------
	Angel (get31720 ragezone
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
		var rings = new Array(1112806, 1112803, 1112807, 1112809);
		var hasRing = false;
		for (var x = 0; x < rings.length && !hasRing; x++) {
			if (cm.haveItem(rings[x])) 
				hasRing = true;
		}
		if (hasRing) {
			cm.sendNext("You've reached the end of the wedding. You will recieve an Onyx Chest for Bride and Groom and an Onyx Chest. Exchange them at Pila, she is at the top of Amoria.");
		} else if (cm.haveItem(4000313)) {
			cm.sendNext("Wow the end of the wedding already ? Good bye then.!");
			status = 20;
		} else {
			cm.sendNext("You do not have the Gold Maple Leaf and you do not have a wedding ring so I will take you to Amoria.");
			status = 21;
		}
        } else if (status == 1) {
			cm.warp(680000000);
		cm.gainItem(4031424,1);
		cm.gainItem(4031423,1);
		cm.dispose();  
        } else if (status == 21) {
			cm.warp(680000000);
			cm.gainItem(4000313,-1);
			cm.gainItem(4031423,1);
			} else if (status == 22) {
				cm.warp(680000000);
			}
}
