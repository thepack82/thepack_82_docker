var status = 0;

importPackage(net.sf.odinms.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status < 1 && mode == 0) {
			cm.sendOk("Ok, hope you learned something...");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (cm.getPlayer().getMapId() == 240050000) {
			if (status == 0) {
				cm.sendNext("I assume you already know how to do the Morph stage to pass the guard.\r\nYou must gather up 6 members and form a party, when your'e done doing that, click the #bEncrypted Slate#k up there.");
			}else if (status == 1) {
				cm.sendNextPrev("#eFirst Stage:\r\n#n\r\nKill the Cornians in this map to obtain 6 #rRed Keys#k.\r\nWhen you have 6 Red Keys, give one to each of your party members except one member. The one member who #eDoesn\'t#n have a key must wait in the #bRoom of Maze#k.");
			}else if (status == 2) {
				cm.sendNextPrev("#eSecond Stage to Fourth Stage:\r\n#n\r\nOnce you enter the Maze rooms, kill the monsters to obtain 6 Keys, then drop them at the shining spot by the portal. The key will then be teleported to the first map. The person who was waiting in the first map will now clear the stage. Repeat for stage two to four.");
			}else if (status == 3) {
				cm.sendNextPrev("#eFifth Stage:#n\r\n\r\nKill all the #bSkelegons#k in this map, and pass all the keys to your party leader, and (s)he will proceed through the portal. The leader will talk to Horntail\'s Schedule and the whole party will be warped to #bThe Cave of Choice#k.")
			}else if (status == 4) {
				cm.sendNextPrev("#eCave of Choice:\r\n\r\n#nHit the globe in this map and you will be warped to either Cave of Light or Cave of Darkness. Kill the monsters in there and proceed.");
			}else if (status == 5) {
				cm.sendPrev("#eEntrance to Horntail\'s Cave:#n\r\n\r\nWait here for more people to finish the quest, or create a squad and prepare for the challenge...");
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 240050100) {
			if (status == 0) {
				var eim = cm.getPlayer().getEventInstance();
				var playerStatus = cm.isLeader();
				var tehProps = eim.getProperty("theLvl");
				if (tehProps == null) {
					if (playerStatus) {
						cm.sendNext("Please talk to me after you\'ve completed the Maze.");
						cm.dispose();
					} else {
						cm.dispose();
					}
				} else {
					if (playerStatus) {
						cm.sendNext("You will now be warped to #bThe Cave of Choice#k.");
					} else {
						cm.sendNext("Please ask your party leader to talk to me.");
						cm.dispose();
					}
				}
			} else if (status == 1) {
				var party = cm.getChar().getEventInstance().getPlayers();
				var eim = cm.getPlayer().getEventInstance();
				for (var outt = 0; outt<party.size(); outt++) {//To the cave of choice!
					var exitMapz = eim.getMapInstance(240050200);
					party.get(outt).changeMap(exitMapz, exitMapz.getPortal(0));
				}
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 240050300 || cm.getPlayer().getMapId() == 240050310) {
			var eim = cm.getPlayer().getEventInstance();
			var party = eim.getPlayers() 
			if (status == 0) {
				if (cm.isLeader()) {
					if (cm.haveItem(4001093, 6)) { //&& cm.countMonster == 0) {
						cm.sendNext("Good job on finishing the Quest! You will now be warped to #bEntrance to Horntail\'s Cave#k.");
					} else {
						cm.sendNext("Please kill all the monsters on the map and get 6 Blue Keys before talking to me...");
						cm.dispose();
					}
				} else {
					cm.sendNext("Only the party leader may talk to me.");
					cm.dispose();
				}
			} else if (status == 1) {
				cm.gainItem(4001093, -6);
				var party = cm.getChar().getEventInstance().getPlayers();
				var eim = cm.getPlayer().getEventInstance();
				for (var outt = 0; outt<party.size(); outt++) {//To the cave of choice!
					var exitMapz = eim.getMapInstance(240050400);
					party.get(outt).changeMap(exitMapz, exitMapz.getPortal(0));
					//party.get(outt).removeAll(4001091);
					eim.unregisterPlayer(party.get(outt));
				}
				eim.dispose();
				cm.dispose();
			}
		}
	}
}