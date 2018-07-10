// Jane the Alchemist
importPackage(net.sf.odinms.client);
var status = 0;
var amount = -1;
var item;
var cost;
var rec;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (status == 0 && mode == 1) {
        if (cm.getQuestStatus(2013).equals(MapleQuestStatus.Status.COMPLETED)) {
            var selStr = "It's you ... thanks to you I was able to get a lot done. Nowadays I've been making a bunch of items. If you need anything let me know.";
            cm.sendNext(selStr);
        } else {
            if (cm.getQuestStatus(2010).equals(MapleQuestStatus.Status.COMPLETED))
                cm.sendNext("You don't seem strong enough to be able to purchase my potion ...");
            else
                cm.sendOk("My dream is to travel everywhere, much like you. My father, however, does not allow me to do it, because he thinks it's very dangerous. He may say yes, though, if I show him some sort of a proof that I'm not the weak girl that he thinks I am ...");
            cm.dispose();
        }
    } else if (status == 1 && mode == 1){
        var selStr = "Which item would you like to buy?#b";
        var items = new Array(2000002,2022003,2022000,2001000);
        var costs = new Array(310,1060,1600,3120);
        for (var i = 0; i < items.length; i++)
            selStr += "\r\n#L" + i + "##z" + items[i] + "# (Price : " + costs[i] + " mesos)#l";
        cm.sendSimple(selStr);
    } else if (status == 2 && mode == 1) {
        var itemSet = new Array(2000002,2022003,2022000,2001000);
        var costSet = new Array(310,1060,1600,3120);
        var recHpMp = new Array(300,1000,800,1000);
        var prompt;
        item = itemSet[selection];
        cost = costSet[selection];
        rec = recHpMp[selection];
        if (selection == 0 || selection == 1)
            prompt = "You want #b#t" + item + "##k? #t" + item + "# allows you to recover " + rec + " HP. How many would you like to buy?";
        else if (selection == 2)
            prompt = "You want #b#t" + item + "##k? #t" + item + "# allows you to recover " + rec + " MP. How many would you like to buy?";
        else if (selection == 3)
            prompt = "You want #b#t" + item + "##k? #t" + item + "# allows you to recover " + rec + " HP and MP. How many would you like to buy?";
        cm.sendGetNumber(prompt, 1, 1, 100);
    } else if (status == 3 && mode == 1) {
        cm.sendYesNo("Will you purchase #r" + selection + "#k #b#t" + item + "#(s)#k? #t" + item + "# costs " + cost + " mesos for one, so the total comes out to be #r" + cost * selection + "#k mesos.");
        amount = selection;
    } else if (status == 3 && mode == 0) {
        cm.sendNext("I still have quite a few of the materials you got me before. The items are all there so take your time choosing.");
        cm.dispose();
    } else if (status == 4 && mode == 1) {
        if (cm.getMeso() < cost * amount)
            cm.sendNext("Are you lacking mesos by any chance? Please check and see if you have an empty slot available at your etc. inventory, and if you have at least #r" + cost * selectedItem + "#k mesos with you.");
        else {
            if (cm.canHold(item)) {
                cm.gainMeso(-cost * amount);
                cm.gainItem(item, amount);
                cm.sendNext("Thank you for coming. Stuff here can always be made so if you need something, please come again.");
            } else
                cm.sendNext("Are you lacking mesos by any chance? Please check and see if you have an empty slot available at your etc. inventory, and if you have at least #r" + cost * selectedItem + "#k mesos with you.");
        }
        cm.dispose();
    }
}