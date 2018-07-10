var status = 0;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0) {
        cm.sendNext("I see Asesson has let you in, you must be worthy of entering! Let's go!");
        status++;
    } else {
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
            cm.dispose();
        } else {
            if (status == 1) {
                cm.warp(670010200, 0);
                cm.dispose();
                cm.changeMusic("Bgm14/Ariant");
            } else {
                cm.sendOk("You need at least one Orbis Rock Scroll.");
                cm.dispose();
            }
        }
    }
}
