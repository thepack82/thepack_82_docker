var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
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
        if (status == 0)
            cm.sendNext("Hi, I'm a ticket gate.");
        else if (status == 1)
            cm.sendNextPrev("I can take you back to Kerning City for just a small fee.")
        else if (status == 2) {
            if (cm.getMeso() < 5000) {
                cm.sendOk("You do not have enough mesos.")
                cm.dispose();
            } else
                cm.sendYesNo("The ride to Kerning City of Victoria Island will cost you #b5000 mesos#k. Are you sure you want to return to #bKerning City#k?");
        } else if (status == 3) {
            cm.gainMeso(-5000);
            cm.warp(103000100, 0);
            cm.dispose();
        }
    }
}