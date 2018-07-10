/* Sera
* By Moogra
*/
var equipGains = Array(1442018, 1442039, 1432038, 1402036, 1372032, 1382036, 1452044, 1462039, 1472051, 1332050, 1482023, 1492013);
var cape = 1102041;

function start() {
    cm.sendSimple("#bWelcome to MapleStory. What job do you wish to be?#k \r\n#L0#Beginner#l \r\n\ #L1#Warrior#l \r\n\ #L2#Magician#k#l \r\n\ #L3#Bowman#l \r\n\ #L4#Thief#l \r\n\ #L5#Pirate#l");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        cm.gainRandEquip(1002357);
        for (var i = 0; i < 2; i++)
            cm.gainRandEquip(equipGains[selection*2+i]);
        if (selection==2) cape++;
        cm.gainRandEquip(cape);
        cm.getChar().maxAllSkills();
        cm.resetStats();
        cm.warp(1,0);
        cm.dispose();
    }
}