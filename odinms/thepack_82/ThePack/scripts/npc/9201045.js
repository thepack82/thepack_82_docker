/**
 *9201045.js - Amos in stage 5, 6 and 7
 *@author Jvlaple
 *For Jvlaple's APQ
 */
 
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.server.life);
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
var party;
var preamble;
var Grog = 9400536;

function start() {
	status = -1;
	mapId = cm.getChar().getMapId();
	if (mapId == 670010500)
		curMap = 4;
	else if (mapId == 670010600)
		curMap = 5;
	else if (mapId == 670010700)
		curMap = 6;
	playerStatus = cm.isLeader();
	preamble = null;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (curMap == 4) { // First Stage.
			if (playerStatus) { // party leader
				if (status == 0) {
					var eim = cm.getChar().getEventInstance();
					party = eim.getPlayers();
					preamble = eim.getProperty("leader5thpreamble");
					if (preamble == null) {
						cm.sendNext("Hello and welcome to the 5th stage. In this map, there are Toy trojans, Curse Eyes and Zombie Lupins. All you have to do is kill them and collect 50 #bCupid code pieces#k. Good Luck!");
						eim.setProperty("leader5thpreamble","done");
						cm.dispose();
					}
					else { // check how many they have compared to number of party members
                        			// check for stage completed
							var complete = eim.getProperty(curMap.toString() + "stageclear");
							if (complete != null) {
								cm.sendNext("Hey, why don't you continue? The portal opened!");
								cm.dispose();
							} else {
								if (cm.haveItem(4031597, 50) == false) {
									cm.sendNext("I'm sorry, but you need a 50 #bCupid code pieces#k to clear this stage.");
									cm.dispose();
								}
								else {
									cm.sendNext("Congratulations on clearing the fifth stage! I will now open the door.");
									clear(4,eim,cm);
									cm.givePartyExp(18000, party);
									cm.gainItem(4031597, -50);
									cm.dispose();
								}
						}
					}
				}
			}
			else { // non leader
				var eim = cm.getChar().getEventInstance();
				pstring = "member5thpreamble" + cm.getChar().getId().toString();
				preamble = eim.getProperty(pstring);
				if (status == 0 && preamble == null) {
					var qstring = "member5th" + cm.getChar().getId().toString();
					var question = eim.getProperty(qstring);
					if (question == null) {
						qstring = "FUCK";
					}
					cm.sendNext("Hello and welcome to the first stage. I will spawn 3 portals here, and you will decide which one is the right one, girls will go into the left portal, while guys go into the right portal. Kill the monsters there and then get the leader to talk to my friend, Glimmer Man.");
				}
				else if (status == 0) {// otherwise
					// check for stage completed
					var complete = eim.getProperty(curMap.toString() + "stageclear");
					if (complete != null) {
						cm.warp(670010300, 0);
						cm.dispose();
					} else {
					cm.sendOk("Please talk to me after you've completed the stage.");
					cm.dispose();
					}
				}
				else if (status == 1) {
					if (preamble == null) {
						cm.sendOk("Ok, best of luck to you!");
						cm.dispose();
					}
					else { // shouldn't happen, if it does then just dispose
						cm.dispose();
					}
						
				}
				else if (status == 2) { // preamble completed
					eim.setProperty(pstring,"done");
					cm.dispose();
				}
				else { // shouldn't happen, but still...
					eim.setProperty(pstring,"done"); // just to be sure
					cm.dispose();
				}
			}
		} else if (curMap == 5) {
			var eim = cm.getPlayer().getEventInstance();
			var party = eim.getPlayers();
			var mf = eim.getMapFactory();
			var map = mf.getMap(670010700);
			if (playerStatus) {
				if (status == 0) {
					cm.sendNext("Wow, you made it!");
				} else if (status == 1) {
					for (var i = 0; i < party.size(); i++) {
						party.get(i).changeMap(map, map.getPortal(0));
					}
					cm.dispose();
				}
			} else {
				cm.sendNext("Please ask your party leader to talk to me.");
				cm.dispose();
			}
		} else if (curMap == 6) {
			var eim = cm.getPlayer().getEventInstance();
			var party = eim.getPlayers();
			var mf = eim.getMapFactory();
			var map = mf.getMap(670010700);
			if (playerStatus) {
				if (eim.getProperty("rogSpawned") == null) {
					if (status == 0) {
						cm.sendNext("Wow, this is it... Your final mission. Kill Giest Balrog and bring back the #bGiest Fang#k he possesses. Good Luck!");
					}else if (status == 1) {
						cm.spawnMonster(Grog, 953, 570);
						for (var i = 0; i < party.size(); i++) {
							party.get(i).changeMap(map, map.getPortal(2));
						}
						eim.setProperty("rogSpawned", "yes");
						cm.dispose();
					}
				} else {
					if (status == 0) {
						if (cm.haveItem(4031594, 1)) {
							cm.sendNext("You finished the challenge! Well, good luck in the vault...");
						} else {
							cm.sendNext("Please bring back the #bGiest Fang#k as evidence.");
							cm.dispose();
						}
					} else if (status == 1) {
						var eim = cm.getPlayer().getEventInstance();
						cm.gainItem(4031594, -1);
						eim.finishPQ();
						cm.dispose();
					}
				}
			}
		}
	}
}

function clear(stage, eim, cm) {
	eim.setProperty(stage.toString() + "stageclear","true");
	var packetef = MaplePacketCreator.showEffect("quest/party/clear");
	var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
	var packetglow = MaplePacketCreator.environmentChange("gate",2);
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
	map.broadcastMessage(packetglow);
	var mf = eim.getMapFactory();
	map = mf.getMap(670010200 + stage * 100);
	//eim.addMapInstance(670010200 + stage,map);
	//cm.givePartyExp(2, party);
	//cm.mapMessage("Clear!");
	//var nextStage = eim.getMapInstance(670010200 + stage*100);
	//var portal = nextStage.getPortal("in00");
	//if (portal != null) {
		//portal.setScriptName("hontale_BtoB1");
		//map.broadcastMessage(packetglow);
	//}
	//else { // into final stage
		//cm.sendNext("Initiating final stage monsters...");
		// spawn monsters
		//var map = eim.getMapInstance(103000804);
		//map.spawnMonsters(monsterIds);
	//}
}