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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Pison - Florina Beach(110000000)
-- By ---------------------------------------------------------------------------------------------
	Information & Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Add null map check [Xterminator]
	1.0 - First Version
---------------------------------------------------------------------------------------------------
**/

importPackage(net.sf.odinms.server.maps);
var status = 0;
var returnmap = cm.getChar().getSavedLocation(SavedLocationType.FLORINA);

function start() {
    if (returnmap == -1)
        returnmap = 104000000;
    cm.sendNext("So you want to leave #b#m110000000##k? If you want, I can take you back to #b#m"+returnmap+"##k.");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendNext("You must have some business to take care of here. It's not a bad idea to take some rest at #m"+returnmap+"# Look at me; I love it here so much that I wound up living here. Hahaha anyway, talk to me when you feel like going back.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendYesNo("Are you sure you want to return to #b#m"+returnmap+"##k? Alright, we'll have to get going fast. Do you want to head back to #m"+returnmap+"# now?")
        else if (status == 2)
            cm.warp(returnmap);
        cm.dispose();
    }
}
