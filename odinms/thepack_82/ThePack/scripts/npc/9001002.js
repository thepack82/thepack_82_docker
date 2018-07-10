var status = 0;

function start() {
    cm.sendYesNo("Do you want to go to the PvP map and fight with your friends?");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        if (status == 0) {
            cm.sendNext ("Alright, good luck in there!");
            status++;
        } else if (status == 1) {
            cm.warp(800020400, 0);
            cm.dispose();
        }
    }
}  