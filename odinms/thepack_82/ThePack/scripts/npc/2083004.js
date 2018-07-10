/*2083004.js - Mark of the Squad
 * @author Jvlaple
 * For Jvlaple's HTPQ
 */
importPackage(java.lang);
importPackage(net.sf.odinms.server);
 
var status = 0;
var toBan = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getSquadState(MapleSquadType.HORNTAIL) == 0) {
                cm.sendSimple("Would you like to assemble a team to take on the mighty #rHorntail#k?\r\n#b#L1#Lets get this going!#l\r\n\#L2#No, I think I'll wait a bit...#l");
            } else if (cm.getSquadState(MapleSquadType.HORNTAIL) == 1) {
                if (cm.checkSquadLeader(MapleSquadType.HORNTAIL)) {
                    cm.sendSimple("What would you like to do?#b\r\n\r\n#L1#View current Squad members#l\r\n#L2#Close registrations#l\r\n#L3#Start the fight!#l");
                    status = 19;
                } else if (cm.isSquadMember(MapleSquadType.HORNTAIL)) {
                    var noOfChars = cm.numSquadMembers(MapleSquadType.HORNTAIL);
                    var toSend = "The following members make up your squad:\r\n#b";
                    for (var i = 1; i <= noOfChars; i++) {
                        toSend += "\r\n#L" + i + "#" + cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName() + "#l";
                    }
                    cm.sendSimple(toSend);
                    cm.dispose();
                } else {
                    cm.sendSimple("Would you like to join the Horntail Squad?\r\n#b#L1#Yes, I'm ready to take him on!#l\r\n\#L2#No, I think I'll wait a bit...#l");
                    status = 9;
                }
            } else {
                if (cm.checkSquadLeader(MapleSquadType.HORNTAIL)) {
                    cm.sendSimple("What would you like to do?\r\n\r\n#b#L1#View current squad members#l\r\n#L2#Open registrations#l\r\n#L3#Start the fight!#l");
                    status = 19;
                } else if (cm.isSquadMember(MapleSquadType.HORNTAIL)) {
                    var noOfChars = cm.numSquadMembers(MapleSquadType.HORNTAIL);
                    var toSend = "The following members make up your squad:\r\n#b";
                    for (var i = 1; i <= noOfChars; i++) {
                        toSend += "\r\n#L" + i + "#" + cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName() + "#l";
                    }
                    cm.sendSimple(toSend);
                    cm.dispose();
                } else {
                    cm.sendOk("Sorry but the leader of the squad has closed registrations. Try again next time.");
                    cm.dispose();
                }
            }
        } else if (status == 1) {
            if (selection == 1) {
                if (cm.createMapleSquad(MapleSquadType.HORNTAIL) != null) {
                    cm.sendOk("The #rHorntail Squad#k has been created, tell your members to sign up now.\r\n\r\nTalk to me again to view the current team, to close registrations, or start the fight!");
                    cm.dispose();
                } else {
                    cm.sendOk("There was an error, someone may have created a Horntail Squad before you, please inform a GameMaster if this is not the case.");
                    cm.dispose();
                }
            } else if (selection == 2) {
                cm.sendOk("Sure, not everyone's up to challenging the might of Horntail.");
                cm.dispose();
            }
        } else if (status == 10) {
            if (selection == 1) {
                if (cm.numSquadMembers(MapleSquadType.HORNTAIL) > 29) {
                    cm.sendOk("Sorry, the Horntail Squad is full.");
                    cm.dispose();
                } else {
                    if (cm.canAddSquadMember(MapleSquadType.HORNTAIL)) {
                        cm.addSquadMember(MapleSquadType.HORNTAIL);
                        cm.sendOk("You have signed up, please wait for further instruction from your squad leader.");
                        cm.dispose();
                    } else {
                        cm.sendOk("Sorry, but the leader has requested you not to be allowed to join.");
                        cm.dispose();
                    }
                }
            } else if (selection == 2) {
                cm.sendOk("Sure, not everyone's up to challenging the might of #rHorntail.#k");
                cm.dispose();
            }
        } else if (status == 20) {
            if (selection == 1) {
                var noOfChars = cm.numSquadMembers(MapleSquadType.HORNTAIL);
                var toSend = "The following members make up your squad (Click on them to Expell them):\r\n#b";
                for (var i = 1; i <= noOfChars; i++) {
                    if (i == 1)
                        toSend += "\r\n" + cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName();
                    else
                        toSend += "\r\n#L" + i + "#" + cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName() + "#l";
                //toSend += cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName();
                }
                //System.out.println(toSend);
                cm.sendSimple(toSend);
            } else if (selection == 2) {
                if (cm.getSquadState(MapleSquadType.HORNTAIL) == 1) {
                    cm.setSquadState(MapleSquadType.HORNTAIL, 2);
                    cm.sendOk("Registrations have been closed, please talk to me again to start the fight or re-open them.");
                } else {
                    cm.setSquadState(MapleSquadType.HORNTAIL, 1);
                    cm.sendOk("Registrations have been opened, please talk to me again to start the fight or close them.");
                }
                cm.dispose();
            } else if (selection == 3) {
                cm.setSquadState(MapleSquadType.HORNTAIL, 2);
                cm.sendOk("I will now take you and your squad to #rCave of Trial I#k.");
                //cm.warpSquadMembers(MapleSquadType.HORNTAIL, 280030000);
                status = 29;
            //cm.dispose();
            }
        } else if (status == 21) {
            if (selection > 0) {
                cm.removeSquadMember(MapleSquadType.HORNTAIL, selection - 1, true);
                cm.sendOk("The selected member has been banned.");
                cm.dispose();
            } else {
                if (cm.getSquadState(MapleSquadType.HORNTAIL) == 1) {
                    cm.sendSimple("What would you like to do?\r\n\r\n#L1#View current squad members#l\r\n#L2#Close registrations#l\r\n#L3#Start the fight!#l");
                } else {
                    cm.sendSimple("What would you like to do?\r\n\r\n#L1#View current squad members#l\r\n#L2#Open registrations#l\r\n#L3#Start the fight!#l");
                }
                status = 19;
            }
        } else if (status == 30) {
            //cm.warpSquadMembersClock(MapleSquadType.HORNTAIL, 280030000, 10800, 211042300); //Should have a clock and exit now :D
            var em = cm.getEventManager("HontaleSquad");
            if (em == null) {
                cm.sendOk("...");
                cm.dispose();
            }
            else {
                // Begin the PQ.
                em.startInstance(cm.getSquad(MapleSquadType.HORNTAIL), cm.getChar().getMap());
            }
            cm.dispose();
        }
    }
}