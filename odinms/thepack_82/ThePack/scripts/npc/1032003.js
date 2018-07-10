/* Shane
2050: JQ1: 101000100 [end npc: 1043000, item: 4031020 x1]
2051: JQ2: 101000102 [end npc: 1043001, item: 4031032 x1]

By Moogra - More GMS like
*/

importPackage(net.sf.odinms.client);

var status = 0;
var zones = 0;
var maps = Array(101000100, 101000102);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 0 && mode == 0) {
            cm.sendOk("Alright, see you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendYesNo("Hello. Would you like to enter the jump quest?");
            if (!cm.getQuestStatus(2050).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones++;
            if (!cm.getQuestStatus(2051).equals(MapleQuestStatus.Status.NOT_STARTED))
                zones++;
        } else if (status == 1) {
            if (zones == 0) {
                cm.sendOk("Sorry, you are not allowed in.");
                cm.dispose();
            } else {
                cm.warp(maps[zones-1],0);
                cm.dispose();
            }
        }
    }
}	