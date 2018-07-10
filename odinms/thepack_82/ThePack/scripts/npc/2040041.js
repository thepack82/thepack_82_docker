/*
@	Author : Raz
@
@	NPC = Aqua Balloon
@	Map = Hidden-Street <Stage 6>
@	NPC MapId = 922010600
@	Function = LPQ - 6th Stage
@
*/

var status = 0;

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
		
                 if (status == 0) {
		cm.sendOk("Im #b#p" + cm.getNpc() + "# #k Heres your tip: find the right combination of teleport boxes to get you to the top. Oh yeah, #bBox-1#k is the first one, hope that helps!");	
		}else if (status == 1) {
		cm.sendOk("Good luck getting all the way up there. <3");		
	 	}else if (status == 2) {
		if(cm.getChar().isGM()){//Show Combo to GM
		cm.sendOk("#b133, 221, 333, 123, 111#k OR TYPE #b!port 46#k");
		}
		cm.dispose();
		}            
          }
     }
