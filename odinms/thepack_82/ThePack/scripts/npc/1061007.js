importPackage(net.sf.odinms.client);
var status = 0;

function start() {
    cm.sendYesNo("Would you like to leave?");
}

function action(mode, type, selection) {
    if(mode == 1)
        cm.warp(105040300,0);
    cm.dispose();
} 