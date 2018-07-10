/*2101014.js - Lobby and Entrance
 * @author Jvlaple
 * For Jvlaple's AriantPQ
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
        if (cm.getPlayer().getMapId() == 980010000) {
            if (status == 0) {
                var toSnd = "Would you like to participate in Ariant Coliseum Challenge? Choose an arena!\r\n#b";
                if (cm.getSquadState(MapleSquadType.ARIANT1) != 2 && cm.getSquadState(MapleSquadType.ARIANT1) != 1) {
                    toSnd += "#L0#Start Ariant Coliseum 1!#l\r\n";
                } else if (cm.getSquadState(MapleSquadType.ARIANT1) == 1) {
                    toSnd += "#L0#Join Ariant Coliseum 1!  Leader (" + cm.getSquadMember(MapleSquadType.ARIANT1, 0).getName() + ")" + " Current Members: " + cm.numSquadMembers(MapleSquadType.ARIANT1) + "\r\n";
                }
                if (cm.getSquadState(MapleSquadType.ARIANT2) != 2 && cm.getSquadState(MapleSquadType.ARIANT2) != 1) {
                    toSnd += "#L1#Start Ariant Coliseum 2!#l\r\n";
                } else if (cm.getSquadState(MapleSquadType.ARIANT2) == 1) {
                    toSnd += "#L1#Join Ariant Coliseum 2!  Leader (" + cm.getSquadMember(MapleSquadType.ARIANT2, 0).getName() + ")" + " Current Members: " + cm.numSquadMembers(MapleSquadType.ARIANT2) + "\r\n";
                }
                if (cm.getSquadState(MapleSquadType.ARIANT3) != 2 && cm.getSquadState(MapleSquadType.ARIANT3) != 1) {
                    toSnd += "#L2#Start Ariant Coliseum 3!#l\r\n";
                } else if (cm.getSquadState(MapleSquadType.ARIANT3) == 1) {
                    toSnd += "#L2#Join Ariant Coliseum 3!  Leader (" + cm.getSquadMember(MapleSquadType.ARIANT3, 0).getName() + ")" + " Current Members: " + cm.numSquadMembers(MapleSquadType.ARIANT3) + "\r\n";
                }
                if (toSnd.equals("Would you like to participate in Ariant Coliseum Challenge? Choose an arena!\r\n#b")) {
                    cm.sendOk("All arenas are taken right now. I suggest you come back later or change channels.");
                    cm.dispose();
                } else {
                    cm.sendSimple(toSnd);
                }
            } else if (status == 1) {
                switch (selection) {
                    case 0 : choice = MapleSquadType.ARIANT1;
                        map = 980010100;
                        break;
                    case 1 : choice = MapleSquadType.ARIANT2;
                        map = 980010200;
                        break;
                    case 2 : choice = MapleSquadType.ARIANT3;
                        map = 980010300;
                        break;
                    default : choice = MapleSquadType.UNDEFINED;
                        map = 0;
                        return;
                        break;
                }
                if (cm.getSquadState(choice) == 0) {
                    if (cm.createMapleSquad(choice) != null) {
                        cm.sendOk("Your Arena has been created! Wait for people to join now.");
                        cm.warp(map, 0);
                        cm.dispose();
                    } else {
                        cm.sendOk("There was an error. Please report this to a GameMaster as soon as possible.");
                        cm.dispose();
                    }
                } else if (cm.getSquadState(choice) == 1) {
                    if (cm.numSquadMembers(choice) > 5) {
                        cm.sendOk("Sorry, the Lobby is full now.");
                        cm.dispose();
                    } else {
                        if (cm.canAddSquadMember(choice)) {
                            cm.addSquadMember(choice);
                            cm.sendOk("You have signed up!");
                            cm.warp(map, 0);
                            cm.dispose();
                        } else {
                            cm.sendOk("Sorry, but the leader has requested you not to be allowed to join.");
                            cm.dispose();
                        }
                    }
                } else {
                    cm.sendOk("Something went wrong.");
                    cm.dispose();
                }
            }
        } /*else if (cm.getPlayer().getMapId() == 980010100 || cm.getPlayer().getMapId() == 980010200 || cm.getPlayer().getMapId() == 980010100) {
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
					cm.sendSimple("What would you like to do?#b\r\n\r\n#L1#View current registered in arena!#l\r\n#L2#Start the fight!#l");
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
						toSend += "\r\n#L" + i + "#" + cm.getSquadMember(MapleSquadType.HORNTAIL, i - 1).getName() + "#l";
					}
					cm.sendSimple(toSend);
					cm.dispose();
				} else if (selection == 2) {
					//Start the fight if there is more than 6 people
					if (cm.numSquadMembers(arena) < 2) {
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
							em.startInstance(cm.getSquad(arena), cm.getChar().getMap());
						}
						cm.dispose();
					}
				}
			}
		}*/
    }
}