importPackage(net.sf.odinms.client);

var status = 0;
var zones = 0;
var tickets = Array(4031036, 4031037, 4031038);
var names = Array("Construction site B1", "Construction site B2", "Construction site B3");
var maps = Array(103000900, 103000903, 103000906);
var selectedMap = -1;

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
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("Hi, I'm the ticket gate.");
			if (!(cm.getQuestStatus(2057).equals(MapleQuestStatus.Status.NOT_STARTED))) {
			zones = 3;
			}
			else if (!(cm.getQuestStatus(2056).equals(MapleQuestStatus.Status.NOT_STARTED))) {
			zones = 2;
			}
			else if (!(cm.getQuestStatus(2055).equals(MapleQuestStatus.Status.NOT_STARTED))) {
			zones = 1;
			}
			else {
			zones = 0;
			}
		} else if (status == 1) {
			if (zones == 0) {
			cm.dispose();
			} else {
			var selStr = "Where would you like to go?#b";
			for (var i = 0; i < zones; i++) {
				selStr += "\r\n#L" + i + "#" + names[i] + "#l";
			}
			cm.sendSimple(selStr);
			}
		} else if (status == 2) {
			if (!(cm.haveItem(tickets[selection]))) {
				cm.sendOk("You do not have a ticket.");
				cm.dispose();
			} else {
			selectedMap = selection;
			cm.gainItem(tickets[selectedMap],-1);
			cm.warp(maps[selectedMap],0);
			cm.dispose();
			}
		} 
	}
}	