/* Mysterious Statue
	1061006

	(crumbling is 1061007)

2052: JQ1: 105040310 [end npc: 1063000, item: 4031025 x10]
2053: JQ2: 105040312 [end npc: 1063001, item: 4031026 x10]
2054: JQ3: 105040314 [end npc: 1063002, item: 4031028 x10]
*/

importPackage(net.sf.odinms.client);

var status = 0;
var zones = 0;
var names = Array("Deep Forest of Patience 1", "Deep Forest of Patience 2", "Deep Forest of Patience 3");
var maps = Array(105040310, 105040312, 105040314);
var selectedMap = -1;

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
            cm.sendNext("You feel a mysterious force surrounding this statue.");
            if (!cm.getQuestStatus(2054).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones = 3;
            else if (!cm.getQuestStatus(2053).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones = 2;
            else if (!cm.getQuestStatus(2052).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones = 1;
            else
                zones = 0;
        } else if (status == 1) {
            if (zones == 0)
                cm.dispose();
            else {
                var selStr = "Its power allows you to will yourself deep inside the forest.#b";
                for (var i = 0; i < zones; i++)
                    selStr += "\r\n#L" + i + "#" + names[i] + "#l";
                cm.sendSimple(selStr);
            }
        } else if (status == 2) {
            cm.warp(maps[selection],0);
            cm.dispose();
        }
    }
}	