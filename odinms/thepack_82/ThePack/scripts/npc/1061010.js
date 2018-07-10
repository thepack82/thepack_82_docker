var status = 0;
var summon;
var nthtext = "bonus";

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
        if (eim == null) {
            cm.warp(109050001,0);
            cm.dispose();
        }
        summon = eim.getProperty("leader" + nthtext + "summon");
        if (status == 0) {
            if (summon == null){
		cm.sendSimple("#L0#Get me outta here!#l\r\n#L1#Summon my prize!#l");
            } else {
		cm.sendSimple("#L0#Get me outta here!#l");
            }
        }else if (status == 1) {
            if (selection == 0) {
		if(isLeader()){
                    cm.sendOk("Ok, bye.");
		}else{
                    cm.sendOk("Ask your #bParty-Leader#k to come talk to me.");
                    cm.dispose();
		}
            } else if (selection == 1) {
		if(isLeader()){
                    cm.sendOk("NX and Silver Slimes!");
                    cm.summonMobAtPosition(9400202,5000,1000,30,-634,334);
                    cm.summonMobAtPosition(9400203,5000,5000,30,179,334);
                    eim.setProperty("leader" + nthtext + "summon","done");
                    cm.dispose();
		}else{
                    cm.sendOk("Ask your #bParty-Leader#k to come talk to me.");
                    cm.dispose();
		}
            }
        }else if (status == 2) {
            var map = eim.getMapInstance(109050000);
            var members = eim.getPlayers();
            cm.warpMembers(map, members);
            //STOP TIMER
            //WARP PLAYERS OUT OF BONUS
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
