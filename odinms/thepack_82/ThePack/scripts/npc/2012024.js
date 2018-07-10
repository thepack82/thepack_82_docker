/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Egnet - Before Takeoff To Ariant(200000152)
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.3 - Add missing return statement [Thanks safiq for the bug, fix by Information]
	1.2 - Replace function to support latest [Information]
	1.1 - Fix wrong placed statement [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(net.sf.odinms.client);
var gm;

function start() {
	status = -1;
	gm = cm.getEventManager("Geenie");
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if(mode == -1) {
		cm.dispose();
		return;
	} else {
		status++;
		if(mode == 0) {
			cm.sendOk("You'll get to your destination in moment. Go ahead and talk to other people, and before you know it, you'll be there already.");
			cm.dispose();
			return;
		}
		if(status == 0) {
			cm.sendYesNo("Do you want to leave the waiting room? You can, but the ticket is NOT refundable. Are you sure you still want to leave this room?");
		} else if(status == 1) {
			cm.warp(200000151);
			cm.dispose();
		}
	}
}
