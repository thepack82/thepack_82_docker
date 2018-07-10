var status = 0;
var party;
var preamble;
var mobcount;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();//ExitChat
    else if (mode == 0)
        cm.dispose();//No
    else{		    //Regular Talk
        if (mode == 1)
            status++;
        else
            status--;
        var eim = cm.getPlayer().getEventInstance();
        var nthtext = "4th";
        if (status == 0) {
            party = eim.getPlayers();
            preamble = eim.getProperty("leader" + nthtext + "preamble");
            mobcount = eim.getProperty("leader" + nthtext + "mobcount");
            if (preamble == null) {
                cm.sendOk("Hi. Welcome to the " + nthtext + " stage. This is the elemental test. Shall we get started?");
                status = 9;
            }else{
                if(!isLeader()){
                    if(mobcount == null){
                        cm.sendOk("Please tell your #bParty-Leader#k to come talk to me");
                        cm.dispose();
                    }else{
                        cm.warp(910000019,0);
                        cm.dispose();
                    }
                }
                if(mobcount == null)
                    cm.sendYesNo("You're finished?!");

            }
        }else if (status == 1){
            if (cm.mapMobCount() == 0) {
                cm.sendOk("Good job! You've killed 'em!");
            }else{
                cm.sendOk("What are you talking about? Kill those creatures!!");
                cm.dispose();
            }
        }else if (status == 2){
            cm.sendOk("You may continue to the next stage!");
        }else if (status == 3){
            cm.clear();
            eim.setProperty("leader" + nthtext + "mobcount","done");
            var map = eim.getMapInstance(910000019);
            var members = eim.getPlayers();
            cm.warpMembers(map, members);
            cm.givePartyExp(1500, eim.getPlayers());
            cm.dispose();
        }else if (status == 10){
            eim.setProperty("leader" + nthtext + "preamble","done");
            cm.summonMobAtPosition(9300089,500000,50000,5,-270,1185);
            cm.summonMobAtPosition(9300090,500000,50000,5,335,1185);
            cm.dispose();
        }
    }
}

function isLeader(){
    if(cm.getParty() == null)
        return false;
    return cm.isLeader();
}