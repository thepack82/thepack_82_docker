/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Amos the Strong
 * 
 * 
 * Amorian Challenge PQ NPC
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

var stage2combos = Array(Array(5,0,0),//Rope stage combos
						Array(4,1,0),
						Array(3,2,0),
						Array(2,3,0),
						Array(1,4,0),
						Array(0,5,0),
						Array(0,4,1),
						Array(0,3,2),
						Array(0,2,3),
						Array(0,1,4),
						Array(0,0,5),
						Array(1,0,4),
						Array(2,0,3),
						Array(3,0,2),
						Array(4,0,1),
						Array(3,1,1),
						Array(2,2,1),
						Array(2,1,2),
						Array(1,3,1),
						Array(1,2,2),
						Array(1,1,3));
			
var stage3combos = Array(Array(0, 0, 0, 0, 1, 1, 1, 1, 1), //Lol, it took me forever to work these out :D
						Array(0, 0, 0, 1, 0, 1, 1, 1, 1),
						Array(0, 0, 0, 1, 1, 0, 1, 1, 1),
						Array(0, 0, 0, 1, 1, 1, 0, 1, 1),
						Array(0, 0, 0, 1, 1, 1, 1, 0, 1),
						Array(0, 0, 0, 1, 1, 1, 1, 1, 0),
						Array(0, 0, 1, 0, 0, 1, 1, 1, 1),
						Array(0, 0, 1, 0, 1, 0, 1, 1, 1),
						Array(0, 0, 1, 0, 1, 1, 0, 1, 1),
						Array(0, 0, 1, 0, 1, 1, 1, 0, 1),
						Array(0, 0, 1, 0, 1, 1, 1, 1, 0),
						Array(0, 0, 1, 1, 0, 0, 1, 1, 1),
						Array(0, 0, 1, 1, 0, 1, 0, 1, 1),
						Array(0, 0, 1, 1, 0, 1, 1, 0, 1),
						Array(0, 0, 1, 1, 0, 1, 1, 1, 0),
						Array(0, 0, 1, 1, 1, 0, 0, 1, 1),
						Array(0, 0, 1, 1, 1, 0, 1, 0, 1),
						Array(0, 0, 1, 1, 1, 0, 1, 1, 0),
						Array(0, 0, 1, 1, 1, 1, 0, 0, 1),
						Array(0, 0, 1, 1, 1, 1, 0, 1, 0),
						Array(0, 0, 1, 1, 1, 1, 1, 0, 0),
						Array(0, 1, 0, 0, 0, 1, 1, 1, 1),
						Array(0, 1, 0, 0, 1, 0, 1, 1, 1),
						Array(0, 1, 0, 0, 1, 1, 0, 1, 1),
						Array(0, 1, 0, 0, 1, 1, 1, 0, 1),
						Array(0, 1, 0, 0, 1, 1, 1, 1, 0),
						Array(0, 1, 0, 1, 0, 0, 1, 1, 1),
						Array(0, 1, 0, 1, 0, 1, 0, 1, 1),
						Array(0, 1, 0, 1, 0, 1, 1, 0, 1),
						Array(0, 1, 0, 1, 0, 1, 1, 1, 0),
						Array(0, 1, 0, 1, 1, 0, 0, 1, 1),
						Array(0, 1, 0, 1, 1, 0, 1, 0, 1),
						Array(0, 1, 0, 1, 1, 0, 1, 1, 0),
						Array(0, 1, 0, 1, 1, 1, 0, 0, 1),
						Array(0, 1, 0, 1, 1, 1, 0, 1, 0),
						Array(0, 1, 0, 1, 1, 1, 1, 0, 0),
						Array(0, 1, 1, 0, 0, 0, 1, 1, 1),
						Array(0, 1, 1, 0, 0, 1, 0, 1, 1),
						Array(0, 1, 1, 0, 0, 1, 1, 0, 1),
						Array(0, 1, 1, 0, 0, 1, 1, 1, 0),
						Array(0, 1, 1, 0, 1, 0, 0, 1, 1),
						Array(0, 1, 1, 0, 1, 0, 1, 0, 1),
						Array(0, 1, 1, 0, 1, 0, 1, 1, 0),
						Array(0, 1, 1, 0, 1, 1, 0, 0, 1),
						Array(0, 1, 1, 0, 1, 1, 0, 1, 0),
						Array(0, 1, 1, 0, 1, 1, 1, 0, 0),
						Array(0, 1, 1, 1, 0, 0, 0, 1, 1),
						Array(0, 1, 1, 1, 0, 0, 1, 0, 1),
						Array(0, 1, 1, 1, 0, 0, 1, 1, 0),
						Array(0, 1, 1, 1, 0, 1, 0, 0, 1),
						Array(0, 1, 1, 1, 0, 1, 0, 1, 0),
						Array(0, 1, 1, 1, 0, 1, 1, 0, 0),
						Array(0, 1, 1, 1, 1, 0, 0, 0, 1),
						Array(0, 1, 1, 1, 1, 0, 0, 1, 0),
						Array(0, 1, 1, 1, 1, 0, 1, 0, 0),
						Array(0, 1, 1, 1, 1, 1, 0, 0, 0),
						Array(1, 0, 0, 0, 0, 1, 1, 1, 1),
						Array(1, 0, 0, 0, 1, 0, 1, 1, 1),
						Array(1, 0, 0, 0, 1, 1, 0, 1, 1),
						Array(1, 0, 0, 0, 1, 1, 1, 0, 1),
						Array(1, 0, 0, 0, 1, 1, 1, 1, 0),
						Array(1, 0, 0, 1, 0, 0, 1, 1, 1),
						Array(1, 0, 0, 1, 0, 1, 0, 1, 1),
						Array(1, 0, 0, 1, 0, 1, 1, 0, 1),
						Array(1, 0, 0, 1, 0, 1, 1, 1, 0),
						Array(1, 0, 0, 1, 1, 0, 0, 1, 1),
						Array(1, 0, 0, 1, 1, 0, 1, 0, 1),
						Array(1, 0, 0, 1, 1, 0, 1, 1, 0),
						Array(1, 0, 0, 1, 1, 1, 0, 0, 1),
						Array(1, 0, 0, 1, 1, 1, 0, 1, 0),
						Array(1, 0, 0, 1, 1, 1, 1, 0, 0),
						Array(1, 0, 1, 0, 0, 0, 1, 1, 1),
						Array(1, 0, 1, 0, 0, 1, 0, 1, 1),
						Array(1, 0, 1, 0, 0, 1, 1, 0, 1),
						Array(1, 0, 1, 0, 0, 1, 1, 1, 0),
						Array(1, 0, 1, 0, 1, 0, 0, 1, 1),
						Array(1, 0, 1, 0, 1, 0, 1, 0, 1),
						Array(1, 0, 1, 0, 1, 0, 1, 1, 0),
						Array(1, 0, 1, 0, 1, 1, 0, 0, 1),
						Array(1, 0, 1, 0, 1, 1, 0, 1, 0),
						Array(1, 0, 1, 0, 1, 1, 1, 0, 0),
						Array(1, 0, 1, 1, 0, 0, 0, 1, 1),
						Array(1, 0, 1, 1, 0, 0, 1, 0, 1),
						Array(1, 0, 1, 1, 0, 0, 1, 1, 0),
						Array(1, 0, 1, 1, 0, 1, 0, 0, 1),
						Array(1, 0, 1, 1, 0, 1, 0, 1, 0),
						Array(1, 0, 1, 1, 0, 1, 1, 0, 0),
						Array(1, 0, 1, 1, 1, 0, 0, 0, 1),
						Array(1, 0, 1, 1, 1, 0, 0, 1, 0),
						Array(1, 0, 1, 1, 1, 0, 1, 0, 0),
						Array(1, 0, 1, 1, 1, 1, 0, 0, 0),
						Array(1, 1, 0, 0, 0, 0, 1, 1, 1),
						Array(1, 1, 0, 0, 0, 1, 0, 1, 1),
						Array(1, 1, 0, 0, 0, 1, 1, 0, 1),
						Array(1, 1, 0, 0, 0, 1, 1, 1, 0),
						Array(1, 1, 0, 0, 1, 0, 0, 1, 1),
						Array(1, 1, 0, 0, 1, 0, 1, 0, 1),
						Array(1, 1, 0, 0, 1, 0, 1, 1, 0),
						Array(1, 1, 0, 0, 1, 1, 0, 0, 1),
						Array(1, 1, 0, 0, 1, 1, 0, 1, 0),
						Array(1, 1, 0, 0, 1, 1, 1, 0, 0),
						Array(1, 1, 0, 1, 0, 0, 0, 1, 1),
						Array(1, 1, 0, 1, 0, 0, 1, 0, 1),
						Array(1, 1, 0, 1, 0, 0, 1, 1, 0),
						Array(1, 1, 0, 1, 0, 1, 0, 0, 1),
						Array(1, 1, 0, 1, 0, 1, 0, 1, 0),
						Array(1, 1, 0, 1, 0, 1, 1, 0, 0),
						Array(1, 1, 0, 1, 1, 0, 0, 0, 1),
						Array(1, 1, 0, 1, 1, 0, 0, 1, 0),
						Array(1, 1, 0, 1, 1, 0, 1, 0, 0),
						Array(1, 1, 0, 1, 1, 1, 0, 0, 0),
						Array(1, 1, 1, 0, 0, 0, 0, 1, 1),
						Array(1, 1, 1, 0, 0, 0, 1, 0, 1),
						Array(1, 1, 1, 0, 0, 0, 1, 1, 0),
						Array(1, 1, 1, 0, 0, 1, 0, 0, 1),
						Array(1, 1, 1, 0, 0, 1, 0, 1, 0),
						Array(1, 1, 1, 0, 0, 1, 1, 0, 0),
						Array(1, 1, 1, 0, 1, 0, 0, 0, 1),
						Array(1, 1, 1, 0, 1, 0, 0, 1, 0),
						Array(1, 1, 1, 0, 1, 0, 1, 0, 0),
						Array(1, 1, 1, 0, 1, 1, 0, 0, 0),
						Array(1, 1, 1, 1, 0, 0, 0, 0, 1),
						Array(1, 1, 1, 1, 0, 0, 0, 1, 0),
						Array(1, 1, 1, 1, 0, 0, 1, 0, 0),
						Array(1, 1, 1, 1, 0, 1, 0, 0, 0),
						Array(1, 1, 1, 1, 1, 0, 0, 0, 0));
			
