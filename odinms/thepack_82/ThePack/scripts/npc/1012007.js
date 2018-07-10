/* Trainer Frod
	Pet Trainer
	located in Pet-Walking Road (100000202)
 */

importPackage(net.sf.odinms.server.maps);

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) 
        cm.dispose();
    else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Alright, see you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            cm.sendNext("Great job! You made it! As a reward, I shall make you and your pet closer! Come back soon, pets need exercise daily.");
        else if (status == 1) {
            cm.gainCloseness(3);
            cm.warp(100000202, 0);
            cm.dispose();
        }
    }
}