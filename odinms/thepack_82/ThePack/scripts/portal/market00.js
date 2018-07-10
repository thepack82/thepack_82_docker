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

importPackage(net.sf.odinms.server.maps);
importPackage(net.sf.odinms.net.channel);

/*
Return from Free Market Script
*/

function enter(pi) {
	var returnMap = pi.getPlayer().getSavedLocation(SavedLocationType.FREE_MARKET);
	if (returnMap < 0) {
		returnMap = 102000000; // to fix people who entered the fm trough an unconventional way
	}
	var target = pi.getPlayer().getClient().getChannelServer().getMapFactory().getMap(returnMap);
	var targetPortal;
	if (returnMap == 230000000) { // aquaroad has a different fm portal - maybe we should store the used portal too?
		targetPortal = target.getPortal("market01");
	} else {
		targetPortal = target.getPortal("market00");
	}
	if (targetPortal == null) {
		targetPortal = target.getPortal(0);
	}
	pi.getPlayer().clearSavedLocation(SavedLocationType.FREE_MARKET);
	pi.getPlayer().changeMap(target, targetPortal);
	return true;
}