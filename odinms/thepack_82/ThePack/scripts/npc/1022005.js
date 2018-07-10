/*
Karma NPC
Author: Moogra
*/
var status = 0;
var scrollId = Array(6,7,403,506,603,709,710,711,806,807,903,1024,1025,3003,3103,3203,3303,3703,3803,4003,4103,4203,4303,4403,4503,4603,4703);
var randomId = Math.floor(Math.random()*scrollId.length);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0)
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            if (cm.getKarma() > 0)
                cm.sendSimple ("So you want to use your karma in exchange for some benefits? What do you want to buy? \n You currently have " + cm.getKarma() + " karma. \b\r\n#L0#Become an Intern (40 karma)\n\#l\r\n #L1#Get a GM Hat (10 karma) \n\#l\r\n #L2#Buy a random GM Scroll (2 karma)\n\#l\r\n#L3#Get 1000 AP (1 karma)");
            else {
                cm.sendSimple ("You don't have any karma!");
                cm.dispose();
            }
        else if (status == 1) {
            switch(selection) {
                case 0:
                    if (cm.getKarma() > 39) {
                        cm.setGMLevel(2);
                        cm.gainKarma(-40);
                    } else 
                        cm.sendSimple("You don't have 40 karma.");
                    break;
                case 1:
                    if (cm.getKarma() > 9) {
                        cm.gainItem(1002140,1);
                        cm.gainKarma(-10);
                    } else
                        cm.sendSimple("You don't have 10 karma.");
                    break;
                case 2:
                    if (cm.getKarma() > 1) {
                        cm.gainItem(scrollId[randomId]+2040000,1);
                        cm.gainKarma(-2);
                    } else
                        cm.sendSimple("You don't have 2 karma.");
                    break;
                case 3:
                    cm.gainAP(1000);
                    cm.gainKarma(-1);
                    break;
            }
            cm.dispose();
        }
    }
}