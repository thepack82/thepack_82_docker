var status = 0;

importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server.maps.pq);

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == -1) {
        cm.dispose();
    }
    if (mode == 0) {
        cm.dispose();
        return;
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendOk("Congratulations! You have beaten the Ludibrium Maze Party Quest!");
        } else if (status == 1) {
            var eim = cm.getPlayer().getEventInstance();
            if (eim != null) {
                var rewards = MaplePQRewards.LMPQrewards;
                MapleReward.giveReward(rewards, cm.getPlayer());
                eim.unregisterPlayer(cm.getPlayer());
            }
            cm.warp(220000000, 0);
            cm.dispose();
        }
    }
}