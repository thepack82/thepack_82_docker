/*
Tommy - 1012113.js
Warps out of HPQ
 @Author Jvlaple
*/

 
var status = 0;

importPackage(net.sf.odinms.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1)
		cm.dispose();
	else {
		if (status >= 0 && mode == 0)
			cm.dispose();
		if (mode == 1)
			status++;
		else
			status--;
		if(cm.getChar().getMapId()==910010300){
			if (status==0) {
				cm.sendNext("Tough Luck eh? Try again later!");				
			}else if (status == 1){
				cm.warp(100000200);
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 910010100) {
			var eim = cm.getPlayer().getEventInstance();
			if (status == 0) {
				cm.sendYesNo("Would you like to go to #rPig Town#k? It is a town where Pigs are everywhere, you might find some valuable items there!");
			} else if (status == 1) {
				cm.mapMessage("You have been warped to Pig Town.");
				var em = cm.getEventManager("PigTown");
				if (em == null) {
					cm.sendOk("#e#bEvent Instance Not Found o.O So Please Fuck Off...#n#k");
					cm.dispose();
				}
				else {
					em.startInstance(cm.getParty(),cm.getChar().getMap());
					party = cm.getChar().getEventInstance().getPlayers();
				}
				cm.dispose();
			}
		}
	}
}	