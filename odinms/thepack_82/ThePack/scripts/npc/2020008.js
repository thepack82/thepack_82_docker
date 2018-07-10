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

/* Tylus
	Warrior 3rd job advancement
	El Nath: Chief's Residence (211000001)
*/


importPackage(net.sf.odinms.client);

var status = 0;
var minLevel = 120;
var maxLevel = 200;
var minPlayers = 0;
var maxPlayers = 6;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.sendOk("Make up your mind and visit me again.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getQuestStatus(6192).equals(net.sf.odinms.client.MapleQuestStatus.Status.STARTED)) {
                if (cm.getParty() == null) { // no party
                    cm.sendOk("Please talk to me again after you've formed a party.");
                    cm.dispose();
                    return;
                }
                if (!cm.isLeader()) { // not party leader
                    cm.sendOk("Please ask your party leader to talk to me.");
                    cm.dispose();
                } else {
                    // Check teh partyy
                    var party = cm.getParty().getMembers();
                    var mapId = cm.getChar().getMapId();
                    var next = true;
                    var levelValid = 0;
                    var inMap = 0;
                    // Temp removal for testing
                    if (party.size() < minPlayers || party.size() > maxPlayers)
                        next = false;
                    else {
                        for (var i = 0; i < party.size() && next; i++) {
                            if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                                levelValid += 1;
                            if (party.get(i).getMapid() == mapId)
                                inMap += 1;
                        }
                        if (levelValid < minPlayers || inMap < minPlayers)
                            next = false;
                    }
                    if (next) {
                        // Kick it into action.  Slate says nothing here, just warps you in.
                        var em = cm.getEventManager("ElnathPQ");
                        if (em == null) {
                            cm.sendOk("unavailable");
                            cm.dispose();
                        }
                        else {
                            // Begin the PQ.
                            var eim = em.startInstance(cm.getParty(),cm.getChar().getMap());
                            cm.dispose();
                        }
                        cm.dispose();
                    }
                    else {
                        cm.sendOk("Your party is not a party of three to six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
                        cm.dispose();
                    }
                }
                cm.dispose();
            }
            else if (!(cm.getJob().equals(MapleJob.PAGE) || cm.getJob().equals(MapleJob.FIGHTER) || cm.getJob().equals(MapleJob.SPEARMAN))) {
                if ((cm.getJob().equals(MapleJob.WHITEKNIGHT) || cm.getJob().equals(MapleJob.CRUSADER) || cm.getJob().equals(MapleJob.SPEARMAN)) && cm.getLevel() >= 120) {
                    cm.sendOk("Please go visit #bHarmonia#k. She resides in #bLeafre#k.");
                    cm.dispose();
                    return;
                } else {
                    cm.sendOk("Want a good time baby?");
                    cm.dispose();
                    return;
                }
            }
            if ((cm.getJob().equals(MapleJob.PAGE) || cm.getJob().equals(MapleJob.FIGHTER) || cm.getJob().equals(MapleJob.SPEARMAN)) && cm.getLevel() >= 70 &&  cm.getChar().getRemainingSp() <= (cm.getLevel() - 70) * 3) {
                cm.sendYesNo("I knew this day would come eventually.\r\n\r\nAre you ready to become much stronger than ever before?");
            } else if (cm.getQuestStatus(6192).equals(net.sf.odinms.client.MapleQuestStatus.Status.STARTED) == false) {
                cm.sendOk("Your time has yet to come...");
                cm.dispose();
            }
        } else if (status == 1) {
            if (cm.getJob().equals(MapleJob.PAGE)) {
                cm.changeJob(MapleJob.WHITEKNIGHT);
                cm.getChar().gainAp(5);
                cm.sendOk("You are now a #bWhite Knight#k.\r\n\r\nNow go, with pride!");
                cm.dispose();
            } else if (cm.getJob().equals(MapleJob.FIGHTER)) {
                cm.changeJob(MapleJob.CRUSADER);
                cm.getChar().gainAp(5);
                cm.sendOk("You are now a #bCrusader#k.\r\n\r\nNow go, with pride!");
                cm.dispose();
            } else if (cm.getJob().equals(MapleJob.SPEARMAN)) {
                cm.changeJob(MapleJob.DRAGONKNIGHT);
                cm.getChar().gainAp(5);
                cm.sendOk("You are now a #bDragon Knight#k.\r\n\r\nNow go, with pride!");
                cm.dispose();
            }
        }
    }
}	
