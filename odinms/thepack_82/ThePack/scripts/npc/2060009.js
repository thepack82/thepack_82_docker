function start() {
    cm.sendYesNo("I drive the Dolphin Taxi! Do you want to go to Herb Town?#l");
}

function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(251000100, 0);
    cm.dispose();
}