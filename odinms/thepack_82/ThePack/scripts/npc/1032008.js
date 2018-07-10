/**
Cherry (Ellinia Boat Loader)
**/

var status = 0;
var bm;
importPackage(net.sf.odinms.client);

function start() {
    action(1, 0, 0);
    bm = cm.getEventManager("Boats");
}

function action(mode, type, selection) {
    if (status == 0) {
        cm.sendYesNo("Do you wish to board the boat?");
        status++;
    } else {
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
            cm.dispose();
        } else {
            if ((status == 1) && (checkQuantity(4031045) == 0)){
                cm.sendOk("You do not have a ticket to get to Orbis");
                cm.dispose();
                return;
            }
            if ((status == 1) && (bm.getProperty("canBoard").equals("true"))) {
                cm.gainItem(4031045,-1);// take ticket
                cm.warp(101000301, 0);// before takeoff
                cm.dispose();
            }
            if ((status == 1) && (bm.getProperty("canBoard").equals("false")) && (bm.getProperty("boatDocked").equals("true"))){
                cm.sendOk("We are just cleaning the boat from the last voyage.\r\nBoarding starts 5 minutes before departure.\r\nTry again shortly.");
                cm.dispose();
            }
            if ((status == 1) && (bm.getProperty("boatDocked").equals("false"))){
                cm.sendOk("Sorry the boat has not docked yet.\r\nTry again shortly.");
                cm.dispose();
            }
        }
    }
}
function checkQuantity(itemId){
    var itemCount = 0;
    var iter = cm.getChar().getInventory(MapleInventoryType.ETC).listById(itemId).iterator();
    while (iter.hasNext()) {
        itemCount += iter.next().getQuantity();
    }
    return itemCount;
}