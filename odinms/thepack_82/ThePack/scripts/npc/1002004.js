/**
-- Odin JavaScript --------------------------------------------------------------------------------
	VIP Cab - Victoria Road : Lith Harbor (104000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var cost = 1000;

function start() {
    cm.sendNext("Hi there! This cab is for VIP customers only. Instead of just taking you to different towns like the regular cabs, we offer a much better service worthy of VIP class. It's a bit pricey, but... for only 10,000 mesos, we'll take you safely to the \r\n#bAnt Tunnel#k.");

}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 1 && mode == 0) {
            cm.sendNext("This town also has a lot to offer. Find us if and when you feel the need to go to the Ant Tunnel Park.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER))
                cm.sendYesNo("We have a special 90% discount for beginners. The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island, where the 24 Hr Mobile Store is. Would you like to go there for #b1,000 mesos#k?");
            else {
                cm.sendYesNo("The regular fee applies for all non-beginners. The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island, where 24 Hr Mobile Store is. Would you like to go there for #b10,000 mesos#k?");
                cost *= 10;
            }
        } else if (status == 2) {
            if (cm.getMeso() < cost) {
                cm.sendNext("It looks like you don't have enough mesos. Sorry but you won't be able to use this without it.")
                cm.dispose();
            } else {
                cm.gainMeso(-cost);
                cm.warp(105070001, 0);
                cm.dispose();
            }
        }
    }
}