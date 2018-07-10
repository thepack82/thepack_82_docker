/*
@	Author : Raz
@	NPC = Blue Balloon
@	Map = Hidden-Street <Stage 8>
@	NPC MapId = 922010800
@	Function = LPQ - 8th Stage
 */
/*
Drop pass on box -> Spawn Alishar
Alishar Drops Key of Dimension
Pass of Dimension - 4001022
Key of Dimension - 4001023
 */

importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.server.life);
importPackage(java.awt);
importPackage(net.sf.odinms.server.pq);
importPackage(net.sf.odinms.client);


var status = 0;
/*
[1] -245 | -223 ||| -190 | -223
[2] -179 | -223 ||| -121 | -223
[3] -245 | -184 ||| -189 | -184
[4] -177 | -184 ||| -121 | -184
[5] -112 | -184 ||| -56  | -184
[6] -244 | -145 ||| -189 | -145
[7] -174 | -145 ||| -126 | -145
[8] -107 | -145 ||| -57  | -145
[9] -39  | -145 ||| 13   | -145
 */
var stage8rects = Array(Rectangle(-245,-223,55,5),//1
Rectangle(-179,-223,58,5),//2
Rectangle(-245,-184,56,5),//3
Rectangle(-177,-184,56,5),//4
Rectangle(-112,-184,56,5),//5
Rectangle(-244,-145,55,5),//6
Rectangle(-174,-145,48,5),//7
Rectangle(-107,-145,50,5),//8
Rectangle(-41,-145,52,5));//9

