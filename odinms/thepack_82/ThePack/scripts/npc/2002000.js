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

/* Rupi
Happyville Warp NPC
*/

function start() {
    cm.sendSimple (" Do you want to go to Happyville or get out of Happyville ?\r\n#L0#I want to go to Happyville !#l\r\n\#L1#I want to get out of Happyville#l");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
//        cm.gainItem(1472063, 1); //what's happyville have to do with gaining items?'
        cm.warp(209000000, 0);
    } else if (selection == 1) {
        cm.warp(101000000, 0);
    }
}