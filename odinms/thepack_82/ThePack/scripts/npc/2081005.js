//Fixed by Moogra
//Fixed grammar, javascript syntax
var status = 0;
var price = 2000000;
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
        if (status == 0) 
            cm.sendSimple("Welcome to Cave of Life - Entrance ! Would you like to go inside and fight #rHorntail#k ? If you want to fight him, you may might need some #b#v2000005##k, so you can recover some HP if you have been hit by #rHorntail#k.\r\n#L1#I would like to buy 10 for 100,000 Mesos!#l\r\n\#L2#No thanks, let me in now!#l");
        else if (status == 1) {
            if (selection == 1) {
                if(cm.getMeso() >= 100000) {
                    cm.gainMeso(-100000);
                    cm.gainItem(2000005, 10);
                    cm.sendOk("Thank you for buying the potion. Use it as well!");
                } else {
                    cm.sendOk("Sorry, you don't have enough mesos to buy them!");
                }
                cm.dispose();
            }
            else if (selection == 2) {
                if(cm.getLevel() > 99)
                    cm.warp(240050000, 0);
                else
                    cm.sendOk("I'm sorry. You need to be atleast level 100 or above to enter.");
                cm.dispose();
            }
        }
    }
}