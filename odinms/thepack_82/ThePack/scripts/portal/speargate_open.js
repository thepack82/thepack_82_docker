importPackage(net.sf.odinms.server.maps);

/*
Stage 2: Exit Door - Guild Quest

@Author Lerk
*/

function enter(pi) {
        if (pi.getPlayer().getMap().getReactorByName("speargate").getState() == 4) {
                pi.warp(990000401);
                return true;
        }
        else {
                pi.playerMessage("This way forward is not open yet.");
                return false;
        }
}