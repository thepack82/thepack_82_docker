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

/* Chamberlain Eak
 *@author Jvlaple
*/
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.server.life);
importPackage(java.awt);
importPackage(java.lang);

var status;
var curMap;
var playerStatus;
var chatState;
//var party;
var preamble;

var SRArrays = Array(Array(0, 0, 5), //Sealed Room
					Array(0, 1, 4),
					Array(0, 2, 3),
					Array(0, 3, 2),
					Array(0, 4, 1),
					Array(0, 5, 0),
					Array(1, 0, 4),
					Array(1, 1, 3),
					Array(1, 2, 2),
					Array(1, 3, 1),
					Array(1, 4, 0),
					Array(2, 0, 3),
					Array(2, 1, 2),
					Array(2, 2, 1),
					Array(2, 3, 0),
					Array(3, 0, 2),
					Array(3, 1, 1),
					Array(3, 2, 0),
					Array(4, 0, 1),
					Array(4, 1, 0),
					Array(5, 0, 0));

			

function start() {
	status = -1;
	mapId = cm.getChar().getMapId();
	if (mapId == 920010000) //Entrance
		curMap = 1;
	else if (mapId == 920010100) //Center Tower
		curMap = 2;
	else if (mapId == 920010200)//Walkway
		curMap = 3;
	else if (mapId == 920010300)//Storage
		curMap = 4;
	else if (mapId == 920010400)//Lobby
		curMap = 5;
	else if (mapId == 920010500)//SR
		curMap = 6;
	else if (mapId == 920010600)//Lounge
		curMap = 7;
	else if (mapId == 920010700)//On the way up
		curMap = 8;
	else if (mapId == 920010800)//Garden
		curMap = 9;
	else if (mapId == 920011000)//Room of Darkness
		curMap = 11;
	else if (mapId == 920011200) //Exit
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
		if (curMap == 1) { // First Stage.
			if (playerStatus) { // party leader
				var eim = cm.getChar().getEventInstance(); // OPQ
				var party = cm.getChar().getEventInstance().getPlayers();
				if (status == 0) {
					party = eim.getPlayers();
					var cleared = eim.getProperty("1stageclear");
					if (cleared == null) {
						cm.sendNext("Thank you for rescuing me. I will now teleport you to the Door of the Goddess's Tower.");
					}
					else { //If complete, just warp to the top :D
							var complete = eim.getProperty(curMap.toString() + "stageclear");
							if (complete != null) {
								cm.warp(920010000, 2);
								cm.dispose();
						}
					}
				} else if (status == 1) {
						cm.givePartyExp(6000, party);
						//eim.setProperty("1stageclear", "true");
						clear(1, eim, cm);
						for (var outt = 0; outt<party.size(); outt++)
							{//Kick everyone out =D
								var exitMapz = eim.getMapInstance(920010000);
								party.get(outt).changeMap(exitMapz, exitMapz.getPortal(2)); //Top
							}
						cm.dispose();
					}
				} else { // non leader
				if (status == 0) {
					var eim = cm.getChar().getEventInstance();
					var complete = eim.getProperty(curMap.toString() + "stageclear");
					if (complete != null) {
						cm.warp(920010000, 2);
						cm.dispose();
					} else {
						cm.sendOk("Please ask your party leader to talk to me.");
						cm.dispose();
					}
				}
			} //End of Entrance Eak
		} else if (curMap == 2) { //Center Tower
			if (playerStatus) { //Me IS party leader :D
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var thisMap = mf.getMap(920010100);
				if (thisMap.getReactorByName("scar1").getState() == 1 &&
					thisMap.getReactorByName("scar2").getState() == 1 &&
					thisMap.getReactorByName("scar3").getState() == 1 &&
					thisMap.getReactorByName("scar4").getState() == 1 &&
					thisMap.getReactorByName("scar5").getState() == 1 &&
					thisMap.getReactorByName("scar6").getState() == 1) {
					cm.mapMessage("You are teleported to the Garden!");
					var party = cm.getChar().getEventInstance().getPlayers();
					for (var outt = 0; outt<party.size(); outt++)
						{//Warp to garden
							var exitMapz = eim.getMapInstance(920010800);
							party.get(outt).changeMap(exitMapz, exitMapz.getPortal(0)); //This is the garden.
						}
					cm.dispose();
				} else {
					cm.sendOk("Please fix the Goddess statue back up before clicking on me!");
					cm.dispose();
				}
			} else {
				cm.sendOk("Please ask your party leader to talk to me.");
				cm.dispose();
			}
		} else if (curMap == 3) { //Walkway
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				premable = eim.getProperty("leader3rdpremable");
				if (premable == null) {
					cm.sendNext("This is the walkway of the Tower of the Goddess. The Pixies broke #bStatue of Goddess: 1st Piece#k into 30 pieces, and took each and everyone of them. Please eliminate the Pixie's and bring back the #b1st Small Piece#k. In return, I will make Statue of Goddess: 1st Piece out of them. The Pixies have been strengthened by the power of the statue of goddess, so please be careful~");
					eim.setProperty("leader3rdpremable", "done");
					cm.dispose();
				} else {
						var complete = eim.getProperty("3stageclear");
						if (complete != null) {
							cm.sendNext("Please go back to the Center Tower and continue the quest.");
							cm.dispose();
						}else if (cm.haveItem(4001050, 30)) {
							cm.sendNext("Thank you for bringing me back the 30 Small Pieces. I will make #bStatue of Goddess: 1st Piece#k right now.");
							cm.gainItem(4001050, -30);
							cm.gainItem(4001044, 1);
							cm.givePartyExp(7500, party);
							clear(3, eim, cm);
							cm.dispose();
						} else {
							cm.sendNext("You have not got 30#b 1st Small Piece#k yet.");
							cm.dispose();
					}
				}
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("This is the walkway of the Tower of the Goddess. The Pixies broke #bStatue of Goddess: 1st Piece#k into 30 pieces, and took each and everyone of them. Please eliminate the Pixie's and bring back the #b1st Small Piece#k. In return, I will make Statue of Goddess: 1st Piece out of them. The Pixies have been strengthened by the power of the statue of goddess, so please be careful~");
					cm.dispose();
				} else {
					cm.sendNext("Please go back to the Center Tower and continue the quest.");
					cm.dispose();
				}
			}
		}else if (curMap == 4) {
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				premable = eim.getProperty("leader4thpremable");
				if (premable == null) {
					cm.sendNext("This was formerly the storage area of the Tower of Goddess, but now, it has turned into a home of the Cellions. A Cellion took #bStatue of Goddess: 2nd#k Piece and hid with it, so it's your job to defeat it and bring back to the #bStatue of Goddess:2nd Piece#k.");
					eim.setProperty("leader4thpremable", "done");
					cm.dispose();
				} else {
						var complete = eim.getProperty(curMap.toString() + "stageclear");
						if (complete != null) {
							cm.sendNext("Please go back to the Center Tower and continue the quest.");
							cm.dispose();
						}else if (cm.haveItem(4001045, 1)) {
							cm.sendNext("Thank you for getting back the piece, use it to fix the Statue of Goddess.");
							clear(4, eim, cm);
							cm.givePartyExp(7500, party);
							cm.dispose();
						} else {
							cm.sendNext("You do not have the 2nd Statue Piece to clear this stage.");
							cm.dispose();
					}
				}
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("This was formerly the storage area of the Tower of Goddess, but now, it has turned into a home of the Cellions. A Cellion took #bStatue of Goddess: 2nd#k Piece and hid with it, so it's your job to defeat it and bring back to the #bStatue of Goddess:2nd Piece#k.");
					cm.dispose();
				} else {
					cm.sendNext("Please go back to the Center Tower and continue the quest.");
					cm.dispose();
				}
			}
		}else if (curMap == 5) { //Lobby
		if (playerStatus) { //Is leader
				var dayNames = Array("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				var dayTxt = dayNames[cm.getDayOfWeek() - 1];
				var thisMap = mf.getMap(920010400);
				var today = cm.getDayOfWeek();
				premable = eim.getProperty("leader5thpremable");
				if (premable == null) {
					cm.sendNext("This is the lobby of the Tower of Goddess. This is the place Minerva the Goddess preferred to listen to music. She loved listening to different kinds of music, depending on what day of the week it was. If you play that music you played before, the spirit of Minerva the Goddess may react to it and ... something curious may happen.\r\n#eMake sure you drop the right CD first time. There is NO backup.#n\r\n\r\nToday is:\r\n" + dayTxt);
					eim.setProperty("leader5thpremable", "done");
					cm.dispose();
				} else {
						var complete = eim.getProperty(curMap.toString() + "stageclear");
						if (complete != null) {
							cm.sendNext("Please go back to the Center Tower and continue the quest.");
							cm.dispose();
						}else if (eim.getMapInstance(920010400).getReactorByName("music").getMode() != 0) {
							if (thisMap.getReactorByName("music").getMode() == today) {
								cm.sendNext("Wow, you did it!");
								clear(5, eim, cm);
								//cm.gainItem(4001046, 1);
								cm.givePartyExp(5500, party);
								//cm.spawnReactor(9102000, new java.awt.Point(-342, -146), 0);
								thisMap.setReactorState(thisMap.getReactorByName("stone3"), 1);
								cm.dispose();
							} else {
								cm.sendNext("Wrong disc, sorry, try again!");
								cm.dispose();
							}
							cm.dispose();
						} else {
							cm.sendNext("You have not played the CD yet.");
							cm.dispose();
					}
				}
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				var dayNames = Array("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				var dayTxt = dayNames[cm.getDayOfWeek() - 1];
				var thisMap = mf.getMap(920010400);
				var today = cm.getDayOfWeek();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("This is the lobby of the Tower of Goddess. This is the place Minerva the Goddess preferred to listen to music. She loved listening to different kinds of music, depending on what day of the week it was. If you play that music you played before, the spirit of Minerva the Goddess may react to it and ... something curious may happen.\r\n#eMake sure you drop the right CD first time. There is NO backup.#n\r\n\r\nToday is:\r\n" + dayTxt);
					cm.dispose();
				} else {
					cm.sendNext("Please go back to the Center Tower and continue the quest.");
					cm.dispose();
				}
			}
		}else if (curMap == 6) { //SR
			sealedRoom(cm);
		}else if (curMap == 7) { //Lounge
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				premable = eim.getProperty("leader7thpremable");
				if (premable == null) {
					cm.sendNext("This is the lounge area of the Tower of Goddess, where the guests stayed for the night or two. #b<Statue of Goddess: 5th Piece>#k has been broken into 40 pieces, and separated all around the lounge. Please roam around and collect the pieces of the #b5th Small Pieces#k.");
					eim.setProperty("leader7thpremable", "done");
					cm.dispose();
				} else {
						var complete = eim.getProperty("7stageclear");
						if (complete != null) {
							cm.sendNext("Please go back to the Center Tower and continue the quest.");
							cm.dispose();
						}else if (cm.haveItem(4001052, 40)) {
							cm.sendNext("Thank you for bringing me back the 40 Small Pieces. I will make #bStatue of Goddess: 5th Piece#k right now.");
							cm.gainItem(4001052, -40);
							cm.gainItem(4001048, 1);
							cm.givePartyExp(7500, party);
							clear(7, eim, cm);
							cm.dispose();
						} else {
							cm.sendNext("You have not got 40#b 5th Small Piece#k yet.");
							cm.dispose();
					}
				}
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("This is the lounge area of the Tower of Goddess, where the guests stayed for the night or two. #b<Statue of Goddess: 5th Piece>#k has been broken into 40 pieces, and separated all around the lounge. Please roam around and collect the pieces of the #b5th Small Pieces#k.");
					cm.dispose();
				} else {
					cm.sendNext("Please go back to the Center Tower and continue the quest.");
					cm.dispose();
				}
			}
		}else if (curMap == 8) { //Way up
			if (playerStatus) { //Is leader
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				premable = eim.getProperty("leader8thpremable");
				if (premable == null) {
					cm.sendNext("Since this stage is incomplete you can have the statue piece for free...");
					eim.setProperty("leader8thpremable", "done");
					cm.gainItem(4001049, 1);
					cm.givePartyExp(7500, party);
					clear(8, eim, cm);
					cm.dispose();
				} else {
					cm.sendNext("Please go back to the Center Tower and continue the quest.");
					cm.dispose();
				}
			} else {
				var eim = cm.getChar().getEventInstance();
				var mf = eim.getMapFactory();
				var party = cm.getChar().getEventInstance().getPlayers();
				if (eim.getProperty(curMap.toString() + "stageclear") == null) {
					cm.sendNext("Ask your leader to talk to me...");
					cm.dispose();
				} else {
					cm.sendNext("Please go back to the Center Tower and continue the quest.");
					cm.dispose();
				}
			}
		}else if (curMap == 11) {
			cm.sendNext("Argh, so dark here. You're now inside the Room of Darkness in the Tower of Goddess, but then, how did you get here? You won't find a piece of the Statue of Goddess here. I suggest you check out the other rooms instead.");
			cm.dispose();
		}else if (curMap == 999) {
			cm.warp(200080101, 0);
			cm.dispose();
		}else {
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
	var mf = eim.getMapFactory();
	map = mf.getMap(920010000 + stage * 100);
	//eim.addMapInstance(670010200 + stage,map);
	//cm.givePartyExp(2, party);
	//cm.mapMessage("Clear!");
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function sealedRoom (cm) {
	var debug = true;
	var eim = cm.getChar().getEventInstance();
	var nthtext = "6th";
	var curcombo = SRArrays;
	var currect = cm.getChar().getMap().getAreas();
	var objset = [0,0,0];
		if (curMap == 6) { //SR
	        if (playerStatus) { // leader
	                if (status == 0) {
	                        // check for preamble
	                     
	                        party = eim.getPlayers();
	                        preamble = eim.getProperty("leader" + nthtext + "preamble");
	                        if (preamble == null) {
	                                cm.sendNext("This is the Sealed Room of the Tower of Goddess. It's the room where Minerva the Goddess felt safe enough to keep her very valuable belongings. The three steps you see on the side work as the locks that can unseal the curse, and they all have to carry the exact amount of weight. Let's see... it'll require five of you to stand on it to match the ideal weight. By the way, you'll need to solve this in 7 attempts or less, or you'll be banished from the sealed room, as well as changing up the answer in the process.");
	                                eim.setProperty("leader" + nthtext + "preamble","done");
	                                var sequenceNum = Math.floor(Math.random() * curcombo.length);
	                                eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
									eim.setProperty("stage6Tries", "0");
	                                cm.dispose();
	                        }
	                        else {
	                        	// otherwise
	                        	// check for stage completed
	                        	var complete = eim.getProperty(curMap.toString() + "stageclear");
	                        	if (complete != null) {	
	                        		var mapClear = curMap.toString() + "stageclear";
	                        		eim.setProperty(mapClear,"true"); // Just to be sure
	                        		cm.sendNext("Please go back to Center Tower and continue the quest!");
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
	                        	        // first, are there 5 players on the objset?
	                        	        if (totplayers == 5 /*|| debug*/) {
	                        	                var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
	                        	                // debug
	                        	                // combo = curtestcombo;
	                        	                var testcombo = true;
												var right = 0;
												var wrong = 0;
												var sndString = "";
	                        	                for (i = 0; i < objset.length; i++) {
	                        	                	if (combo[i] != objset[i]){
	                        	                		testcombo = false;
														wrong += 1;
													} else {
														right += 1;
													}
	                        	                }
												if (right > 1) {
													sndString += right + " correct.\r\n";
												}
												if (wrong > 1) {
													sndString += wrong + " different.\r\n";
												}
	                        	                if (testcombo || debug) {
	                        	                        // do clear
	                        	                        clear(curMap,eim,cm);
	                        	                        var exp = 7500;
	                        	                        cm.givePartyExp(exp, party);
														//cm.getPlayer().getEventInstance().getMapInstance(920010500).getReactorByName("stone4").setState(1);
														cm.getPlayer().getMap().setReactorState(cm.getPlayer().getMap().getReactorByName("stone4"), 1);
	                        	                        cm.dispose();
	                        	                }
	                        	                else { // wrong
	                        	                        // do wrong
	                        	                        //failstage(eim,cm);
														var tries = Integer.parseInt(eim.getProperty("stage6Tries"));
														eim.setProperty("stage6Tries", tries + 1);
														if (tries > 6) {
															cm.mapMessage(5, "You have been Banished from the Sealed Room, and the Combination shall be reset.");
															var sequenceNum = Math.floor(Math.random() * curcombo.length); // Reset combo
															eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
															eim.setProperty("stage6Tries", "0");//set back to zero
															for (var outt = 0; outt<party.size(); outt++) {//Kick everyone out =D
																var exitMapz = eim.getMapInstance(9200100100);
																party.get(outt).changeMap(exitMapz, exitMapz.getPortal(0)); //Out
															}
														} else {
															cm.sendOk(sndString);
														}
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
													var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
													//eim.setProperty("6stageclear", "true");
													//cm.getPlayer().getEventInstance().getMapInstance(920010500).getReactorByName("stone4").hitReactor(02, 1, cm.getPlayer().getClient());
													//cm.getPlayer().getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.triggerReactor(cm.getPlayer().getEventInstance().getMapInstance(920010500).getReactorByName("stone4"), 0));
													//cm.sendNext(combo);
													//clear(curMap,eim,cm);
													//cm.getPlayer().getEventInstance().getMapInstance(920010500).getReactorByName("stone4").setState(1);
	                        	                }
	                        	                else
													cm.sendNext("Looks like you haven't found the platforms just yet. Keep trying!!");
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
	                		//var target = eim.getMapInstance(920010100);
							//var targetPortal = target.getPortal("st00");
	                		//cm.getChar().changeMap(target, targetPortal);
	                	}
	                	cm.dispose();
	                }
	        }
	        else { // not leader
	        	if (status == 0) {
	        	        var complete = eim.getProperty(curMap.toString() + "stageclear");
	        	        if (complete != null) {
	        	        	cm.sendNext("Please go back to Center Tower and continue the quest!");
							cm.dispose();
	        	        }
	        	        else {
	        	        	cm.sendNext("Please have the party leader talk to me.");
	        	        	cm.dispose();
	        	        }
	        	}
			else {
	                	var complete = eim.getProperty(curMap.toString() + "stageclear");
			       	if (complete != null) {	
					//var target = eim.getMapInstance(920010100);
					//var targetPortal = target.getPortal("st00");
	                		//cm.getChar().changeMap(target, targetPortal);
				}
	                	cm.dispose();
	                }
	        }
		}
}

