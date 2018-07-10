/*
@	Author : Raz
@
@	NPC = Green Balloon
@	Map = Hidden-Street <Stage 5>
@	NPC MapId = 922010500
@	Function = LPQ - 5th Stage
@
 */

var status = 0;
var party;
var preamble;
var gaveItems;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

        
    if (mode == -1) {
        cm.dispose();//ExitChat
    }else if (mode == 0){
        cm.dispose();//No
    }else{		    //Regular Talk
        if (mode == 1)
            status++;
        else
            status--;
        var eim = cm.getPlayer().getEventInstance(); 
        var nthtext = "5th";


        if (status == 0) {
            party = eim.getPlayers();
            preamble = eim.getProperty("leader" + nthtext + "preamble");
            gaveItems = eim.getProperty("leader" + nthtext + "gaveItems");
            if (preamble == null) {
                cm.sendNext("Hi. Welcome to the " + nthtext + " stage. This is the 2nd stage, but everyone has to cooperate. There are 6 portals here. One is guarded by undefeatable monsters, and one is very high. I'd like you and your party to go in each one and break the boxes inside. Bring back the drops -- there should be 24.");//not 24!
                eim.setProperty("leader" + nthtext + "preamble","done");
                cm.dispose();
            }else{
                if(!isLeader()){
                    if(gaveItems == null){
                        cm.sendOk("Please tell your #bParty-Leader#k to come talk to me.");
                        cm.dispose();
                    }else{
                        cm.sendOk("Hurry, goto the next stage, the portal is open!");
                        cm.dispose();
                    }
		}else{
                    if(gaveItems == null){
                        if(cm.itemQuantity(4001022) >= 24){
                            cm.sendOk("Good job! you have collected all 24 #b#t4001022#'s#k");
                        }else{
                            cm.sendOk("Sorry you don't have all 24 #b#t4001022#'s#k");
                            cm.dispose();
                        }
                    }else{
                        cm.sendOk("Hurry, goto the next stage, the portal is open!");
                        cm.dispose();
                    }
		}}
        }else if (status == 1){
            cm.sendOk("You may continue to the next stage!");
            cm.removeAll(4001022);
            cm.gate();
            cm.clear();
            cm.givePartyExp(5400, eim.getPlayers());
            eim.setProperty("5stageclear","true");
            eim.setProperty("leader" + nthtext + "gaveItems","done");
            cm.dispose();
        }            
    }
}
     
     
function isLeader(){
    if(cm.getParty() == null){
        return false;
    }else{
        return cm.isLeader();
    }
}