/* Pison
	Warp NPC to Florina Beach (110000000)
	located in Lith Harbor (104000000)
 */

importPackage(net.sf.odinms.server.maps);

var status = 0;

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
        if (status == 0) 
            cm.sendNext("Hi, I drive a Boat.");
        else if (status == 1)
            cm.sendNextPrev("I can take you to Florina Beach for just a small fee.")
        else if (status == 2) {
            if (cm.getMeso() < 1500) {
                cm.sendOk("You do not have enough mesos.")
                cm.dispose();
            } else 
                cm.sendYesNo("So you want to pay #b1500 mesos#k and leave for Florina Beach? Alright, then, but just be aware that you may be running into some monsters around there, too. Okay, would you like to head over to Florina Beach right now?");
        } else if (status == 3) {
            cm.gainMeso(-1500);
            cm.getPlayer().saveLocation(SavedLocationType.FLORINA);
            cm.warp(110000000, 0);
            cm.dispose();
        }
    }
}