/*
Multi-Purpose NPC
Author: Moogra
 */

var npcid = Array(9000020, 9200000, 2003, 1061008, 9201082, 2050015, 9201091, 9001002, 2040048, 9120024, 1022005);

function start() {
    cm.sendSimple("What do you want me to do for you? \b\r\n#L0#Travel to Towns or Fight Bosses\n\#l\r\n#L1#Job Advance\n\#l\r\n#L2#Reset Stats\n\#l\r\n#L3#Change Music#l\r\n#L4#Buy NX#l\r\n#L5#Exchange Mesos for Items#l\r\n#L6#Get Buffs#l\r\n#L7#Go To PvP#l\r\n#L8#Donation Point NPC#l\r\n#L9#Pirate Shop#l\r\n#L10#Karma Shop#l");
}

function action(m, t, s) {
    cm.openNpc(npcid[s]);
}