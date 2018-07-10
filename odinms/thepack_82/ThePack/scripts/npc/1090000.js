/*
   by EspadaFung
   Pirate Job Advancement
*/

var status = 0;
var job;

importPackage(net.sf.odinms.client);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if ((mode == 0 && status == 2) || (mode == 0 && status == 13)) {
            cm.sendOk("Come back once you have thought about it some more.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) {
                if (cm.getLevel() >= 10 && cm.getChar().getDex() >= 25)
                    cm.sendNext("So you decided to become a #rPirate#k?");
                else {
                    cm.sendOk("Train a bit more and I can show you the way of the #rPirate#k.")
                    cm.dispose();
                }
            } else {
                if (cm.getLevel() >= 30 && cm.getJob().equals(net.sf.odinms.client.MapleJob.PIRATE)) {
                    status = 10;
                    cm.sendNext("The progress you have made is astonishing.");
                } else if (cm.getLevel() >= 70 && (cm.getJob().equals(net.sf.odinms.client.MapleJob.BRAWLER) || cm.getJob().equals(net.sf.odinms.client.MapleJob.GUNSLINGER)))
                    cm.sendOk("Please go visit #bArec#k. He resides in #bEl Nath#k.");
                else if (cm.getLevel() < 30 && cm.getJob().equals(net.sf.odinms.client.MapleJob.PIRATE))
                    cm.sendOk("Please come back to see me once you have trained more.");
                else if (cm.getLevel() >= 120 && (cm.getJob().equals(net.sf.odinms.client.MapleJob.GUNSLINGER) || cm.getJob().equals(net.sf.odinms.client.MapleJob.OUTLAW)))
                    cm.sendOk("Please go visit the 4th job advancement person.");
                else
                    cm.sendOk("Please let me down...");
                cm.dispose();
            }
        } else if (status == 1)
            cm.sendNextPrev("It is an important and final choice. You will not be able to turn back.");
        else if (status == 2)
            cm.sendYesNo("Do you want to become a #rPirate#k?");
        else if (status == 3) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER))
                cm.changeJob(net.sf.odinms.client.MapleJob.PIRATE);
            cm.sendOk("So be it! Now go with pride.");
            cm.dispose();
        } else if (status == 11)
            cm.sendNextPrev("You are now ready to take the next step as a #rGunslinger#k or #rBrawler#k.");
        else if (status == 12)
            cm.sendSimple("What do you want to become?#b\r\n#L0#Gunslinger#l\r\n#L1#Brawler#l#k");
        else if (status == 13) {
            var jobName;
            if (selection == 0) {
                jobName = "Gunslinger";
                job = net.sf.odinms.client.MapleJob.GUNSLINGER;
            } else {
                jobName = "Brawler";
                job = net.sf.odinms.client.MapleJob.BRAWLER;
            }
            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 14) {
            cm.changeJob(job);
            cm.sendOk("Congratulations, you are now a " + jobName+ ".");
            cm.dispose();
        }
    }
}