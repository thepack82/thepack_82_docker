/* Arec
	Thief 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

importPackage(net.sf.odinms.client);

var status = 0;
var job;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 1) {
			cm.sendOk("Make up your mind and visit me again.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (!(cm.getJob().equals(MapleJob.ASSASSIN) ||
				cm.getJob().equals(MapleJob.BANDIT))) {
				cm.sendOk("May #rOdin#k be with you!");
				cm.dispose();
				return;
			}
			cm.completeQuest(100100);
			cm.completeQuest(100102);
			if (cm.getQuestStatus(100102).equals(MapleQuestStatus.Status.COMPLETED)) {
				cm.sendNext("#rBy Odin's ring!#k Indeed, you have proven to be worthy of the strength I will now bestow upon you.");
			} else if (cm.getQuestStatus(100102).equals(MapleQuestStatus.Status.STARTED)) {
				cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
				cm.dispose();
			} else if (cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.COMPLETED)) {
				cm.sendNext("#rBy Odin's raven!#k I was right, your strength is truly excellent.");
			} else if (cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.STARTED)) {
				cm.sendOk("Well, well. Now go and see #bthe Dark Lord#k. He will show you the way.");
				cm.dispose();
			} else if ((cm.getJob().equals(MapleJob.ASSASSIN) ||
				cm.getJob().equals(MapleJob.BANDIT)) &&
				cm.getLevel() >= 70 && 
				cm.getChar().getRemainingSp() <= (cm.getLevel() - 70) * 3) {
				cm.sendNext("#rBy Odin's beard!#k You are a strong one.");
			} else {
				cm.sendOk("Your time has yet to come...");
				cm.dispose();
			}
		} else if (status == 1) {
			if (cm.getQuestStatus(100102).equals(MapleQuestStatus.Status.COMPLETED)) {
				if (cm.getJob().equals(MapleJob.ASSASSIN)) {
					cm.changeJob(MapleJob.HERMIT);
					cm.getChar().gainAp(5);
					cm.sendOk("You are now a #bHermit#k. May #rOdin#k be with you!");
					cm.dispose();
				} else if (cm.getJob().equals(MapleJob.BANDIT)) {
					cm.changeJob(MapleJob.CHIEFBANDIT);
					cm.getChar().gainAp(5);
					cm.sendOk("You are now a #bChief Bandit#k. May #rOdin#k be with you!");
					cm.dispose();
				}
			} else if (cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.COMPLETED)) {
				cm.sendAcceptDecline("Is your mind ready to undertake the final test?");
			} else {
				cm.sendAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
			}
		} else if (status == 2) {
			if (cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.COMPLETED)) {
				cm.startQuest(100102);
				cm.sendOk("Go and find me the #rNecklace of Wisdom#k which is hidden on the Holy Ground at the Snowfield.");
				cm.dispose();
			} else {
				cm.startQuest(100100);
				cm.sendOk("Well, well. Now go and see #bthe Dark Lord#k. He will show you the way.");
				cm.dispose();
			}
		}
	}
}	
