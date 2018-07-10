/**
 *2022004.js - Tylus after the PQ
 *@author Jvlaple
 */
importPackage(net.sf.odinms.client); 
 
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.sendOk("Make up your mind and visit me again.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (cm.getQuestStatus(6192).equals(net.sf.odinms.client.MapleQuestStatus.Status.STARTED)) {		
			if (status == 0) {
				cm.sendNext("Hey, you saved me from all those #bCrimson Balrogs#k and #bLycanthropes#k!");
			} else if (status == 1) {
				cm.gainItem(4031495, 1);
				cm.warp(211000001, 0);
				cm.dispose();
			}
		} else {
			if (status == 0) {
				cm.sendNext("Thank you for protecting me from all those #bCrimson Balrogs#k and #bLycanthropes#k!");
			} else if (status == 1) {
				cm.warp(211000001, 0);
				cm.dispose();
			}
		}
	}
}
				