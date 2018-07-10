/* Sabi JQ herb pile #1
*/

importPackage(net.sf.odinms.client);

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1060041, 1060048, 1060116, 1061113, 1061130, 1061139, 1062009, 1062017, 1062024, 1062056, 1062061, 1702045, 1702114);
    var chances = Array(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 5, 5);
    var totalodds = 0;
    var choice = 0;
    for (var i = 0; i < chances.length; i++) {
        var itemGender = (Math.floor(prizes[i]/1000)%10);
        if ((cm.getChar().getGender() != itemGender) && (itemGender != 2))
            chances[i] = 0;
    }
    for (var i = 0; i < chances.length; i++)
        totalodds += chances[i];
    var randomPick = Math.floor(Math.random()*totalodds)+1;
    for (var i = 0; i < chances.length; i++) {
        randomPick -= chances[i];
        if (randomPick <= 0) {
            choice = i;
            randomPick = totalodds + 100;
        }
    }
    if (cm.getQuestStatus(2051).equals(MapleQuestStatus.Status.STARTED))
        cm.gainItem(4031032,1);
    cm.gainItem(prizes[choice],1);
    cm.warp(101000000, 0);
    cm.dispose();
}