//Made by Moogra
//Vicious Old Arrow/Bow maker

importPackage(net.sf.odinms.scripting);//This isn't needed is it

function start() {
    cm.sendYesNo("Do you want to go to the event map?");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.sendOk("All right, come back next time.");
        cm.dispose();
        return;
    } else {
        if (cm.eventOn())
            cm.doEvent();
        cm.dispose();
    }
}
