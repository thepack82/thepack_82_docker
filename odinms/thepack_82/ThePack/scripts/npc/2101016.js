/**2101016.js
 *Adrea
 **@author Jvlaple
 */

var status = 0;
var copns;

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
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			copns = cm.getPlayer().countItem(4031868);
			if (copns <= 5) {
				cm.removeAll(4031868);
				cm.sendNext("Get me more #bJewels#k next time if you want #rEXP#k...");
			} else {
				if (!cm.getPlayer().isGM())
				cm.removeAll(4031868);
				cm.sendNext("Thank you for the #bJewels#k.");
				cm.getPlayer().gainExp(5 * cm.getPlayer().getClient().getChannelServer().getExpRate() * copns, true, true);
			}
		} else if (status == 1) {
			cm.warp(980010020, 0);
			cm.dispose();
		}
	}
}