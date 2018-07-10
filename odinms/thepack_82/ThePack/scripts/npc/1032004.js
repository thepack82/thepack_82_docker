/* Shane 1032004
 * By Moogra
*/

function start() {
    cm.sendYesNo("Would you like to return to Ellinia?");
}

function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(101000000,0);
    cm.dispose();
}	