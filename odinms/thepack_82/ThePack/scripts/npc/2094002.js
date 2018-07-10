/*2094002.js [Guon in PPQ(Pirate PQ)]
 *@author Jvlaple
 */
 
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.server.life);
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
//var party;
var preamble;
var PQItems = new Array(4001117, 4001120, 4001121, 4001122);

function start() {
	status = -1;
	mapId = cm.getChar().getMapId();
	if (mapId == 925100100) //Through head of ship...
		curMap = 1;
	else if (mapId == 925100200) //Through Deck I
		curMap = 2;
	else if (mapId == 925100300)//Through Deck II
		curMap = 3;
	else if (mapId == 925100400)//Eliminate Pirates
		curMap = 4;
	else if (mapId == 925100500)//The Captains Dignity
		curMap = 5;
	else if (mapId == 925100700) //Exit
		curMap = 999;
	if (cm.getParty() != null) //Check for Party
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
		if (curMap == 1) { //First Stage
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				premable = eim.getProperty("leader1stpremable");
				if (premable == null) {
					cm.sendNext("Here is the back of the Pirate Ship. You will work your way to the front and save Wu-Yang for me. See the Pirates here? Kill them and give me 20 of each Marks. There are three kinds of Marks - Rookie, Rising, and Veteran. These pirates aren't the same as the ones off the ship, so please be careful ~");
					eim.setProperty("leader1stpremable", "done");
					cm.dispose();
				} else {
						var complete = eim.getProperty("1stageclear");
						if (complete != null) {
							cm.sendNext("Please continue through the portal!");
							cm.dispose();
						}else if (cm.haveItem(4001120, 20) && cm.haveItem(4001121, 20) && cm.haveItem(4001122, 20)) {
							cm.sendNext("Thank you for bringing me 20 of each Mark. You may proceed now!");
							cm.gainItem(4001120, -20);
							cm.gainItem(4001121, -20);
							cm.gainItem(4001122, -20);
							//cm.gainItem(4001044, 1);
							cm.givePartyExp(5000, party);
							clear(1, eim, cm);
							cm.dispose();
						} else {
							cm.sendNext("You have not got 20 of each #bMark of the Pirate.#k yet.");
							cm.dispose();
					}
				}
				cm.dispose();
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("Here is the back of the Pirate Ship. You will work your way to the front and save Wu-Yang for me. See the Pirates here? Kill them and give me 20 of each Marks. There are three kinds of Marks - Rookie, Rising, and Veteran. These pirates aren't the same as the ones off the ship, so please be careful ~");
					cm.dispose();
				} else {
					cm.sendNext("Please proceed through the portal!");
					cm.dispose();
				}
			}
		} else if (curMap == 2 || curMap == 3) {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				cm.sendOk("This is the Storage of the Pirate Ship. Break the Boxes, and kill all the monsters, then proceed through the portal.");
				cm.dispose();
		} else if (curMap == 4) { //2nd to last stage! w00t!!!
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				var thisMap = mf.getMap(925100400);
				premable = eim.getProperty("leader4thpremable");
				if (premable == null) {
					cm.sendNext("Over here, is the place where the high - ranked pirates are trying to escape. They have the keys with them. Kill them, and occasionally they may drop a Key. Drop the key on to the door you wish to seal, then they can't run away. Once you seal all the doors, talk to me and you can proceed. Again, these Pirates aren't the same as the ones off the ship, so please be careful ~");
					eim.setProperty("leader4thpremable", "done");
					cm.dispose();
				} else {
						var thisMap = mf.getMap(925100400);
						var complete = eim.getProperty("4stageclear");
						if (complete != null) {
							cm.sendNext("Please continue through the portal!");
							cm.dispose();
						}else if (thisMap.getReactorByName("sMob1").getState() != 0 &&
							thisMap.getReactorByName("sMob2").getState() != 0 &&
							thisMap.getReactorByName("sMob3").getState() != 0 &&
							thisMap.getReactorByName("sMob4").getState() != 0) {
							//cm.sendNext("Thank you for stopping the Pirates from running away. Please proceed, Lord Pirate must be taken down as soon as possible, and Wu Yang must be saved.");
							cm.sendNext("Thank you for stopping the Pirates from running away. Please proceed, Lord Pirate must be taken down as soon as possible, and Wu Yang must be saved.");
							// cm.gainItem(4001120, -20);
							// cm.gainItem(4001121, -20);
							// cm.gainItem(4001122, -20);
							//cm.gainItem(4001044, 1);
							cm.givePartyExp(15000, party);
							clear(4, eim, cm);
							cm.dispose();
						} else {
							cm.sendNext("You have not sealed all the doors yet.");
							cm.dispose();
					}
				}
				cm.dispose();
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("Over here, is the place where the high - ranked pirates are trying to escape. They have the keys with them. Kill them, and occasionally they may drop a Key. Drop the key on to the door you wish to seal, then they can't run away. Once you seal all the doors, talk to me and you can proceed. Again, these Pirates aren't the same as the ones off the ship, so please be careful ~");
					cm.dispose();
				} else {
					cm.sendNext("Please proceed through the portal!");
					cm.dispose();
				}
			}
		} else if (curMap == 5) { //Boss Stage!! W00t!!!
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				var toSpawn;
				premable = eim.getProperty("leader5thpremable");
				if (premable == null) {
					cm.sendNext("Here is Lord Pirate's Room. He will appear any minute... Be careful ~");
					var numOfBoxesOpened = parseInt(eim.getProperty("openedBoxes"));
					if (numOfBoxesOpened < 3) {
						toSpawn = 9300119;
					} else if (numOfBoxesOpened > 3 && numOfBoxesOpened < 7) {
						toSpawn = 9300105;
					} else {
						toSpawn = 9300106;
					}
					cm.spawnMonster(toSpawn, 766, 238);
					cm.mapMessage(6, "Here comes Lord Pirate...");
					eim.setProperty("leader5thpremable", "done");
					cm.dispose();
				} else {
					cm.sendNext("Kill Lord Pirate, and Wu Yang will be released.");
					cm.dispose();
					} 
				}else {
					var eim = cm.getChar().getEventInstance();
					var mf = eim.getMapFactory();
					var party = cm.getChar().getEventInstance().getPlayers();
					if (eim.getProperty(curMap.toString() + "stageclear") == null) {
						cm.sendNext("This is Lord Pirate's Headquarters.");
						cm.dispose();
					} else {
						cm.sendNext("Please proceed through the portal!");
						cm.dispose();
				}
			}
		}else if (curMap == 999) { //Teh Exit
			if (cm.getPlayer().isGM() == false) {
				for (var i = 0; i < PQItems.length; i++) {
					cm.removeAll(PQItems[i]);
				}
			}
			cm.warp(251010404, 0);
			cm.dispose();
		}
	}
	if (curMap == 2 || curMap == 3) {
		cm.dispose();
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
	var mf = eim.getMapFactory();
	map = mf.getMap(920010000 + stage * 100);
	//eim.addMapInstance(670010200 + stage,map);
	//cm.givePartyExp(2, party);
	cm.mapMessage("Clear!");
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}