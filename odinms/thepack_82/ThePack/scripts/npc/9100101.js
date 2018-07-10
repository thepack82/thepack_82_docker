/*
	Gachapon Script
	Created by Crovax for FlowsionMS
*/

/* Gachapon
*/

importPackage(net.sf.odinms.client);

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (cm.haveItem(5220000, 1)) {
	var prizes = new Array();
	var odds = new Array();
	//Odds for each category. Odds are relative, not percentages, they don't have to add up to anything.
	var globalOdds = Array(20, 20, 20, 20, 20);
	//Potions, other stackable junk
	prizes[0] = Array(2000000, 2000001, 2000002);
	odds[0] = Array(100, 50, 50);
	//Quantity is only used for the stackable junk category.
	var quantity = Array(30, 50, 40);
	//Don't forget to add the items. Using an itemid of 0 will probably cause some errors.
    //FMS items are secret. Note: Who cares? FMS is dead.
	//Scrolls
	prizes[1] = Array(0, 0, 0);
	odds[1] = Array(1, 1, 1);
	//Weapons
	prizes[2] = Array(0, 0, 0);
	odds[2] = Array(1, 1, 1);
	//Armors
	prizes[3] = Array(0, 0, 0);
	odds[3] = Array(1, 1, 1);
	//NX equips prizes
	prizes[1] = Array(0, 0, 0);
	odds[1] = Array(1, 1, 1);
	var totalodds = 0;
	var catChoice = 0;
	var itemChoice = 0;
	var itemQuantity = 1;

	/* 1. Add up the probabilities of each category (This way, you can freely add/remove/edit the prize
	      tables without screwing anything up).
	   2. Randomly determine prize category.
	   3. If the category is for equipment, set the odds for all opposite-gender equips to 0.
	   4. Add up the probabilities of each item.
	   5. Randomly determine item number.
	   6. If the item is a potion/stackable junk, grab the quantity too.
	   7. Award item. */

	for (var i = 0; i < globalOdds.length; i++) {
		totalodds += globalOdds[i];
	}

	var randomPick = Math.floor(Math.random()*totalodds)+1;

	for (var i = 0; i < globalOdds.length; i++) {
		randomPick -= globalOdds[i];
		if (randomPick <= 0) {
			catChoice = i;
			randomPick = totalodds + 100;
			i = globalOdds.length;
		}
	}

	totalodds = 0;
	//category 3 and 4 are for armors and NX items.
	if ((catChoice == 3) || (catChoice == 4)) {
		for (var i = 0; i < odds[catChoice].length; i++) {
			var itemGender = (Math.floor(prizes[catChoice][i]/1000)%10);
			if ((cm.getChar().getGender() != itemGender) && (itemGender != 2)) {
				odds[catChoice][i] = 0;
			}
		}
	}
	for (var i = 0; i < odds[catChoice].length; i++) {
		totalodds += odds[catChoice][i];
	}
	randomPick = Math.floor(Math.random()*totalodds)+1;
	for (var i = 0; i < odds[catChoice].length; i++) {
		randomPick -= odds[catChoice][i];
		if (randomPick <= 0) {
			itemChoice = i;
			randomPick = totalodds + 100;
			i = odds[catChoice].length;
		}
	}
	if (catChoice == 0) {
		itemQuantity = quantity[itemChoice];
	}
	//category 2 and 3 are for equipment/weapons, so randomize the stats.
	//I think a function here is missing. Will be updated next rev.
	if ((catChoice == 2) || (catChoice == 3)) {
		var ii = net.sf.odinms.server.MapleItemInformationProvider.getInstance();
		var newItem = ii.randomizeStats(ii.getEquipById(prizes[catChoice][itemChoice]),true);
		net.sf.odinms.server.MapleInventoryManipulator.addFromDrop(cm.getC(), newItem, "Created " + ii.getName(prizes[catChoice][itemChoice])  + " from Kerning Gachapon.");
	} else {
		cm.gainItem(prizes[catChoice][itemChoice], itemQuantity);
		}
		cm.gainItem(5220000, -1);
		cm.dispose();
	} else {
		cm.sendOk("Here's Gachapon.");
		cm.dispose();
	}
}