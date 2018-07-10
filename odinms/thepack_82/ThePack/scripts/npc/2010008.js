/*
	This file was written by "StellarAshes" <stellar_dust@hotmail.com> 
			as a part of the Guild package for
			the OdinMS Maple Story Server
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

/* guild emblem npc */
var status = 0;
var sel;
function start() {
	status = -1;
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

		if (status == 0)
			cm.sendSimple("What would you like to do?\r\n#b#L0#Create/Change your Guild Emblem#l#k");
		else if (status == 1) {
			sel = selection;
			if (sel == 0) {
				if (cm.getChar().getGuildRank() == 1)
					cm.sendYesNo("Creating or changing Guild Emblem costs #b" + cm.getChar().emblemCost() + " mesos#k, are you sure you want to continue?");
				else
					cm.sendOk("You must be the Guild Leader to change the Emblem.  Please tell your leader to speak with me.");
			}
		}
		else if (status == 2) {
			if (sel == 0) {
				cm.getChar().genericGuildMessage(17);
				cm.dispose();
			}
		}
	}
}
