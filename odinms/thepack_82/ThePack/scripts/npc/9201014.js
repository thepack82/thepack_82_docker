/**
	Pila Present
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
---------------------------------------------------------------------------------------------------
**/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) { 
    if (mode == -1 || mode == 0) {
        cm.sendOk("Goodbye then"); 
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
		
    if (status == 0) {
        var msg = "Hello I exchange Onyx Chest for Bride and Groom and the Onyx Chest for prizes!";
        var choice1 = new Array("I have an Onyx Chest for Bride and Groom", "I have an Onyx Chest");
        for (var i = 0; i < choice1.length; i++) {
            msg += "\r\n#L" + i + "#" + choice1[i] + "#l";
        }
        cm.sendSimple(msg);
    } else if (status == 1) {
        if (selection == 0) {
            if (cm.haveItem(4031424)) {
                var rand = Math.floor(Math.random() * 4);
                if (rand == 0)
                    cm.gainItem(2022179,10);
                else if (rand == 1)
                    cm.gainItem(2022282,10);
                else if (rand == 2)
                    cm.gainItem(2210005,5);
                else if (rand == 3)
                    cm.gainItem(2210003,5);
                cm.gainItem(4031424,-1);
            } else {
                cm.sendOk("You don't have an Onyx Chest for Bride and Groom.");
                cm.dispose();
            }
        } else if (selection == 1) {
            if (cm.haveItem(4031423)) {
                cm.sendSimple("You may choose your prize.\r\n#L0#Triangular Sushi#l\r\n#L1#50 power elixers#l\r\n#L2#10 Swiss Cheese#l\r\n#L3#3 Onyx Apples#l");
            } else {
                cm.sendOk("You don't have an Onyx Chest");
                cm.dispose();
            }
        }
    } else if (status == 2) {
        if (selection == 0)
            cm.gainItem(2022011,10);
        else if (selection == 1)
            cm.gainItem(2000005,50);
        else if (selection == 2)
            cm.gainItem(2022273,10);
        else if (selection == 3)
            cm.gainItem(2022179,3);
        cm.gainItem(4031423,-1);
        cm.dispose();
    }
} 
