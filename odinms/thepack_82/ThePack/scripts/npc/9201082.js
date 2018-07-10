//Author: Moogra
var status = 1;
var gain = 1000;
var cost = 10000000;

function start() {
    cm.sendSimple("Hi #b#h ##k. Which of the following deals do you want?\r\n#L0##b" + gain + "  NX (" + cost + " mesos)#k #l\r\n#L1##b" + 10 * gain + " NX (" + cost * 9 + " mesos)#k #l");
}
function action(mode, type, selection) {
    if (mode < 1) cm.dispose();
    if (status == 1) {
        if (selection == 0) {
            if (cm.getMeso() >= cost) {
                cm.sendSimple("What would you like?\r\n#L0#Paypal NX\r\n#L1#Maple Points\r\n#L2#Nexon Game Card Cash");
                status++;
            } else {
                cm.sendOk("You don't have enough mesos!");
                cm.dispose();
            }
        } else if (selection == 1) {
            cost *= 9;
            gain *= 10;
            if (cm.getMeso() >= cost) {
                cm.sendSimple("What would you like?\r\n#L0#Paypal NX\r\n#L1#Maple Points\r\n#L2#Nexon Game Card Cash");
                status++;
            } else {
                cm.sendOk("You don't have enough mesos!");
                cm.dispose();
            }
        }
    }  else if (status == 2) {
        cm.modifyNX(gain,Math.pow(2,selection));
        cm.gainMeso(-cost);
        cm.dispose();
    }
}