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

/*Wu Yang
 *Pirate PQ
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
	if (mapId == 925100500)//The Captains Dignity
		curMap = 1;
	else if (mapId == 925100600) //Wu Yang giving thanks
		curMap = 2;
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
		if (curMap == 1) {
			if (playerStatus) { //Is teh leader
				var eim = cm.getPlayer().getEventInstance();
				var party = cm.getPlayer().getEventInstance().getPlayers();
				var winMap = eim.getMapInstance(925100600);
				if (status == 0) {
					cm.sendNext("Thank you for saving me from Lord Pirate. Finally, the Bellflowers may be out of the grasp of Lord Pirates.");
				} else if (status == 1) {
					for (var iii=0; iii < party.size(); iii++) {
						party.get(iii).changeMap(winMap, winMap.getPortal(0));
						eim.unregisterPlayer(party.get(iii));
					}
					cm.dispose();
				}
			} else {
				cm.sendNext("Please ask yur party leader to speak to me.");
				cm.dipsose();
			}
		} else if (curMap == 2) {
			if (status == 0) {
				cm.sendNext("Thank you for saving us Bellflowers, your work will be remembered for eternity.");
			} else if (status == 1) {
				if (cm.getPlayer().isGM() == false) {
					for (var i = 0; i < PQItems.length; i++) {
						cm.removeAll(PQItems[i]);
					}
				}
				cm.warp(251010404, 0);
				cm.dispose();
			}
		}
	}
}
			
			