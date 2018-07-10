/* Brittany
	Henesys Random Hair/Hair Color Change.
*/
var status = 0;
var beauty = 0;
var hairprice = 1000000;
var haircolorprice = 1000000;
var mhair = Array(30310, 30330, 30060, 30150, 30410, 30210, 30140, 30120, 30200, 30560, 30510, 30610, 30470);
var fhair = Array(31150, 31310, 31300, 31160, 31100, 31410, 31030, 31080, 31070, 31610, 31350, 31510, 31740);
var hairnew = Array();

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("I'm Brittany the assistant. If you have #b#t5150010##k or #b#t5151000##k by any chance, then how about letting me change your hairdo?\r\n#L0#I want to buy a coupon!#l\r\n#L1#Haircut: #i5150010##t5150010##l\r\n#L2#Dye your hair: #i5151000##t5151000##l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 0;
                cm.sendSimple("Which coupon would you like to buy?\r\n#L0#Haircut for " + hairprice + " mesos: #i5150010##t5150010##l\r\n#L1#Dye your hair for " + haircolorprice + " mesos: #i5151000##t5151000##l");
            } else if (selection == 1) {
                beauty = 1;
                hairnew = Array();
                if (cm.getChar().getGender() == 0) {
                    for(var i = 0; i < mhair.length; i++)
                        hairnew.push(mhair[i] + parseInt(cm.getChar().getHair()% 10));
                }
                if (cm.getChar().getGender() == 1) {
                    for(var i = 0; i < fhair.length; i++)
                        hairnew.push(fhair[i] + parseInt(cm.getChar().getHair() % 10));
                }
                cm.sendYesNo("If you use the EXP coupon your hair will change RANDOMLY with a chance to obtain a new experimental style that even you didn't think was possible. Are you going to use #b#t5150010##k and really change your hairstyle?");
            } else if (selection == 2) {
                beauty = 2;
                haircolor = Array();
                var current = parseInt(cm.getChar().getHair()/10)*10;
                for(var i = 0; i < 8; i++)
                    haircolor.push(current + i);
                cm.sendYesNo("If you use a regular coupon your hair will change RANDOMLY. Do you still want to use #b#t5151000##k and change it up?");
            }
        }
        else if (status == 2){
            cm.dispose();
            if (beauty == 1){
                if (cm.haveItem(5150010) == true){
                    cm.gainItem(5150010, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("Enjoy your new and improved hairstyle!");
                } else {
                    cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry...");
                }
            }
            if (beauty == 2){
                if (cm.haveItem(5151000) == true){
                    cm.gainItem(5151000, -1);
                    cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
                    cm.sendOk("Enjoy your new and improved haircolor!");
                } else {
                    cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't dye your hair without it. I'm sorry...");
                }
            }
            if (beauty == 0){
                if (selection == 0 && cm.getMeso() >= hairprice) {
                    cm.gainMeso(-hairprice);
                    cm.gainItem(5150010, 1);
                    cm.sendOk("Enjoy!");
                } else if (selection == 1 && cm.getMeso() >= haircolorprice) {
                    cm.gainMeso(-haircolorprice);
                    cm.gainItem(5151000, 1);
                    cm.sendOk("Enjoy!");
                } else {
                    cm.sendOk("You don't have enough mesos to buy a coupon!");
                }
            }
        }
    }
}
