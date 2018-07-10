/* [NPC]
	Job Advancer
	Made by Tryst (wasdwasd) of Odinms Forums.
	Please don't release this anywhere else.
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
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getLevel() < 120) {
                cm.sendNext("Sorry, but you have to be at least level 120 to make 4th Job Advancement.");
                status = 98;
            } else if (cm.getLevel() >=120) {
                status = 101;
                cm.sendNext("Hello, So you want 4th Job Advancement eh?.");
            }
        } else if (status == 102) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.RANGER)) {
                status = 111;
                cm.sendYesNo("Conratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.SNIPER)) {
                status = 114;
                cm.sendYesNo("Conratulations on reaching such a high level. Do you want to Job Advance now?");
            }		
        } else if (status == 112) {
            cm.changeJob(MapleJob.BOWMASTER);
            cm.teachSkill(3121003,0,30);
            cm.teachSkill(3121004,0,30);
            cm.teachSkill(3121006,0,30);
            cm.teachSkill(3121008,0,30);
            cm.sendOk("There you go. Hope you enjoy it. I've given you a full set of skills as well, aren't you lucky!");
        } else if (status == 115) {
            cm.changeJob(MapleJob.MARKSMAN);
            cm.teachSkill(3221001,0,30);
            cm.teachSkill(3221003,0,30);
            cm.teachSkill(3221005,0,30);
            cm.teachSkill(3221007,0,30);
            cm.sendOk("There you go. Hope you enjoy it. I've given you a full set of skills as well, aren't you lucky!");
        }
    }
}