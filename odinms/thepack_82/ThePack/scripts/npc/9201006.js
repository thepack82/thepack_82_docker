/**
	Debbie
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
		
    switch (status) {
        case 0:
            cm.sendNext("I only warp out people who are here by accident.");
            break;
        case 1:
            var engagementRings = new Array(4031360, 4031358, 4031362, 4031364);
            var hasEngagement = false;
            for (var x = 0; x < engagementRings.length && !hasEngagement; x++) {
                if (cm.haveItem(engagementRings[x], 1))
                    hasEngagement = true;
            }
            if (cm.haveItem(4000313) && hasEngagement) {
                cm.sendOk("Please continue with the wedding.");
                cm.dispose();
            } else {
                cm.warp(680000000,0);
                cm.dispose();
            }
            break;
        case 2:
            cm.sendOk("You do not have the required item to continue through this wedding.");
            break;
    }
}
