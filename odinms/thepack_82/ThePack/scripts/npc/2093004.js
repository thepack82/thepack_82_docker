var status = 0;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0) {
        cm.sendYesNo("I am Dolphin! Do you wish to go to Aquaroad?");
        status++;
    } else {
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
            cm.dispose();
        } else {
            if (status == 1) {
                cm.sendNext("Alright, see you next time. Take care.");
                status++;
            } else if (status == 2) {
                cm.warp(230000000, 0);
                cm.dispose();
            }
        }
    }
}
