//AP adding/resetting NPC By Moogra

function start() {
    cm.sendYesNo("Do you want a free stat reset?");
}

function action(mode, type, selection) {
    if(mode > 0) {
        cm.resetStats();
        cm.sendOk("Your stats have been reset!");
    }
    cm.dispose();
}