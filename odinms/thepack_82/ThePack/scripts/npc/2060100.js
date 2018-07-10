//carta
function start(){
    if(cm.getChar().getQuest(net.sf.odinms.server.quest.MapleQuest.getInstance(6301)).getStatus().equals(net.sf.odinms.client.MapleQuestStatus.Status.STARTED)) {
        if (cm.haveItem(4000175))
            cm.warp(923000000)
        else
            cm.sendOk("In order to open the crack of dimension you will have to posess one piece of Miniature Pianus. Those could be gained by defeating a Pianus.");
    } else
        cm.sendOk("I'm #bCarta the sea-witch.#k Don't fool around with me, as I'm known for my habit of turning people into worms.");
    cm.dispose();
}