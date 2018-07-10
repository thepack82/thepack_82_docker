//Robo Pet Hatcher
//By : Vietno from Ragezone+ OdinTeh Forums
importPackage(net.sf.odinms.client);
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Tehehehe. :)");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            cm.sendNext("I will give you a Robo Baby if you give me \r\n#v5000047# 1 Robo Egg.");
        else if (status == 1) {
            if (cm.haveItem(5000047, 1) && cm.haveItem(5000047, 1) && cm.haveItem(5000047, 1))
                cm.sendYesNo("I am so glad you have them! Here is the \r\n#v5000048# #bRobo Baby#k for you.");
            else {
                cm.sendOk("#h #, You don't have a robo egg. \r\n#v5000047#");
                cm.dispose();
            }
        }
        else if (status == 2) {
            cm.gainItem(5000047, -1);
            cm.gainItem(5000048, 1);
        }
    }
}