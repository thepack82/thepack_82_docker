/*
New gachapon format by dillusion @ SgPalMs
Perion Gachapon
 */
var status = 0;

//Configurations From Here Onwards.
//Junk items id
var junk = new Array(2060001, 2061001, 2000005, 4020004, 4010000, 4020003, 4010002, 2000002);

//Scrolls items id
var scrolls = new Array(2040805, 2044702, 2043302, 2041020, 2040502, 2043102, 2044302);

//Eq items id
var eq = new Array(1050018, 1332026, 1032032, 1032032, 1452004, 1040070, 1041066, 1040073, 1041068, 1041008, 1041063, 1002137, 1061080, 1002064, 1002145, 1382007, 1040019, 1002073, 1040035, 1040048, 1332008, 1060043, 1040058, 1472007, 1041044, 1060032, 1002056, 1312001, 1050005, 1060028, 1040012, 1061015, 1002086, 1002055, 1082036, 1002011, 1061087, 1002056, 1002058, 1412004, 1041028);

//Unique items id. Eg. BWG
var unique = new Array(1082150, 1082148, 1082146, 1082145, 2340000, 1302021, 1432013, 1002359, 1002393);

//Random Factory lol..
var junkc = Math.floor(Math.random()*junk.length);
var scrollsc = Math.floor(Math.random()*scrolls.length);
var eqc = Math.floor(Math.random()*eq.length);
var uniquec = Math.floor(Math.random()*unique.length);
var itemamount = Math.floor(Math.random()*50+1);

importPackage(net.sf.odinms.client);

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
                case 19:
                    cm.gainItem(junk[junkc], itemamount);
                    break;
                case 6:
                case 12:
                case 14:
                    cm.gainItem(scrolls[scrollsc], 1);
                    break;
                case 3:
                case 7:
                case 4:
                case 18:
                case 13:
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