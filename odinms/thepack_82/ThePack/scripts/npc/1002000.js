/* Phil
	Warp NPC
	- Lith Harbour
 */

var status = 0;
var maps = Array(102000000, 101000000, 100000000, 103000000);
var cost = Array(1200, 1200, 800, 1000);
var costBeginner = Array(120, 120, 80, 100);
var job;

importPackage(net.sf.odinms.client);

function start() {
    cm.sendNext("Hi, I'm Phil.");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Alright, see you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendNextPrev("I can take you to various locations for just a small fee. Beginners will get a 90% discount on normal prices.")
        else if (status == 2) {
            var selStr = "Select your destination.#b";
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) {
                for (var i = 0; i < maps.length; i++)
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " meso)#l";
            } else {
                for (var i = 0; i < maps.length; i++)
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " meso)#l";
            }
            cm.sendSimple(selStr);
        } else if (status == 3) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) {
                if (cm.getMeso() < costBeginner[selection]) {
                    cm.sendOk("You do not have enough mesos.")
                    cm.dispose();
                    return;
                }
            } else {
                if (cm.getMeso() < cost[selection]) {
                    cm.sendOk("You do not have enough mesos.")
                    cm.dispose();
                    return;
                }
            }
            cm.sendYesNo("So you have nothing left to do here? Do you want to go to #m" + maps[selection] + "#?");
        } else if (status == 4) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER))
                cm.gainMeso(-costBeginner[selection]);
            else
                cm.gainMeso(-cost[selection]);
            cm.warp(maps[selection], 0);
            cm.dispose();
        }
    }
}