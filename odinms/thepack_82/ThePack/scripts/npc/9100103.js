/*
New gachapon format by dillusion @ SgPalMs
Kerning Gachapon
 */
var status = 0;

//Configurations From Here Onwards.

//Junk items id
var junk = new Array(4010000, 4010001, 4010004, 4020001, 4020002, 2000005, 2000004, 2002000, 2002003, 4003000, 2030004, 2030006, 2030007);

//Scrolls items id
var scrolls = new Array(2040805, 2044702, 2043302, 2041020);

//Eq items id
var eq = new Array(1002062, 1442016, 1432001, 1060004, 1002096, 1302017, 1102002, 1002020, 1402001, 1040024, 1041034, 1062002, 1040003, 1002164, 1002120, 1002119, 1061061, 1060062, 1452003, 1462001, 1041063, 1061006, 1002168, 1041020, 1061017, 1002119, 1061024, 1002159, 1051039, 1452006, 1041029, 1050023, 1040020, 1051026, 1372002, 1051004, 1082022, 1002073, 1061028, 1472016, 1472006, 1332011, 1060023, 1061031, 1040033, 1041038, 1002130, 1040050, 1041058, 1041057, 1002172, 1002174, 1060025, 1061038, 1060024, 1472004, 1472005, 1472006, 1060038, 1061053, 1061056, 1061033, 1472023, 1472019, 1472011, 1002129, 1002131, 1041079, 1332008, 1040084, 1041076, 1332006, 1472002, 1332012, 1472020, 1092018, 1472008, 1472005, 1082042, 1472001, 1332002, 1041022, 1041024, 1040037, 1060009, 1041085, 1060019, 1061018, 1332088, 1422002, 1002011, 1002058, 1302002, 1002047, 1061017, 1002023, 1302006, 1092006, 1002087, 1040039, 1040040, 1090021, 1051000, 1002009, 1092000, 1412007, 1302002);

//Unique items id. Eg. BWG
var unique = new Array(1302049, 1002394, 1002394, 1002082, 1302021, 1102040, 2340000, 1002082, 1302021, 1102040);

//Random Factory lol..
var junkc = Math.floor(Math.random()*junk.length);
var scrollsc = Math.floor(Math.random()*scrolls.length);
var eqc = Math.floor(Math.random()*eq.length);
var uniquec = Math.floor(Math.random()*unique.length);

//itemamount. Amount of items u get for junks.
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
                case 8:
                case 9:
                case 10:
                case 13:
                case 19:
                    cm.gainItem(junk[junkc], itemamount);
                    break;
                case 6:
                case 12:
                case 14:
                    cm.gainItem(scrolls[scrollsc], 1);
                    break;
                case 3:
                case 4:
                case 7:
                case 5:
                case 18:
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