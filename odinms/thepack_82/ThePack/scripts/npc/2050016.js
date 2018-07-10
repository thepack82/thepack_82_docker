var status = 0;

function start() {
    cm.sendSimple("Do you want to go to Free Market 22 to fight some bosses?\r\n#L0#Yes#l\n#L1#No#l");
}

function action(mode, type, selection) {
    if (selection==0)
        cm.warp(910000022,0);
    cm.dispose();
}