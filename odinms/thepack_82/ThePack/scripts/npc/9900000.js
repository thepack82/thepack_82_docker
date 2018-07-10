// KIN - v.62 Male Support
var status = 0;
var beauty = 0;
var haircolor = Array();
var skin = Array(0, 1, 2, 3, 4, 9);
var hair = Array(30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820);
var hairnew = Array();
var face = Array(20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024);
var facenew = Array();
var colors = Array();

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if(!cm.getPlayer().isGM()) {
                cm.sendOk("Wait a minute, you aren't a GM!");
                cm.dispose();
            } else if (cm.getChar().getGender() == 1) {
                cm.sendOk("I'm sorry, I only do Males. You should try my sister NimaKIM.");
                cm.dispose();
            } else 
                cm.sendSimple("Hey there, I can change your look. What would you like to change?\r\n#L0#Skin#l\r\n#L1#Hair#l\r\n#L2#Hair Color#l\r\n#L3#Eye#l\r\n#L4#Eye Color#l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 1;
                cm.sendStyle("Pick one?", skin);
            } else if (selection == 1) {
                beauty = 2;
                hairnew = Array();
                for(var i = 0; i < hair.length; i++) {
                    if (hair[i] == 30100 || hair[i] == 30010)
                        hairnew.push(hair[i]);
                    else
                        hairnew.push(hair[i] + parseInt(cm.getPlayer().getHair() % 10));
                }
                cm.sendStyle("Pick one?", hairnew);
            } else if (selection == 2) {
                beauty = 3;
                haircolor = Array();
                var current = parseInt(cm.getPlayer().getHair()/10)*10;
                if(current == 30100)
                    haircolor = Array(current , current + 1, current + 2, current + 3, current +4);
                else if (current == 30010)
                    haircolor = Array(current);
                else {
                    for(var i = 0; i < 8; i++)
                        haircolor.push(current + i);
                }
                cm.sendStyle("Pick one?", haircolor);
            } else if (selection == 3) {
                beauty = 4;
                facenew = Array();
                for(var i = 0; i < face.length; i++) {
                    if (face[i] == 20022 || face[i] == 20021)
                        facenew.push(face[i]);
                    else
                        facenew.push(face[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                }
                cm.sendStyle("Pick one?", facenew);
            } else if (selection == 4) {
                beauty = 5;
                var current = cm.getPlayer().getFace() % 100 + 20000;
                colors = Array();
                if(current == 20022 || current == 20021)
                    colors = Array(current , current + 100, current + 200, current + 300, current +400, current + 600, current + 700);
                else
                    colors = Array(current , current + 100, current + 200, current + 300, current +400, current + 500, current + 600, current + 700);
                cm.sendStyle("Pick one?", colors);
            }
        }
        else if (status == 2){
            if (beauty == 1) cm.setSkin(skin[selection]);
            if (beauty == 2) cm.setHair(hairnew[selection]);
            if (beauty == 3) cm.setHair(haircolor[selection]);
            if (beauty == 4) cm.setFace(facenew[selection]);
            if (beauty == 5) cm.setFace(colors[selection]);
            cm.dispose();
        }
    }
}