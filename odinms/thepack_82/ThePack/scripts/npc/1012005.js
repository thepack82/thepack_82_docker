/*
	Author: Frisby
	NPC   : Cloy
	Use   : Pet Quest Entrance NPC.
	Quest : 100200
 */

importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.tools);

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
            cm.sendOk("What a shame... \r I thought that you had the #rpower#k #bwithin#k.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getQuestStatus(100200) == MapleQuestStatus.Status.COMPLETED) {
                cm.sendNext("I sell pets. Pets are a great companion to your daily adventures! My friend Doofus sells equipment and things you might need for 'em.");
                status = 2;
            } else {
                cm.sendYesNo("Hey there! You probably want a #rpet#k right? Well you came to the right place. But, however you'll have to earn your pet. Are you upto it?");
            }
        } else if (status == 1) {
            cm.sendSimple("Thats great then if you have nothing left to do here I can warp you to the quest. \r\n#L0#Yes! Lets do this, I want a pet!#l \r\n#L1#I fail at life#l");
        } else if (status == 2) {
            if (selection == 0) {
                cm.warp(109010110, 0);	
                cm.startQuest(100200);
                cm.sendOk("Find a jewel and give it to Pettite in exchange!");
                cm.dispose();		
            } else if (selection == 1) {
                cm.sendOk("We all fail sometimes.");
                cm.dispose();
            }
        } else if (status == 3) {
            var items = new Array (5000000, 5000001, 5000002, 5000003, 5000004, 5000005, 5000006, 5000007, 5000008, 5000009, 5000010, 5000011, 5000012, 5000013, 5000014, 5000015, 5000017,/* 5000018,*/ 5000020,/* 5000021,*/ 5000022, 5000023, 5000024, 5000025,/* 5000026,*/ 5000029, 5000034, 5000036, 5000037, 5000039, 5000041, 5000044, 5000045);
            var selStr = "So, what is your choice? Remember, it'll cost you #b50,000,000 mesos#k for each pet I sell. Those critters aren't easy to catch!";
            for (var i = 0; i < items.length; i++){
                selStr += "\r\n#b#L" + i + "# #v" + items[i] + "# #t" + items[i] + "##l#k";
            }
            cm.sendSimple(selStr);
        } else if (status == 4) {
            if (cm.getMeso() < 50000000) {
                cm.sendOk("You do not have enough mesos.");
                cm.dispose();
            } else {
                cm.gainMeso(-50000000);
                cm.gainItem(items[selection]);
                cm.dispose();
            }
        }
    }
}
