/*
@	Author : Raz
@
@	NPC = Lime Balloon
@	Map = Hidden-Street <Stage 4>
@	NPC MapId = 922010400
@	Function = LPQ - 4th Stage
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
		var eim = cm.getChar().getEventInstance(); 
		var nthtext = "4th";


                 if (status == 0) {
		 party = eim.getPlayers();
                 preamble = eim.getProperty("leader" + nthtext + "preamble");
		 gaveItems = eim.getProperty("leader" + nthtext + "gaveItems");
                        if (preamble == null) {
                                cm.sendNext("Hi. Welcome to the " + nthtext + " stage.");
                                eim.setProperty("leader" + nthtext + "preamble","done");
                                cm.dispose();
                        }else{
		 if(!isLeader()){
		 if(gaveItems == null){
		 cm.sendOk("Please tell your #bParty-Leader#k to come talk to me");
		 cm.dispose();
		 }else{
		  cm.sendOk("Hurry, goto the next stage, the portal is open!");
		  cm.dispose();
		 }
		}else{
		if(gaveItems == null){
		if(cm.itemQuantity(4001022) >= 6){
		cm.sendOk("Good job! you have collected all 6 #b#t4001022#'s#k");
		cm.removeAll(4001022);
		}else{
		cm.sendOk("Sorry you don't have all 6 #b#t4001022#'s#k");
		cm.dispose();
		}
		}else{
		cm.sendOk("Hurry, goto the next stage, the portal is open!");
		cm.dispose();
		}
		}}
		}else if (status == 1){
		cm.sendOk("You may continue to the next stage!");
		cm.gate();
		cm.clear();
		cm.givePartyExp(4800, eim.getPlayers());
		eim.setProperty("4stageclear","true");
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
