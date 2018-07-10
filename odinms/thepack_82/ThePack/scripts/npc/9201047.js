/*
9201047 - Glimmer Man 
*/

var status = 0;

importPackage(net.sf.odinms.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 2 && mode == 0) {
			cm.sendOk("Alright, see you next time.");
			cm.dispose();
			return;
		}
		if (mode == 1) {
			status++;
		}
		else {
			status--;
		}
		if (cm.getPlayer().getMapId() == 670010200) {
			if (status == 0 && cm.isLeader()) {
				var num = cm.countMonster();
				if (num == 0) {
					cm.sendOk("Kill this fairy I'm about to spawn, and drop the Hammer onto the mirror to break it.");
					cm.spawnMonster(9400518, -5, 150);
					cm.dispose();
				}else {
					cm.sendOk("Please kill all the monsters in the map before talking to me.");
					cm.dispose();
				}
			} else {
				cm.sendNext("Ask your party leader to talk to me.");
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 670011000) {
			cm.warp(670010000, 0);
			cm.dispose();
		}
	}
}