importPackage(net.sf.odinms.client);

var status = 0;
var zones = 0;
var tickets = Array(4031036, 4031037, 4031038);
var names = Array("Construction site B1", "Construction site B2", "Construction site B3");
var cost = Array(1000, 1000, 1000);
var selectedItem = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Alright, see you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendNext("Hi, I'm the ticket salesman.");
            if (!cm.getQuestStatus(2055).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones++;
            if (!cm.getQuestStatus(2056).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones++;
            if (!cm.getQuestStatus(2057).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones++;
        } else if (status == 1) {
            if (zones == 0)
                cm.dispose();
            else {
                var selStr = "Which ticket would you like?#b";
                for (var i = 0; i < zones; i++)
                    selStr += "\r\n#L" + i + "#" + names[i] + " (" + cost[i] + " mesos)#l";
                cm.sendSimple(selStr);
            }
        } else if (status == 2) {
            if (cm.getMeso() < cost[selection]) {
                cm.sendOk("You do not have enough mesos.");
                cm.dispose();
            } else {
                cm.gainMeso(-cost[selection]);
                cm.gainItem(tickets[selection],1);
                cm.dispose();
            }
        }
    }
}	