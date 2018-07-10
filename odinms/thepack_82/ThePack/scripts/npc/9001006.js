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

/*

9001006

Coke Exchange Quest

 */

importPackage(net.sf.odinms.client);

var status = 0;
var eQuestChoices = new Array (4000209,4000210,4000211,4000212,4000213,4000214,4000216,4000217,4000219,4000221); 
var eQuestPrizes = new Array();
eQuestPrizes[0] = new Array ([2000000,1],  // Red Potion
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1002418,1],	// Newspaper Hat
[1102061,1],	// Oxygen Tank 
[1102109,1],	// Cape of Warmness
[1102147,1]);	// Toymaker Cape
eQuestPrizes[1] = new Array ([2000006,1],	// Mana Elixir
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1102079,20],	// Ragged Red Cape
[1102080,15],	// Ragged Blue Cape
[1102081,15],	// Ragged Yellow Cape
[1102083,15]);	// Ragged Green Cape
eQuestPrizes[2] = new Array ([2000006,1],	// Mana Elixir
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1102082,20],	// Ragged Black Cape
[1102040,20],	// Yellow Adventurer Cape
[1102041,15],	// Pink Adventurer Cape
[1102042,1],	// Purple Adventurer Cape
[1102043,1]);	// Brown Adventurer Cape
eQuestPrizes[3] = new Array ([2060003,1000],	// Red Arrow for Bow
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1102100,10],	// Amos' Spirit Cape 
[1102145,15],	// Sirius Cloak 
[1102146,15]);	// Zeta Cape
eQuestPrizes[4] = new Array ([2000003,1],	//Blue Potion
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1102084,15],	// Pink Gaia Cape 
[1102085,10],	// Yellow Gaia Cape 
[1102086,15],	// Purple Gaia Cape 
[1102087,1000]);	// Green Gaia Cape
eQuestPrizes[5] = new Array ([2000006,1],	// Mana Elixir
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1102101,1],	// The Legendary Elias Cape 1 
[1102102,1],	// The Legendary Elias Cape 2
[1102103,1]);	// The Legendary Elias Cape 3
eQuestPrizes[6] = new Array ([2022019,5],	// Kinoko Ramen (Pig Bone)
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1102104,1],	// Cecelia Cloak 1
[1102105,1],	// Cecelia Cloak 1
[1102106,1]);	// Cecelia Cloak 3
eQuestPrizes[7] = new Array ([2000003,1],	// Blue Potion
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1442018,10],	// Frozen Tuna (Beginner)
[1422011,15],	// Sake Bottle
[1442023,10],	// Maroon Mop
[1032048,15]);	// Crystal Leaf Earrings
eQuestPrizes[8] = new Array ([2022019,5],	// Kinoko Ramen (Pig Bone)
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1122001,15],	// Bow-tie(Green) 
[1122003,15],	// Bow-tie(Yellow) 
[1122004,15],	// Bow-tie(Pink) 
[1122006,1],	// Bow-tie(Blue)
[1122002,1],	// Bow-tie(Red)
[1122005,1]);	// Bow-tie(Black) 
eQuestPrizes[9] = new Array ([4010004,5],	// Silver Ore
[2022075,1],	// Mini Coke
[2022076,5], 	// Coke Pill
[2022077,5],	// Coke Lite Pill
[2022078,10],	// Coke Zero Pill
[2020031,5],	// Coca Cola
[1082145,1],	// Yellow Work Gloves
[1082146,1],	// Red Work Gloves
[1082147,1],	// Blue Work Gloves
[1082148,1],	// Purple Work Gloves
[1082149,1],	// Brown Work Gloves
[1082150,1],	// Grey Work Gloves
[1082174,1]);	// Lunar Work Gloves
var requiredItem  = 0;
var lastSelection = 0;
var prizeItem     = 0;
var prizeQuantity = 0;
var info;
var itemSet;
var reward;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.sendOk("Really? Let me know if you ever change your mind.");
            cm.dispose();
            return;
        } if (mode == 0 && status == 1) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        if (status == 0) {
            cm.sendYesNo("If you're looking for someone that can pinpoint the characteristics of various items, you're looking at one right now. I'm currently looking for something. Would you like to hear my story?");
        } else if (status == 1) {
            var eQuestChoice = makeChoices(eQuestChoices);
            cm.sendSimple(eQuestChoice);
        } else if (status == 2){
            requiredItem = eQuestChoices[selection];
            reward = eQuestPrizes[selection];
            itemSet = (Math.floor(Math.random() * reward.length));
            prizeItem = reward[itemSet][0];
            prizeQuantity = reward[itemSet][1];
            if (!cm.canHold(prizeItem)){
                cm.sendNext("What? I can't give you the reward if your equip., use, or etc. inventory is full. Please go take a look right now.");
            } else if (checkQuantity(requiredItem) >= 100){   // check they have >= 100 in Inventory
                cm.gainItem(requiredItem,-100);
                cm.gainItem(prizeItem,prizeQuantity);
                cm.sendOk("Hmmm ... if not for this minor scratch ... sigh. I'm afaird I can only deem this a standard-quality item. Well, here's \r\n#t"+ prizeItem +"# for you.");
            } else{
                cm.sendOk("Hey, what do you think you're doing? Go lie someone that DOESN'T know what he's talking about. Not me!");
            }
            cm.dispose();
        }
    }
}

function makeChoices(a){
    var result  = "The items I'm looking for are 1,2,3 ... phew, too many to\r\nmention. Anyhow, if you gather up 100 of the same items,\r\nthen i may trade it with something similiar. What? You may\r\nnot know this, but i keep my end of the promise, so you\r\nneed not worry. Now, shall we trade?\r\n";
    for (var x = 0; x< a.length; x++){
        result += " #L" + x + "##v" + a[x] + "##t" + a[x] + "##l\r\n";
    }
    return result;
}
function checkQuantity(itemId){
    var itemCount = 0;
    var iter = cm.getChar().getInventory(MapleInventoryType.ETC).listById(itemId).iterator();
    while (iter.hasNext()) {
        itemCount += iter.next().getQuantity();
    }
    return itemCount;
}