var stage8combos = 
    Array(Array(0,0,0,1,1,1),//126 total
Array(1,1,1,1,1,0,0,0,0),
Array(1,1,1,1,0,1,0,0,0),
Array(1,1,1,1,0,0,1,0,0),
Array(1,1,1,1,0,0,0,1,0),
Array(1,1,1,1,0,0,0,0,1),
Array(1,1,1,0,1,1,0,0,0),
Array(1,1,1,0,1,0,1,0,0),
Array(1,1,1,0,1,0,0,1,0),
Array(1,1,1,0,1,0,0,0,1),
Array(1,1,1,0,0,1,1,0,0),
Array(1,1,1,0,0,1,0,1,0),
Array(1,1,1,0,0,1,0,0,1),
Array(1,1,1,0,0,0,1,1,0),
Array(1,1,1,0,0,0,1,0,1),
Array(1,1,1,0,0,0,0,1,1),
Array(1,1,0,1,1,1,0,0,0),
Array(1,1,0,1,1,0,1,0,0),
Array(1,1,0,1,1,0,0,1,0),
Array(1,1,0,1,1,0,0,0,1),
Array(1,1,0,1,0,1,1,0,0),
Array(1,1,0,1,0,1,0,1,0),
Array(1,1,0,1,0,1,0,0,1),
Array(1,1,0,1,0,0,1,1,0),
Array(1,1,0,1,0,0,1,0,1),
Array(1,1,0,1,0,0,0,1,1),
Array(1,1,0,0,1,1,1,0,0),
Array(1,1,0,0,1,1,0,1,0),
Array(1,1,0,0,1,1,0,0,1),
Array(1,1,0,0,1,0,1,1,0),
Array(1,1,0,0,1,0,1,0,1),
Array(1,1,0,0,1,0,0,1,1),
Array(1,1,0,0,0,1,1,1,0),
Array(1,1,0,0,0,1,1,0,1),
Array(1,1,0,0,0,1,0,1,1),
Array(1,1,0,0,0,0,1,1,1),
Array(1,0,1,1,1,1,0,0,0),
Array(1,0,1,1,1,0,1,0,0),
Array(1,0,1,1,1,0,0,1,0),
Array(1,0,1,1,1,0,0,0,1),
Array(1,0,1,1,0,1,1,0,0),
Array(1,0,1,1,0,1,0,1,0),
Array(1,0,1,1,0,1,0,0,1),
Array(1,0,1,1,0,0,1,1,0),
Array(1,0,1,1,0,0,1,0,1),
Array(1,0,1,1,0,0,0,1,1),
Array(1,0,1,0,1,1,1,0,0),
Array(1,0,1,0,1,1,0,1,0),
Array(1,0,1,0,1,1,0,0,1),
Array(1,0,1,0,1,0,1,1,0),
Array(1,0,1,0,1,0,1,0,1),
Array(1,0,1,0,1,0,0,1,1),
Array(1,0,1,0,0,1,1,1,0),
Array(1,0,1,0,0,1,1,0,1),
Array(1,0,1,0,0,1,0,1,1),
Array(1,0,1,0,0,0,1,1,1),
Array(1,0,0,1,1,1,1,0,0),
Array(1,0,0,1,1,1,0,1,0),
Array(1,0,0,1,1,1,0,0,1),
Array(1,0,0,1,1,0,1,1,0),
Array(1,0,0,1,1,0,1,0,1),
Array(1,0,0,1,1,0,0,1,1),
Array(1,0,0,1,0,1,1,1,0),
Array(1,0,0,1,0,1,1,0,1),
Array(1,0,0,1,0,1,0,1,1),
Array(1,0,0,1,0,0,1,1,1),
Array(1,0,0,0,1,1,1,1,0),
Array(1,0,0,0,1,1,1,0,1),
Array(1,0,0,0,1,1,0,1,1),
Array(1,0,0,0,1,0,1,1,1),
Array(1,0,0,0,0,1,1,1,1),//ALL ONES DONE - 70
Array(0,1,1,1,1,1,0,0,0),
Array(0,1,1,1,1,0,0,1,0),
Array(0,1,1,1,1,0,0,0,1),
Array(0,1,1,1,0,1,1,0,0),
Array(0,1,1,1,0,1,0,1,0),
Array(0,1,1,1,0,1,0,0,1),
Array(0,1,1,1,0,0,1,1,0),
Array(0,1,1,1,0,0,1,0,1),
Array(0,1,1,1,0,0,0,1,1),
Array(0,1,1,0,1,1,1,0,0),
Array(0,1,1,0,1,1,0,1,0),
Array(0,1,1,0,1,1,0,0,1),
Array(0,1,1,0,1,0,1,1,0),
Array(0,1,1,0,1,0,1,0,1),
Array(0,1,1,0,1,0,0,1,1),
Array(0,1,1,0,0,1,1,1,0),
Array(0,1,1,0,0,1,1,0,1),
Array(0,1,1,0,0,1,0,1,1),
Array(0,1,1,0,0,0,1,1,1),
Array(0,1,0,1,1,1,1,0,0),
Array(0,1,0,1,1,1,0,1,0),
Array(0,1,0,1,1,1,0,0,1),
Array(0,1,0,1,1,0,1,1,0),
Array(0,1,0,1,1,0,1,0,1),
Array(0,1,0,1,1,0,0,1,1),
Array(0,1,0,1,0,1,1,1,0),
Array(0,1,0,1,0,1,1,0,1),
Array(0,1,0,1,0,1,0,1,1),
Array(0,1,0,1,0,0,1,1,1),
Array(0,1,0,0,1,1,1,1,0),
Array(0,1,0,0,1,1,1,0,1),
Array(0,1,0,0,1,1,0,1,1),
Array(0,1,0,0,1,0,1,1,1),
Array(0,1,0,0,0,1,1,1,1),
Array(0,0,1,1,1,1,1,0,0),//ALL TWOS DONE - 34
Array(0,0,1,1,1,1,0,1,0),
Array(0,0,1,1,1,1,0,0,1),
Array(0,0,1,1,1,0,1,1,0),
Array(0,0,1,1,1,0,1,0,1),
Array(0,0,1,1,1,0,0,1,1),
Array(0,0,1,1,0,0,1,1,1),
Array(0,0,1,1,0,1,1,1,0),
Array(0,0,1,1,0,1,1,0,1),
Array(0,0,1,1,0,1,0,1,1),
Array(0,0,1,1,0,0,1,1,1),
Array(0,0,1,0,1,1,1,1,0),
Array(0,0,1,0,1,1,1,0,1),
Array(0,0,1,0,1,1,0,1,1),
Array(0,0,1,0,1,0,1,1,1),
Array(0,0,1,0,0,1,1,1,1),
Array(0,0,0,1,1,1,1,1,0),//ALL THREES DONE - 16
Array(0,0,0,1,1,1,1,0,1),
Array(0,0,0,1,1,1,0,1,1),
Array(0,0,0,1,1,0,1,1,1),
Array(0,0,0,1,0,1,1,1,1),
Array(0,0,0,0,1,1,1,1,1)//ALL FOURS DONE - 5
);

