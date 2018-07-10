/*2101017.js
 *Cesar
 *@author Jvlaple
 */
 
importPackage(java.lang);
importPackage(net.sf.odinms.server);
 
var status = 0;
var toBan = -1;
var choice;
var arena;
var arenaName;
var type;
var map;

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
	
        if (cm.getPlayer().getMapId() == 980010100 || cm.getPlayer().getMapId() == 980010200 || cm.getPlayer().getMapId() == 980010300) {
            if (status == 0) {
                switch (cm.getPlayer().getMapId()) {
                    case 980010100:
                        arena = MapleSquadType.ARIANT1;
                        break;
                    case 980010200:
                        arena = MapleSquadType.ARIANT2;
                        break;
                    case 980010300:
                        arena = MapleSquadType.ARIANT3;
                        break;
                    default :
                        return;
                }
                if (cm.checkSquadLeader(arena)) {
                    cm.sendSimple("What would you like to do?#b\r\n\r\n#L1#View current registered in arena!#l\r\n#L2#Start the fight!#l\r\n#L3#Quit this arena!#l");
                    status = 19;
                } else if (cm.isSquadMember(arena)) {
                    var noOfChars = cm.numSquadMembers(arena);
                    var toSend = "You currently have these people in your arena:\r\n#b";
                    for (var i = 1; i <= noOfChars; i++) {
                        toSend += "\r\n#L" + i + "#" + cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName() + "#l";
                    }
                    cm.sendSimple(toSend);
                    cm.dispose();
                } else {
                    cm.sendOk("what happened o.O");
                    cm.dispose();
                }
            } else if (status == 20) {
                switch (cm.getPlayer().getMapId()) {
                    case 980010100:
                        arena = MapleSquadType.ARIANT1;
                        arenaName = "AriantPQ1";
                        break;
                    case 980010200:
                        arena = MapleSquadType.ARIANT2;
                        arenaName = "AriantPQ2";
                        break;
                    case 980010300:
                        arena = MapleSquadType.ARIANT3;
                        arenaName = "AriantPQ3";
                        break;
                    default :
                        return;
                }
                if (selection == 1) {
                    var noOfChars = cm.numSquadMembers(arena);
                    var toSend = "You currently have these people in your arena:\r\n#b";
                    for (var i = 1; i <= noOfChars; i++) {
                        toSend += "\r\n#L" + i + "#" + cm.getSquadMember(arena, i - 1).getName() + "#l";
                    }
                    cm.sendSimple(toSend);
                    cm.dispose();
                } else if (selection == 2) {
                    //Start the fight if there is more than 6 people
                    if (cm.numSquadMembers(arena) < 2 && !cm.getChar().isGM()) {
                        cm.sendOk("I can only let you fight when you have two or more people.");
                        cm.dispose();
                    } else {
                        var em = cm.getEventManager(arenaName);
                        if (em == null) {
                            cm.sendOk("...");
                            cm.dispose();
                        }
                        else {
                            // Begin the PQ.
                            cm.setSquadState(arena, 2);
                            em.startInstance(cm.getSquad(arena), cm.getChar().getMap());
                        }
                        cm.dispose();
                    }
                } else if (selection == 3) {
                    cm.mapMessage("Your Arena leader has quit.");
                    cm.warpSquadMembers(arena, 980010000)
                    var squad = cm.getPlayer().getClient().getChannelServer().getMapleSquad(arena);
                    cm.getPlayer().getClient().getChannelServer().removeMapleSquad(squad, arena);
                    //cm.warpAllInMap(980010000, 0);
                    cm.dispose();
                }
            }
        } else if (cm.getPlayer().getMapId() == 980010101 || cm.getPlayer().getMapId() == 980010201 || cm.getPlayer().getMapId() == 980010301) {
            var eim = cm.getChar().getEventInstance();
            if (status == 0) {
                var gotTheBombs = eim.getProperty("gotBomb" + cm.getChar().getId());
                if (gotTheBombs != null) {
                    cm.sendOk("I have already given you bombs, please kill the Scorpions for more!");
                    cm.dispose();
                } else {
                    cm.sendOk("I have given you 5 #bBombs#k and 50 #bElement Rocks#k. Use the Element Rocks to capture the scorpions for #rSpirit Jewels#k!");
                    eim.setProperty("gotBomb" + cm.getChar().getId(), "got");
                    cm.gainItem(2270002, 50);
                    cm.gainItem(2100067, 5);
                    cm.dispose();
                }
            }
        }
    }
}