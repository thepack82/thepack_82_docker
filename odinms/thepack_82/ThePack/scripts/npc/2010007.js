/* guild creation npc */
var status = 0;
var sel;
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

		if (status == 0)
			cm.sendSimple("What would you like to do?\r\n#b#L0#Create a Guild#l\r\n#L1#Disband your Guild#l\r\n#L2#Increase your Guild's capacity#l#k");
		else if (status == 1) {
			sel = selection;
			if (selection == 0) {
				if (cm.getChar().getGuildId() > 0) {
					cm.sendOk("You may not create a new Guild while you are in one.");
					cm.dispose();
				} else
					cm.sendYesNo("Creating a Guild costs #b" + cm.getChar().guildCost() + " mesos#k, are you sure you want to continue?");
			}
			else if (selection == 1) {
				if (cm.getChar().getGuildId() <= 0 || cm.getChar().getGuildRank() != 1) {
					cm.sendOk("You can only disband a Guild if you are the leader of that Guild.");
					cm.dispose();
				} else
					cm.sendYesNo("Are you sure you want to disband your Guild? You will not be able to recover it afterward and all your GP will be gone.");
			}
			else if (selection == 2) {
				if (cm.getChar().getGuildId() <= 0 || cm.getChar().getGuildRank() != 1) {
					cm.sendOk("You can only increase your Guild's capacity if you are the leader.");
					cm.dispose();
				}
				else
					cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b" + cm.getChar().capacityCost() + " mesos#k, are you sure you want to continue?");
			}
		}
		else if (status == 2) {
			if (sel == 0 && cm.getChar().getGuildId() <= 0) {
				cm.getChar().genericGuildMessage(1);
				cm.dispose();
			} else if (sel == 1 && cm.getChar().getGuildId() > 0 && cm.getChar().getGuildRank() == 1) {
				cm.getChar().disbandGuild();
				cm.dispose();
			} else if (sel == 2 && cm.getChar().getGuildId() > 0 && cm.getChar().getGuildRank() == 1) {
				cm.getChar().increaseGuildCapacity();
				cm.dispose();
			}
		}
	}
}
