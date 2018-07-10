importPackage(net.sf.odinms.server.maps);

/*
Stage 1: Gatekeeper door - Guild Quest

@Author Lerk
*/

function enter(pi) {
        //if (pi.getPlayer().getMap().getReactorByName("statuegate") == null) {
        if (pi.getPlayer().getMap().getReactorByName("statuegate").getState() == 1) {
                pi.warp(990000301);
                return true;
        }
        else {
                pi.playerMessage("The gate is closed.");
                return false;
        }
}