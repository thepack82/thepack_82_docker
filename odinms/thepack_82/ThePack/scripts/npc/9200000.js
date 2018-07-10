/* [NPC]
      Universal Job Advancer and Rebirth
      Made by Moogra
      Took 2 lines from airflow0's code
 */

importPackage(net.sf.odinms.client);

var status = 0;
var jobName;
var job;

function start() {
    cm.sendNext("Hello, I'm in charge of Job Advancing.");
}

function action(mode, type, selection) {
    var jobId=cm.getPlayer().getJob().getId();
    if (mode == -1) {
        cm.sendOk("Well okay then. Come back if you change your mind.\r\n\r\nGood luck on your training.");
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (cm.getLevel() < 200 && cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) {
                if (cm.getLevel() < 8) {
                    cm.sendNext("Sorry, but you have to be at least level 8 to use my services.");
                    cm.dispose();
                } else if (cm.getLevel() < 10) {
                    cm.sendYesNo("Congratulations of reaching such a high level. Would you like to make the #rFirst Job Advancement#k as a #rMagician#k?");
                    status = 150;
                } else {
                    cm.sendYesNo("Congratulations on reaching such a high level. Would you like to make the #rFirst Job Advancement#k?");
                    status = 153;
                }
            } else if (cm.getLevel() < 30) {
                cm.sendOk("Sorry, but you have to be at least level 30 to make the #rSecond Job Advancement#k.");
                cm.dispose();
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.THIEF))
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L0#Assassin#l\r\n#L1#Bandit#l#k");
            else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WARRIOR))
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L2#Fighter#l\r\n#L3#Page#l\r\n#L4#Spearman#l#k");
            else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.MAGICIAN))
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L5#Ice Lightning Wizard#l\r\n#L6#Fire Poison Wizard#l\r\n#L7#Cleric#l#k");
            else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BOWMAN))
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L8#Hunter#l\r\n#L9#Crossbowman#l#k");
            else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.PIRATE))
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L10#Brawler#l\r\n#L11#Gunslinger#l#k");
            else if (cm.getLevel() < 70) {
                cm.sendOk("Sorry, but you have to be at least level 70 to make the #rThird Job Advancement#k.");
                cm.dispose();
            }
            else if (jobId % 100 != 0 && (cm.getLevel() >= 70 && jobId % 10 == 0 || cm.getLevel() >= 120 && jobId % 10 == 1)){
                cm.getPlayer().changeJob(MapleJob.getById(jobId + 1));
                cm.sendOk("Congratulations, you have been job advanced!");
                cm.dispose();
            }
            else if (cm.getLevel() < 200) {
                cm.sendNext("Sorry, but you have already attained the highest level of your job's mastery. \r\n\r\nHowever, you can #rrebirth#k when you are level 200.");
                status = 98;
            } else if (cm.getLevel() >= 200) {
                cm.sendYesNo("Ahh... It is good to see you again. Your skills have finally reached the maximum of its potential. So, with all my heart, I congratulate you, great hero. \r\n\r\nYou have been through a long and challenging road, and in so doing, have become immensely strong. However, I can increase your power even further, and surpass your limits! If you accept, you will become a level 1 #bBeginner#k again, but all your abilities, skills, items and mesos, will remain as they are. However, you will only be able to keep the skills that you have placed in your #bkey setting#k. \r\n\r\nSo, tell me, do you wish to be #rreborn#k?");
                status = 160;
            } else
                cm.dispose();
        } else if (status == 2) {
            switch(selection) {
                case 0: jobName = "Assassin";             job = net.sf.odinms.client.MapleJob.ASSASSIN;    break;
                case 1: jobName = "Bandit";               job = net.sf.odinms.client.MapleJob.BANDIT;      break;
                case 2: jobName = "Fighter";              job = net.sf.odinms.client.MapleJob.FIGHTER;     break;
                case 3: jobName = "Page";                 job = net.sf.odinms.client.MapleJob.PAGE;        break;
                case 4: jobName = "Spearman";             job = net.sf.odinms.client.MapleJob.SPEARMAN;    break;
                case 5: jobName = "Ice/Lightning Wizard"; job = net.sf.odinms.client.MapleJob.IL_WIZARD;   break;
                case 6: jobName = "Fire/Poison Wizard";   job = net.sf.odinms.client.MapleJob.FP_WIZARD;   break;
                case 7: jobName = "Cleric";               job = net.sf.odinms.client.MapleJob.CLERIC;      break;
                case 8: jobName = "Hunter";               job = net.sf.odinms.client.MapleJob.HUNTER;      break;
                case 9: jobName = "Crossbowman";          job = net.sf.odinms.client.MapleJob.CROSSBOWMAN; break;
                case 10:jobName = "Brawler";              job = net.sf.odinms.client.MapleJob.BRAWLER;     break;
                case 11:jobName = "Gunslinger";           job = net.sf.odinms.client.MapleJob.GUNSLINGER;  break;
            }
            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 3) {
            cm.changeJob(job);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future.");
            cm.dispose();
        }
        else if (status == 151) {
            if (cm.c.getPlayer().getInt() >= 20) {
                cm.changeJob(net.sf.odinms.client.MapleJob.MAGICIAN);
                cm.sendOk("There you go. Hope you enjoy it. See you around in the future.");
                cm.dispose();
            } else {
                cm.sendOk("You did not meet the minimum requirement of #r20 INT#k.");
                cm.dispose();
            }
        } else if (status == 154) {
            cm.sendSimple("Which would you like to be? #b\r\n#L0#Warrior#l\r\n#L1#Magician#l\r\n#L2#Bowman#l\r\n#L3#Thief#l\r\n#L4#Pirate#l#k");
        } else if (status == 155) {
            if (selection == 0) jobName = "Warrior";
            else if (selection == 1) jobName = "Magician";
            else if (selection == 2) jobName = "Bowman";
            else if (selection == 3) jobName = "Thief";
            else if (selection == 4) jobName = "Pirate";
            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 156) {
            if (selection == 0 && cm.c.getPlayer().getStr() < 35)
                cm.sendOk("You did not meet the minimum requirement of #r35 STR#k.");
            else if (selection == 1 && cm.c.getPlayer().getInt() < 20)
                cm.sendOk("You did not meet the minimum requirement of #r20 INT#k.");
            else if (selection == 2 && cm.c.getPlayer().getDex() < 25)
                cm.sendOk("You did not meet the minimum requirement of #r25 DEX#k.");
            else if (selection == 3 && cm.c.getPlayer().getDex() < 25)
                cm.sendOk("You did not meet the minimum requirement of #r25 DEX#k.");
            else if (selection == 4 && cm.c.getPlayer().getDex() < 20)
                cm.sendOk("You did not meet the minimum requirement of #r20 DEX#k.");
            else {
                cm.getPlayer().setJob(MapleJob.getById(100*(selection+1)));
                cm.sendOk("There you go. Hope you enjoy it. See you around in the future.");
            }
            cm.dispose();
        } else if (status == 161) {
            cm.doReborn();
            cm.sendOk("You have been reborn! Good luck on your next journey!");
            cm.dispose();
        } else
            cm.dispose();
    }
}