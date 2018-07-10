/* Mr. Goldstein
	located in Lith Harbour (104000000)
	Buddy List Admin
 */

var status = 0;
var newcapacity = cm.getBuddyCapacity() + 5;
var price= 100000;

function start() {
    cm.sendNext("Hello, I can update your Buddy List Capacity.");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status>=2) {
            cm.sendOk("If you want to increase your Buddy List Capacity in the future, feel free to come back.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (cm.getBuddyCapacity() == 100) {
                cm.sendOk("You already have the maximum capacity of 100, you cannot upgrade your Buddy List any further");
                cm.dispose();
            }
            else 
                cm.sendYesNo("Would you like to update your Buddy List Capacity to #b" + newcapacity + "#k for #b" + price + " #kmesos?");
        } else if (status == 2) {
            if (cm.getMeso() < price) {
                cm.sendOk("Sorry but it doesn't look like you have enough mesos!")
                cm.dispose();
            }
            else {
                cm.updateBuddyCapacity(newcapacity);
                cm.gainMeso(-price);
                cm.sendOk("Enjoy your new buddy capacity.");
                cm.dispose();
            }
        } 
    }
}