var curMap;

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

        curMap = 8;
        var debug = false;
        var nthtext = "8th";
        var nthobj = "boxes";
        var nthverb = "stand";
        var nthpos = "stand too close to the edges";
        var curcombo = stage8combos;
        var currect = stage8rects;
        var objset = [0,0,0,0,0,0,0,0,0];
		

        if (isLeader()) { // leader
            if (status == 0) {
                // check for preamble
                var eim = cm.getPlayer().getEventInstance();
                party = eim.getPlayers();
                preamble = eim.getProperty("leader" + nthtext + "preamble");
                if (preamble == null) {
                    cm.sendNext("Hi. Welcome to the " + nthtext + " stage. Next to me, you'll see a number of " + nthobj + ". Out of these " + nthobj + ", #b5 are connected to the portal that sends you to the next stage#k. All you need to do is have #b5 party members find the correct " + nthobj + " and " + nthverb + " on them.#k\r\nBUT, it doesn't count as an answer if you " + nthpos + "; please be near the middle of the " + nthobj + " to be counted as a correct answer. Also, only 5 members of your party are allowed on the " + nthobj + ". Once they are " + nthverb + "ing on them, the leader of the party must #bdouble-click me to check and see if the answer's correct or not#k. Now, find the right " + nthobj + " to " + nthverb + " on!");
                    eim.setProperty("leader" + nthtext + "preamble","done");
                    var sequenceNum = Math.floor(Math.random() * curcombo.length);
                    eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
                    cm.dispose();
                }else{
                    // otherwise
                    // check for stage completed
                    var complete = eim.getProperty(curMap.toString() + "stageclear");
                    if (complete != null) {	
                        var mapClear = curMap.toString() + "stageclear";
                        eim.setProperty(mapClear,"true"); // Just to be sure
                        cm.sendNext("Please hurry on to the next stage, the portal opened!");
                        cm.dispose();
                    }else{//Check Ropes 
                        // check for people on ropes(objset)
                        var totplayers = 0;
                        for (i = 0; i < objset.length; i++) {
                            for (j = 0; j < party.size(); j++) {
                                var present = currect[i].contains(party.get(j).getPosition());
                                if (present) {
                                    objset[i] = objset[i] + 1;
                                    totplayers = totplayers + 1;
                                }
                            }
                        }
                        // compare to correct
                        // first, are there 5 players on the objset?
                        if (totplayers == 5 || debug) {
                            var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
                            // debug
                            // combo = curtestcombo;
                            var testcombo = true;
                            for (i = 0; i < objset.length; i++) {
                                if (combo[i] != objset[i])
                                    testcombo = false;
                            }
                            if (testcombo || debug) {
                                // do clear
                                clear(curMap,eim,cm);
                                var exp = (5040);
                                cm.givePartyExp(exp, party);
                                cm.dispose();
                            }else{ // wrong
                                // do wrong
                                failstage(eim,cm);
                                cm.dispose();
                            }
                        }else{
                        	            
                            var outstring = "Objects contain:"
                            for (i = 0; i < objset.length; i++) {
                                outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
                            }
                            //DEBUGING ANSWER
                            if(cm.getPlayer().isGM()){
                                var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
                                outstring += "\r\n#b" + combo + "#k";
                                clear(curMap,eim,cm);
                            }
                            //END ANSWER
                            cm.sendNext(outstring); //
	
                            cm.dispose();
                        }
                    }
                }
                cm.dispose(); // just in case.
            }
        }else{ // not leader
            var eim = cm.getPlayer().getEventInstance();
            var complete = eim.getProperty(curMap.toString() + "stageclear");
            if (complete != null) {
                cm.sendNext("Please hurry on to the next stage, the portal opened!");
                cm.dispose();
            }else{
                cm.sendNext("Please have the party leader talk to me.");
                cm.dispose();
            }
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


function clear(stage, eim, cm) {
    eim.setProperty(stage.toString() + "stageclear","true");
    cm.clear();
    cm.gate();
    cm.givePartyExp(7200, eim.getPlayers());
}

function failstage(eim, cm) {
    cm.wrong();
}