var GSlime = 9400519;
var PSlime = 9400520;
var OSlime = 9400521;
var BSlime = 9400522;
var randomSlime = Array(9400519, 9400520, 9400521, 9400522);
var Grog = 9400536;
			

function start() {
	status = -1;
	mapId = cm.getChar().getMapId();
	if (mapId == 670010200)
		curMap = 1;
	else if (mapId == 670010300)
		curMap = 2;
	else if (mapId == 670010400)
		curMap = 3;
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
		if (curMap == 1) { // First Stage.
			if (playerStatus) { // party leader
				if (status == 0) {
					var eim = cm.getChar().getEventInstance();
					party = eim.getPlayers();
					preamble = eim.getProperty("leader1stpreamble");
					if (preamble == null) {
						cm.sendNext("Hello and welcome to the first stage. I will spawn 3 portals here, and you will decide which one is the right one, girls will go into the left portal, while guys go into the right portal. Kill the monsters there and then get the leader to talk to my friend, Glimmer Man.");
						var map = eim.getMapInstance(cm.getChar().getMapId());
						var packetglow = MaplePacketCreator.environmentChange("gate",2);	
						map.broadcastMessage(packetglow);
						eim.setProperty("leader1stpreamble","done");
						cm.dispose();
					}
					else { // check how many they have compared to number of party members
                        			// check for stage completed
							var complete = eim.getProperty(curMap.toString() + "stageclear");
							if (complete != null) {
								cm.warp(670010300, 0);
								cm.dispose();
							} else {
								if (cm.haveItem(4031595, 1) == false) {
									cm.sendNext("I'm sorry, but you need a #bPiece of Broken Mirror#k to clear this stage.");
									cm.dispose();
								}
								else {
									cm.sendNext("Congratulations on clearing the first stage! Tell your party members to click on me to continue.");
									clear(1,eim,cm);
									cm.givePartyExp(3000, party);
									cm.gainItem(4031595, -1);
									cm.dispose();
								}
						}
					}
				}
			}
			else { // non leader
				var eim = cm.getChar().getEventInstance();
				party = eim.getPlayers();
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				if (complete != null) {
					cm.warp(670010300, 0);
					cm.dispose();
				} else {		
					cm.sendNext("Hello and welcome to the first stage. I will spawn 3 portals here, and you will decide which one is the right one, girls will go into the left portal, while guys go into the right portal. Kill the monsters there and then get the leader to talk to my friend, Glimmer Man.");
					cm.dispose();
				}
			}
		} // end first map scripts
		
		else if (2 <= curMap && 3 >= curMap) {
			rectanglestages(cm);
		}
		// etc
                else { // no map found
                        cm.sendNext("No map found...");
                        cm.dispose();
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

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function rectanglestages (cm) {
	var debug = false;
	var eim = cm.getChar().getEventInstance();
	if (curMap == 2) {
		var nthtext = "2nd";
		var nthobj = "ropes";
		var nthverb = "hang";
		var nthpos = "hang on the ropes too low";
		var curcombo = stage2combos;
		var currect = cm.getChar().getMap().getAreas();
		var objset = [0,0,0];
	}
	else if (curMap == 3) {
		var nthtext = "3rd";
		var nthobj = "platforms";
		var nthverb = "stand";
		var nthpos = "stand too close to the edges";
		var curcombo = stage3combos;
		var currect = cm.getChar().getMap().getAreas();
		var objset = [0,0,0,0,0,0,0,0,0];
	}
		if (curMap == 2) {
	        if (playerStatus) { // leader
	                if (status == 0) {
	                        // check for preamble
	                     
	                        party = eim.getPlayers();
	                        preamble = eim.getProperty("leader" + nthtext + "preamble");
	                        if (preamble == null) {
	                                cm.sendNext("Hi. Welcome to the 2nd stage. To clear this stage, all you have to do, is have 5 people hang on the ropes. If you get the right amount of people hanging on each rope, the weight will cause the gate to open.");
	                                eim.setProperty("leader" + nthtext + "preamble","done");
	                                var sequenceNum = Math.floor(Math.random() * curcombo.length);
	                                eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
									eim.setProperty("stage2Tries", "0");
	                                cm.dispose();
	                        }
	                        else {
	                        	// otherwise
	                        	// check for stage completed
	                        	var complete = eim.getProperty(curMap.toString() + "stageclear");
	                        	if (complete != null) {	
	                        		var mapClear = curMap.toString() + "stageclear";
	                        		eim.setProperty(mapClear,"true"); // Just to be sure
	                        		cm.sendNext("Please hurry on to the next stage, the portal opened!");
	                        	}
	                        	// check for people on ropes
	                        	else { 
	                        	        // check for people on ropes(objset)
	                        	        var totplayers = 0;
	                        	        for (i = 0; i < objset.length; i++) {
	                        	                for (j = 0; j < party.size(); j++) {
	                        	                        var present = currect.get(i).contains(party.get(j).getPosition());
	                        		                        if (present) {
	                        	                                objset[i] = objset[i] + 1;
	                        	                                totplayers = totplayers + 1;
	                        	                        }
	                        	                }
	                        	        }
	                        	        // compare to correct
	                        	        // first, are there 3 players on the objset?
										var numSpawn = 5;
	                        	        if (totplayers == 5 /*|| debug*/) {
	                        	                var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
	                        	                // debug
	                        	                // combo = curtestcombo;
	                        	                var testcombo = true;
	                        	                for (i = 0; i < objset.length; i++) {
	                        	                	if (combo[i] != objset[i]){
	                        	                		testcombo = false;
														}
	                        	                }
	                        	                if (testcombo || debug) {
	                        	                        // do clear
	                        	                        clear(2,eim,cm);
	                        	                        var exp = (Math.pow(2,curMap) * 350);
	                        	                        cm.givePartyExp(exp, party);
	                        	                        cm.dispose();
	                        	                }
	                        	                else { // wrong
	                        	                        // do wrong
	                        	                        failstage(eim,cm);
														var tries = Integer.parseInt(eim.getProperty("stage2Tries"));
														eim.setProperty("stage2Tries", tries + 1);
														if (tries == 7) {
															cm.sendOk("You got the combination wrong seven times, so now it will be reset.");
															var sequenceNum = Math.floor(Math.random() * curcombo.length);
															eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
															eim.setProperty("stage2Tries", "0");//set back to zero
														}
														//var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
														cm.sendOk(combo);
														//cm.sendOk("Wrong!");1706/300
														/*var numSpawn = 5;
														var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
														for (i = 0; i < objset.length; i++) {
																if (combo[i] != objset[i]){
																numSpawn--;
															}
														}
														var randSlime11 = Math.floor(Math.random() * randomSlime.length);
														var randSlime = randomSlime[randSlime11];
														for (var k = 0; k < numSpawn; k++)
														cm.spawnMonster(randSlime, 1706, 300);*/
														//cm.sendOk(combo);
	                        	                        cm.dispose();
														
	                        	                }
	                        	        }
	                        	        else {
	                        	                if (debug) {
	                        	               		var outstring = "Objects contain:"
	                        	               		for (i = 0; i < objset.length; i++) {
	                        	               			outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
	                        	               		}
	                        	                	cm.sendNext(outstring); 
													//clear(curMap,eim,cm);
	                        	                }
	                        	                else
													cm.sendNext("Looks like you haven't found the ropes just yet. Keep trying!!");
													//var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
													//cm.sendOk(combo);
													//clear(curMap,eim,cm);
													cm.dispose();
	                        	        }
	                        	}
	                        }
	                        // just in case.
	                }
	                else {
	                	var complete = eim.getProperty(curMap.toString() + "stageclear");
	                       	if (complete != null) {	
	                		// var target = eim.getMapInstance(103000800 + curMap);
							// var targetPortal = target.getPortal("st00");
	                		// cm.getChar().changeMap(target, targetPortal);
	        	        	cm.sendNext("Please hurry on to the next stage, the portal opened!");
							cm.dispose();
	                	}
	                	cm.dispose();
	                }
	        }
	        else { // not leader
	        	if (status == 0) {
	        	        var complete = eim.getProperty(curMap.toString() + "stageclear");
	        	        if (complete != null) {
	        	        	cm.sendNext("Please hurry on to the next stage, the portal opened!");
							cm.dispose();
	        	        }
	        	        else {
	        	        	cm.sendNext("Please have the party leader talk to me.");
	        	        	cm.dispose();
	        	        }
	        	}
			else {
	                	var complete = eim.getProperty("2stageclear");
			       	if (complete != null) {	
					var target = eim.getMapInstance(103000800 + curMap);
					var targetPortal = target.getPortal("st00");
	                		cm.getChar().changeMap(target, targetPortal);
				}
	                	cm.dispose();
	                }
	        }
		}	else if (curMap == 3) {
	        if (playerStatus) { // leader
	                if (status == 0) {
	                        // check for preamble
	                     
	                        party = eim.getPlayers();
	                        preamble = eim.getProperty("leader" + nthtext + "preamble");
	                        if (preamble == null) {
	                                cm.sendNext("Hi. Welcome to the third stage. Next to me, there are nine platforms. All you have to do, is have 5 people stand on them and then, the leader must click on me to see if it is correct. If it is #bnot#k correct, a number of #eSlimes#n will spawn and that number is the amount of right platforms that you are standing on. Also, you only have 7 guesses before the combination resets.");
	                                eim.setProperty("leader" + nthtext + "preamble","done");
	                                var sequenceNum = Math.floor(Math.random() * curcombo.length);
	                                eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
									eim.setProperty("stage3Tries", 0);
	                                cm.dispose();
	                        }
	                        else {
	                        	// otherwise
	                        	// check for stage completed
	                        	var complete = eim.getProperty("3stageclear");
	                        	if (complete != null) {	
	                        		var mapClear = "3stageclear";
	                        		eim.setProperty(mapClear,"true"); // Just to be sure
	                        		cm.sendNext("Please hurry on to the next stage, the portal opened!");
	                        	}
	                        	// check for people on ropes
	                        	else { 
	                        	        // check for people on ropes(objset)
	                        	        var totplayers = 0;
	                        	        for (i = 0; i < objset.length; i++) {
	                        	                for (j = 0; j < party.size(); j++) {
	                        	                        var present = currect.get(i).contains(party.get(j).getPosition());
	                        		                        if (present) {
	                        	                                objset[i] = objset[i] + 1;
	                        	                                totplayers = totplayers + 1;
	                        	                        }
	                        	                }
	                        	        }
	                        	        // compare to correct
	                        	        // first, are there 3 players on the objset?
										var numSpawn = 5;
	                        	        if (totplayers == 5 /*|| debug*/) {
	                        	                var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
	                        	                // debug
	                        	                // combo = curtestcombo;
	                        	                var testcombo = true;
	                        	                for (i = 0; i < objset.length; i++) {
	                        	                	if (combo[i] != objset[i]){
	                        	                		testcombo = false;
														}
	                        	                }
	                        	                if (testcombo || debug) {
	                        	                        // do clear
	                        	                        clear(3,eim,cm);
	                        	                        var exp = (Math.pow(2,curMap) * 350);
	                        	                        cm.givePartyExp(exp, party);
	                        	                        cm.dispose();
	                        	                }
	                        	                else { // wrong
	                        	                        // do wrong
	                        	                        //failstage(eim,cm);
														//cm.sendOk("Wrong!");1706/300
														var numSpawn = 5;
														var tries = eim.getProperty("stage3Tries");
														eim.setProperty("stage3Tries", tries + 1);
														if (tries == 7) {
															cm.sendOk("The combination has been reset because you didn't get it right in 7 guesses.");
															var sequenceNum = Math.floor(Math.random() * curcombo.length);
															eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
															eim.setProperty("stage3Tries", 0);//set back to zero
														}
														var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
														for (i = 0; i < objset.length; i++) {
																if (combo[i] != objset[i]){
																numSpawn--;
															}
														}
														var randSlime11 = Math.floor(Math.random() * randomSlime.length);
														var randSlime = randomSlime[randSlime11];
														for (var k = 0; k < numSpawn; k++)
														cm.spawnMonster(randSlime, 1706, 300);
														//cm.sendOk(combo);
														failstage(eim, cm);
	                        	                        cm.dispose();
														
	                        	                }
	                        	        }
	                        	        else {
											if (debug) {
												var outstring = "Objects contain:"
												for (i = 0; i < objset.length; i++) {
													outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
												}
												cm.sendNext(outstring); 
											}
											else
											cm.sendNext("It looks like you haven't found the 5 " + nthobj + " just yet. Please think of a different combination of " + nthobj + ". Only 5 are allowed to " + nthverb + " on " + nthobj + ", and if you " + nthpos + " it may not count as an answer, so please keep that in mind. Keep going!");
											cm.dispose();
	                        	        }
	                        	}
	                        }
	                        // just in case.
	                }
	                else {
	                	var complete = eim.getProperty("3stageclear");
						if (complete != null) {	
	        	        	cm.sendNext("Please hurry on to the next stage, the portal opened!");
							cm.dispose();
	                	}
	                	cm.dispose();
	                }
	        }
	        else { // not leader
	        	if (status == 0) {
	        	        var complete = eim.getProperty(curMap.toString() + "stageclear");
	        	        if (complete != null) {
	        	        	cm.sendNext("Please hurry on to the next stage, the portal opened!");
							cm.dispose();
	        	        }
	        	        else {
	        	        	cm.sendNext("Please have the party leader talk to me.");
	        	        	cm.dispose();
	        	        }
	        	}
			else {
				var complete = eim.getProperty("3stageclear");
				if (complete != null) {	
					cm.sendNext("Please hurry on to the next stage, the portal opened!");
					cm.dispose();
					}
					cm.dispose();
				}
	        }
		}
}
