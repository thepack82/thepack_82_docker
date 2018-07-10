//Author: Moogra
var status = 0;
var map = Array(240010501);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0)
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("Hello, I am the meso to item converter. Do you want to trade in 1 billion mesos for a #b#v4000313##k? Or do you want to trade in my #b#v4000313##k for 1 billion mesos?\r\n#L1# I would like to trade my #b#v4000313##k for 1 billion mesos!#l\r\n\#L2# I would like to exchange 1 billion mesos for a #b#v4000313##k!#l");
        } else if (status == 1) {
            if (selection == 1) {
                if(cm.haveItem(4000313)) {
                    cm.gainMeso(1000000000);
                    cm.gainItem(4000313,-1);
                    cm.sendOk("Thank you for your mesos!");
                } else
                    cm.sendOk("Sorry, you don't have a #b#v4000313##k!");
                cm.dispose();
            } else if (selection == 2) {
                if(cm.getMeso() >= 1000000000) {
                    cm.gainMeso(-1000000000);
                    cm.gainItem(4000313,1);
                    cm.sendOk("Thank you for your item!");
                } else
                    cm.sendOk("Sorry, you don't have enough mesos!");
                cm.dispose();
            }
            else
                cm.sendOk("All right. Come back later");
        }
    }
}