/*
Donator NPC
Author: Moogra
*/
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple ("So you want to use your donation points? What do you want to do? \n You currently have " + cm.getDonatorPoints() + " donator points. \b\r\n#L0#Become a Donator (10 points)!\n\#l\r\n #L1#Get a GM Hat (5 points) \n\#l\r\n #L2#Buy Rebirths (10 for 1 donator point)\n\#l\r\n#L3#Get 1000 AP (1 donator point)");
        } else if (status == 1) {
            switch(selection) {
                case 0:
                    if (cm.getDonatorPoints() > 10) {
                        cm.setGMLevel(1);
                        cm.gainDonatorPoints(-10);
                        cm.dispose();
                    } else {
                        cm.sendSimple("You don't have 10 donator points.");
                        cm.dispose();
                    }
                    break;
                case 1:
                    if (cm.getDonatorPoints() > 5) {
                        cm.gainItem(1002140,1);
                        cm.gainDonatorPoints(-5);
                        cm.dispose();
                    } else {
                        cm.sendSimple("You don't have 5 donator points.");
                        cm.dispose();
                    }
                    break;
                case 2:
                    if (cm.getDonatorPoints() > 0) {
                        cm.gainReborns(10);
                        cm.gainDonatorPoints(-1);
                        cm.dispose();
                    } else {
                        cm.sendSimple("You don't have any donator points.");
                        cm.dispose();
                    }
                    break;
                case 3:
                    if (cm.getDonatorPoints() > 0) {
                        cm.gainAP(1000);
                        cm.gainDonatorPoints(-1);
                        cm.dispose();
                    } else {
                        cm.sendSimple("You don't have any donator points.");
                        cm.dispose();
                    }
                    break;
            }
        }
    }
}