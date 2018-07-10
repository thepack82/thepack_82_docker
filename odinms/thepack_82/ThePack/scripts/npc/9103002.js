/*
 *@Author RMZero213
 * Ludibrium Maze Party Quest
 * Do not release anywhere other than RaGEZONE. Give credit if used.
 */

var status = 0;

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
            var eim = cm.getPlayer().getEventInstance();
            if (eim != null) {
                eim.unregisterPlayer(cm.getPlayer());
            }
            cm.warp(220000000, 0);
            cm.dispose();
        }
    }
}