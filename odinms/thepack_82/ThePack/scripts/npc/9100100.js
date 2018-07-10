/*
New gachapon format by dillusion @ SgPalMs
Hennesy Gachapon
 */

var status = 0;
//Chance of getting item type. Do not change if u do not noe what u are doing = ="

//Configurations From Here Onwards.

//Junk items id
var junk = new Array(4010000, 4020001, 4020008, 4020007, 4020005, 4006000, 2000004, 2000005, 4004001, 2002002, 2002004);
//Junk item random. Change 2 to the size of the array - 1.
var junkc = Math.floor(Math.random()*10);

//Scrolls items id
var scrolls = new Array(2040602, 2040805, 2040002, 2040502, 2040902);
//Scroll item random
var scrollsc = Math.floor(Math.random()*4);

//Eq items id
var eq = new Array(1322021, 1442017, 1050018, 1442004, 1442000, 1422004, 1092008, 1002033, 1041004, 1062001, 1002012, 1452004, 1040024, 1040034, 1002162, 1002138, 1061058,  1040008, 1002164, 1061078, 1452003, 1462001, 1002165, 1082108, 1041008, 1040022, 1041032, 1040007, 1040074, 1002119, 1061059, 1452001, 1082069, 1462000, 1041065, 1040011,  1041061, 1002037, 1002034, 1060014, 1061036, 1051026, 1372004, 1061034, 1061035, 1002016, 1372006, 1051005, 1382004, 1002153, 1372001, 1040044, 1002172, 1002175, 1002173,  1041057, 1041060, 1002184, 1002150, 1040061, 1061041, 1332012, 1040060, 1472008, 1092014, 1002055, 1442009, 1302004, 1061017, 1442006, 1322000, 1002099, 1002058, 1060011,  1050006, 1002009, 1302009, 1060074);
//Eq items random
var eqc = Math.floor(Math.random()*eq.length);

//Unique items id. Eg. BWG
var unique = new Array(1082149, 1082147, 1082150, 1102041, 1002586, 1002587, 1102042, 1082145, 1082146, 2340000, 1102041, 1002586, 1002587, 1082149, 1002587, 1102041);
//Unique items random
var uniquec = Math.floor(Math.random()*15);

//itemamount. Amount of items u get for junks.
var itemamount = Math.floor(Math.random()*50+1);

importPackage(net.sf.rise.client);

function start() {
    status = -1;
    action(1, 0, 0);
}


function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("See you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendNext("I am gachapon...");
        }
        else if (status == 1) {
            if (cm.haveItem(5220000)) {
                cm.sendOk("I see you have a #bGachapon Ticket#k, Do you wish to use it?");
            }
            else if (!cm.haveItem(5220000)) {
                cm.sendOk("You dont have any #bGachapon Ticket#k.");
                cm.dispose();
            }
        }
        else if (status == 2) {
            cm.gainItem(5220000, -1);
            //Chance of getting item type. Do not change if u do not noe what u are doing = ="
            var type = Math.floor(Math.random()*22+1);
            switch(type) {
                case 1:
                case 2:
                case 5:
                case 8:
                case 9:
                case 10:
                case 13:
                case 19:
                    cm.gainItem(junk[junkc], itemamount);
                    break;
                case 6:
                case 7:
                case 12:
                case 14:
                case 18:
                    cm.gainItem(scrolls[scrollsc], 1);
                    break;
                case 3:
                case 4:
                case 15:
                case 16:
                case 17:
                case 20:
                    cm.gainItem(eq[eqc], 1);
                    break;
                case 11:
                    cm.gainItem(unique[uniquec], 1);
                    break;
                default:
                    cm.gainItem(junk[junkc], itemamount);
            }
            cm.dispose();
        }
    }
}