/* Sabi JQ herb pile #1
*/

importPackage(net.sf.odinms.client);

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1040052, 1040054, 1040130, 1041143, 1042013, 1042022, 1042034, 1042084, 1042098, 1042117, 1702002, 1702015);
    var chances = Array(10, 10, 10, 15, 10, 10, 10, 10, 10, 10, 5, 5);
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
    if (cm.getQuestStatus(2050).equals(MapleQuestStatus.Status.STARTED))
        cm.gainItem(4031020,1);
    cm.gainItem(prizes[choice],1);
    cm.warp(101000000, 0);
    cm.dispose();
}