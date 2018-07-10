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
Sunny (Orbis Train Loader) 2012013
**/

var status = 0;
var tm;
importPackage(net.sf.odinms.client);

function start() {
	action(1, 0, 0);
	tm = cm.getEventManager("Trains");
}

function action(mode, type, selection) {
	if (status == 0) {
		cm.sendYesNo("Do you wish to board the train?");
		status++;
	} else {
		if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
			cm.dispose();
		} else {
			if ((status == 1) && (tm.getProperty("TcanBoard").equals("true"))) {
					cm.warp(200000122, 0);// before takeoff
					cm.dispose();
			}
			if ((status == 1) && (tm.getProperty("trainDocked").equals("false"))){
                            				cm.sendOk("Sorry the train has not arrived yet.\r\nTry again shortly.");
                            				cm.dispose();
                        		}
			if ((status == 1) && (tm.getProperty("TcanBoard").equals("false")) && (tm.getProperty("trainDocked").equals("true"))) {
					cm.sendOk("We are just cleaning the train after it's last journey.\r\nBoarding will commence 5 minutes before departure.\n\rCome back soon.");
					cm.dispose();
		                }			
	                }
                }
}
