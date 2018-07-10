/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Lakelis - Victoria Road: Kerning City (103000000)
-- By ---------------------------------------------------------------------------------------------
	Stereo
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Stereo
---------------------------------------------------------------------------------------------------
**/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            // Lakelis has no preamble, directly checks if you're in a party
            if (cm.getParty() == null) { // No Party
                cm.sendOk("How about you and your party members collectively beating a quest? Here you'll find obstacles and problems where you won't be able to beat it unless with great teamwork. If you want to try it, please tell the #bleader of your party#k to talk to me.");
                cm.dispose();
            } else if (!cm.isLeader()) { // Not Party Leader
                cm.sendOk("If you want to try the quest, please tell the #bleader of your party#k to talk to me.");
                cm.dispose();
            } else {
                // Check if all party members are within Levels 21-30
                var party = cm.getParty().getMembers();
                var mapId = cm.getChar().getMapId();
                var next = true;
                var levelValid = 0;
                var inMap = 0;
                var it = party.iterator();
                while (it.hasNext()) {
                    var cPlayer = it.next();
                    if ((cPlayer.getLevel() >= 0) && (cPlayer.getLevel() <= 255)) {
                        levelValid += 1;
                    } else {
                        next = false;
                    }
                    if (cPlayer.getMapid() == mapId) {
                        inMap += 1;
                    }
                }
                if (party.size() < 0 || party.size() > 6 || inMap < 4)
                    next = false;
                if (next) {
                    var em = cm.getEventManager("KerningPQ");
                    if (em == null) {
                        cm.sendOk("This PQ is not currently available.");
                    } else {
                        //                        if(em.getProperty("entryPossible") == "true"){
                        //                            // Begin the PQ.
                        em.startInstance(cm.getParty(),cm.getChar().getMap());
                        // Remove Passes and Coupons
                        party = cm.getChar().getEventInstance().getPlayers();
                        cm.removeFromParty(4001008, party);
                        cm.removeFromParty(4001007, party);
                        //                            em.setProperty("entryPossible", "false");
                        //                        }else{
                        //                            cm.sendNext("It looks like there is already another party inside the Party Quest. Why don't you come back later?");
                        //                        }
                        cm.dispose();
                    }
                } else {
                    cm.sendNext("Your party is not a party of four. Please come back when you have four party members.");
                    cm.dispose();
                }
            }
        }
    }
}