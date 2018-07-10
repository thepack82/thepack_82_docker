/* Warrior Job Instructor
	Warrior 2nd Job Advancement
	Hidden Street : Warrior's Rocky Mountain (108000300)

TECHNICALLY THIS IS BROKEN, WHAT IS THIS SUPPOSED TO DO?
*/

function start() {
    if (cm.getQuestStatus(100004) == net.sf.odinms.client.MapleQuestStatus.Status.COMPLETED) {
        cm.startQuest(100005);
        cm.sendOk("You're a true hero! Take this and Dances with Balrog will acknowledge you.");
    } else {
        cm.sendOk("You will have to collect me #b30 #t4031013##k. Good luck.")
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(102020300, 0);
    cm.dispose();
}