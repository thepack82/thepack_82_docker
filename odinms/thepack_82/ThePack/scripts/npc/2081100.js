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
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CRUSADER)) {
                status = 126;
                cm.sendYesNo("Conratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WHITEKNIGHT)) {
                status = 129;
                cm.sendYesNo("Conratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.DRAGONKNIGHT)) {
                status = 132;
                cm.sendYesNo("Conratulations on reaching such a high level. Do you want to Job Advance now?");
            }		
        } else if (status == 127) {
            cm.changeJob(MapleJob.HERO);
            cm.teachSkill(1121002,0,30);
            cm.teachSkill(1120003,0,30);
            cm.teachSkill(1120005,0,30);
            cm.teachSkill(1121006,0,30);
            cm.teachSkill(1121010,0,30);
            cm.sendOk("There you go. Hope you enjoy it. I've given you a full set of skills as well, aren't you lucky!");
        } else if (status == 130) {
            cm.changeJob(MapleJob.PALADIN);
            cm.teachSkill(1221002,0,30);
            cm.teachSkill(1221003,0,30);
            cm.teachSkill(1221004,0,30);
            cm.teachSkill(1220006,0,30);
            cm.teachSkill(1221007,0,30);
            cm.teachSkill(1221009,0,30);
            cm.teachSkill(1220010,0,10);
            cm.teachSkill(1221011,0,30);
            cm.sendOk("There you go. Hope you enjoy it. I've given you a full set of skills as well, aren't you lucky!");
        } else if (status == 133) {
            cm.changeJob(MapleJob.DARKKNIGHT);
            cm.teachSkill(1321002,0,30);
            cm.teachSkill(1321003,0,30);
            cm.teachSkill(1320006,0,30);
            cm.teachSkill(1321007,0,10);
            cm.teachSkill(1320008,0,25);
            cm.sendOk("There you go. Hope you enjoy it. I've given you a full set of skills as well, aren't you lucky!");
        }
    }
}