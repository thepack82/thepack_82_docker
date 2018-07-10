var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("All right, see you next time.");
			cm.dispose();
            return;
		}
		status++;
		if (status == 0) {
			cm.sendNext("I am Ria. For a small fee of #b1000000 meso#k I can send you to the #rField of Judgement#k.");
		} else if (status == 1) {
			cm.sendYesNo("Do you wish to enter #rField of Judgement#k now?");
		} else if (status == 2) {
			var em = cm.getEventManager("lolcastle");
			if (em == null || !em.getProperty("entryPossible").equals("true")) {
				cm.sendOk("Sorry, but #rField of Judgement#k is currently closed.");
                cm.dispose();
			} else if (cm.getMeso() < 1000000) {
				cm.sendOk("You do not have enough mesos.");
                cm.dispose();
			} else if (cm.getChar().getLevel() < 21) {
				cm.sendOk("You have to be at least level 21 to enter #rField of Judgement.#k");
                cm.dispose();
			} else if (cm.getChar().getLevel() >= 21 && cm.getChar().getLevel() < 31) {
				em.getInstance("lolcastle1").registerPlayer(cm.getChar());
			} else if (cm.getChar().getLevel() >= 31 && cm.getChar().getLevel() < 51) {
				em.getInstance("lolcastle2").registerPlayer(cm.getChar());
			} else if (cm.getChar().getLevel() >= 51 && cm.getChar().getLevel() < 71) {
				em.getInstance("lolcastle3").registerPlayer(cm.getChar());
			} else if (cm.getChar().getLevel() >= 71 && cm.getChar().getLevel() < 91) {
				em.getInstance("lolcastle4").registerPlayer(cm.getChar());
			} else {
				em.getInstance("lolcastle5").registerPlayer(cm.getChar());
			}
            cm.gainMeso(-1000000);
			cm.dispose();
		}
	}
}
