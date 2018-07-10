//Gachaphon

importPackage(net.sf.odinms.client);

var status = 0;
var chance1 = Math.floor(Math.random()*200+1);
var chance2 = Math.floor(Math.random()*50);
var chance3 = (Math.floor(Math.random()*20)+1);
var chance4 = Math.floor(Math.random()*2+1);
var ichance = chance1 + chance2 + chance3 * chance4;
var iamount = Math.floor(Math.random()*50+1);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) 
        cm.dispose();
     else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("See you next time, when you try your luck here~!");
            cm.dispose();
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) 
            cm.sendNext("I am gachapon.\r\nI give things to people if they give me cardboard tickets~!\r\nThe tickets look like this: #v5220000#");
        else if (status == 1) {
            if (cm.haveItem(5220000))
                cm.sendYesNo("I see you have a ticket of mine, do you wish to use it?");
            else if (!cm.haveItem(5220000)) {
                cm.sendOk("You dont have any #bgachapon tickets#k.");
                cm.dispose();
            }
        }
        else if (status == 2) {
            cm.gainItem(5220000, -1);
            if ((ichance >= 1) && (ichance <= 20)) 
                cm.gainItem(2000004, iamount);
            else if ((ichance >= 21) && (ichance <= 40)) 
                cm.gainItem(2020012, iamount);
            else if ((ichance >= 41) && (ichance <= 50)) 
                cm.gainItem(2000005, iamount);
            else if ((ichance >= 51) && (ichance <= 60))
                cm.gainItem(2030007, iamount);
            else if ((ichance >= 61) && (ichance <= 70)) 
                cm.gainItem(2022027, iamount);
            else if (ichance == 77) {
                cm.gainItem(2043702, 1);
            }
            else if (ichance == 78) {
                cm.gainItem(1302022, 1);
            }
            else if (ichance == 79) {
                cm.gainItem(1322021, 1);
            }	
            else if (ichance == 80) {
                cm.gainItem(1322026, 1);
            }	
            else if (ichance == 81) {
                cm.gainItem(1302026, 1);
            }
            else if (ichance == 82) {
                cm.gainItem(1442017, 1);
            }
            else if (ichance == 83) {
                cm.gainItem(1082147, 1);
            }	
            else if (ichance == 84) {
                cm.gainItem(1102043, 1);
            }
            else if (ichance == 85) {
                cm.gainItem(1442016, 1);
            }
            else if (ichance == 86) {
                cm.gainItem(1402012, 1);
            }
            else if (ichance == 87) {
                cm.gainItem(1302027, 1);
            }	
            else if (ichance == 88) {
                cm.gainItem(1322027, 1);
            }
            else if (ichance == 89) {
                cm.gainItem(1322025, 1);
            }
            else if (ichance == 90) {
                cm.gainItem(1312012, 1);
            }
            else if (ichance == 91) {
                cm.gainItem(1062000, 1);
            }
            else if (ichance == 92) {
                cm.gainItem(1332020, 1);
            }
            else if (ichance == 93) {
                cm.gainItem(1302028, 1);
            }
            else if (ichance == 94) {
                cm.gainItem(1372002, 1);
            }
            else if (ichance == 95) {
                cm.gainItem(1002033, 1);
            }
            else if (ichance == 96) {
                cm.gainItem(1092022, 1);
            }
            else if (ichance == 97) {
                cm.gainItem(1302021, 1);
            }
            else if (ichance == 98) {
                cm.gainItem(1102041, 1);
            }
            else if (ichance == 99) {
                cm.gainItem(1102042, 1);
            }
            else if (ichance == 100) {
                cm.gainItem(1322024, 1);
            }
            else if (ichance == 101) {
                cm.gainItem(1082148, 1);
            }
            else if (ichance == 102) {
                cm.gainItem(1002012, 1);
            }
            else if (ichance == 103) {
                cm.gainItem(1322012, 1);
            }
            else if (ichance == 104) {
                cm.gainItem(1322022, 1);
            }
            else if (ichance == 105) {
                cm.gainItem(1002020, 1);
            }
            else if (ichance == 106) {
                cm.gainItem(1302013, 1);
            }
            else if (ichance == 107) {
                cm.gainItem(1082146, 1);
            }
            else if (ichance == 108) {
                cm.gainItem(1442014, 1);
            }
            else if (ichance == 109) {
                cm.gainItem(1002096, 1);
            }
            else if (ichance == 110) {
                cm.gainItem(1302017, 1);
            }
            else if (ichance == 111) {
                cm.gainItem(1442012, 1);
            }
            else if (ichance == 112) {
                cm.gainItem(1322010, 1);
            }
            else if (ichance == 113) {
                cm.gainItem(1442011, 1);
            }
            else if (ichance == 114) {
                cm.gainItem(1442018, 1);
            }
            else if (ichance == 115) {
                cm.gainItem(1092011, 1);
            }
            else if (ichance == 116) {
                cm.gainItem(1092014, 1);
            }
            else if (ichance == 117) {
                cm.gainItem(1302003, 1);
            }
            else if (ichance == 118) {
                cm.gainItem(1432001, 1);
            }
            else if (ichance == 119) {
                cm.gainItem(1312011, 1);
            }
            else if (ichance == 120) {
                cm.gainItem(1002088, 1);
            }
            else if (ichance == 121) {
                cm.gainItem(1041020, 1);
            }
            else if (ichance == 122) {
                cm.gainItem(1322015, 1);
            }
            else if (ichance == 123) {
                cm.gainItem(1442004, 1);
            }
            else if (ichance == 124) {
                cm.gainItem(1422008, 1);
            }
            else if (ichance == 125) {
                cm.gainItem(1302056, 1);
            }
            else if (ichance == 126) {
                cm.gainItem(1432000, 1);
            }
            else if (ichance == 127) {
                cm.gainItem(1382001, 1);
            }
            else if (ichance == 128) {
                cm.gainItem(1041053, 1);
            }
            else if (ichance == 129) {
                cm.gainItem(1060014, 1);
            }
            else if (ichance == 130) {
                cm.gainItem(1050053, 1);
            }
            else if (ichance == 131) {
                cm.gainItem(1051032, 1);
            }
            else if (ichance == 132) {
                cm.gainItem(1050073, 1);
            }
            else if (ichance == 133) {
                cm.gainItem(1061036, 1);
            }
            else if (ichance == 134) {
                cm.gainItem(1002253, 1);
            }
            else if (ichance == 135) {
                cm.gainItem(1002034, 1);
            }
            else if (ichance == 136) {
                cm.gainItem(1051025, 1);
            }
            else if (ichance == 137) {
                cm.gainItem(1050067, 1);
            }
            else if (ichance == 138) {
                cm.gainItem(1051052, 1);
            }
            else if (ichance == 139) {
                cm.gainItem(1002072, 1);
            }
            else if (ichance == 140) {
                cm.gainItem(1002144, 1);
            }
            else if (ichance == 141) { 
                cm.gainItem(1051054, 1);
            }
            else if (ichance == 142) { 
                cm.gainItem(1050069, 1);
            }
            else if (ichance == 143) { 
                cm.gainItem(1372007, 1);
            }
            else if (ichance == 144) { 
                cm.gainItem(1050056, 1);
            }
            else if (ichance == 145) { 
                cm.gainItem(1050074, 1);
            }
            else if (ichance == 146) { 
                cm.gainItem(1002254, 1);
            }
            else if (ichance == 147) {
                cm.gainItem(1002274, 1);
            }
            else if (ichance == 148) { 
                cm.gainItem(1002218, 1);
            }
            else if (ichance == 149) { 
                cm.gainItem(1051055, 1);
            }
            else if (ichance == 150) { 
                cm.gainItem(1382010, 1);
            }
            else if (ichance == 151) { 
                cm.gainItem(1002246, 1);
            }
            else if (ichance == 152) { 
                cm.gainItem(1050039, 1);
            }
            else if (ichance == 153) { 
                cm.gainItem(1382007, 1);
            }
            else if (ichance == 154) { 
                cm.gainItem(1372000, 1);
            }
            else if (ichance == 155) { 
                cm.gainItem(1002013, 1);
            }
            else if (ichance == 156) { 
                cm.gainItem(1050072, 1);
            }
            else if (ichance == 157) { 
                cm.gainItem(1002036, 1);
            }
            else if (ichance == 158) { 
                cm.gainItem(1002243, 1);
            }
            else if (ichance == 159) { 
                cm.gainItem(1372008, 1);
            }
            else if (ichance == 160) { 
                cm.gainItem(1382008, 1);
            }
            else if (ichance == 161) { 
                cm.gainItem(1382011, 1);
            }
            else if (ichance == 162) { 
                cm.gainItem(1092021, 1);
            }
            else if (ichance == 163) { 
                cm.gainItem(1051034, 1);
            }
            else if (ichance == 164) { 
                cm.gainItem(1050047, 1);
            }

            else if (ichance == 165) { 
                cm.gainItem(1040019, 1);
            }
            else if (ichance == 166) { 
                cm.gainItem(1041031, 1);
            }
            else if (ichance == 167) { 
                cm.gainItem(1051033, 1);
            }
            else if (ichance == 168) { 
                cm.gainItem(1002153, 1);
            }
            else if (ichance == 169) { 
                cm.gainItem(1002252, 1);
            }
            else if (ichance == 170) { 
                cm.gainItem(1051024, 1);
            }
            else if (ichance == 171) { 
                cm.gainItem(1002153, 1);
            }
            else if (ichance == 172) { 
                cm.gainItem(1050068, 1);
            }
            else if (ichance == 173) { 
                cm.gainItem(1382003, 1);
            }
            else if (ichance == 174) { 
                cm.gainItem(1382006, 1);
            }
            else if (ichance == 175) { 
                cm.gainItem(1050055, 1);
            }
            else if (ichance == 176) { 
                cm.gainItem(1051031, 1);
            }
            else if (ichance == 177) { 
                cm.gainItem(1050025, 1);
            }
            else if (ichance == 178) { 
                cm.gainItem(1002155, 1);
            }
            else if (ichance == 179) { 
                cm.gainItem(1002245, 1);
            }
            else if (ichance == 180) { 
                cm.gainItem(13720013, 1);
            }
            else if (ichance == 181) { 
                cm.gainItem(1452004, 1);
            }
            else if (ichance == 182) { 
                cm.gainItem(1452023, 1);
            }
            else if (ichance == 183) { 
                cm.gainItem(1060057, 1);
            }
            else if (ichance == 184) { 
                cm.gainItem(1040071, 1);
            }
            else if (ichance == 185) { 
                cm.gainItem(1002137, 1);
            }
            else if (ichance == 186) { 
                cm.gainItem(1462009, 1);
            }
            else if (ichance == 187) { 
                cm.gainItem(1452017, 1);
            }
            else if (ichance == 188) { 
                cm.gainItem(1040025, 1);
            }
            else if (ichance == 189) { 
                cm.gainItem(1041027, 1);
            }
            else if (ichance == 190) { 
                cm.gainItem(1452005, 1);
            }
            else if (ichance == 191) { 
                cm.gainItem(1452007, 1);
            }
            else if (ichance == 192) { 
                cm.gainItem(1061057, 1);
            }
            else if (ichance == 193) { 
                cm.gainItem(1472006, 1);
            }
            else if (ichance == 194) { 
                cm.gainItem(1472019, 1);
            }
            else if (ichance == 195) { 
                cm.gainItem(1060084, 1);
            }
            else if (ichance == 196) { 
                cm.gainItem(1472028, 1);
            }
            else if (ichance == 197) { 
                cm.gainItem(1002179, 1);
            }
            else if (ichance == 198) { 
                cm.gainItem(1082074, 1);
            }
            else if (ichance == 199) { 
                cm.gainItem(1332015, 1);
            }
            else if (ichance == 200) { 
                cm.gainItem(1432001, 1);
            }
            else if (ichance == 201) { 
                cm.gainItem(1060071, 1);
            }
            else if (ichance == 202) { 
                cm.gainItem(1472007, 1);
            }
            else if (ichance == 203) { 
                cm.gainItem(1472002, 1);
            }
            else if (ichance == 204) { 
                cm.gainItem(1051009, 1);
            }
            else if (ichance == 205) { 
                cm.gainItem(1061037, 1);
            }
            else if (ichance == 206)
                cm.gainItem(1332016, 1);
            else if (ichance == 207)
                cm.gainItem(1332034, 1);
            else if (ichance == 208)
                cm.gainItem(1472020, 1);
            else if ((ichance >= 209) && (ichance <= 215)) 
                cm.gainItem(1102084, 1);
            else if ((ichance >= 216) && (ichance <= 221))
                cm.gainItem(1102086, 1);
            else if ((ichance >= 222) && (ichance <= 228))
                cm.gainItem(1102042, 1);
            else if ((ichance >= 228) && (ichance <= 240))
                cm.gainItem(1032026, 1);
            else if (ichance >= 228)
                cm.gainItem(1082149, 1);
            switch(ichance) {
                case 71:cm.gainItem(204001,1);break;
                case 72:cm.gainItem(2041002, 1);break;
                case 73:cm.gainItem(2040805, 1);break;
                case 74:cm.gainItem(2040702, 1);break;
                case 75:cm.gainItem(2043802, 1);break;
                case 76:cm.gainItem(2040402, 1);break;
                case 77:cm.gainItem(2043702, 1);break;
            }
            cm.dispose();
        }
    }
}
