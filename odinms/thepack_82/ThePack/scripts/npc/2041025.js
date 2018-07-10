/*
	Machine Apparatus
    By Moogra
 */

function start() {
    cm.sendYesNo("Do you want to leave?");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    } else {
        cm.warp(220080000, 0);
        if (cm.getCharsOnMap() < 2)
            cm.resetReactorsFull();
        else
            cm.resetReactors();
        cm.dispose();
    }
}