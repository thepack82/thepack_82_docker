/* 	Xan
	Lian Hua Hua Skin Care
	MADE BY AAron and Cody from FlowsionMS
    Fixed as it didn't work at all by Moogra
*/
var status = 0;
var skin = Array(0, 1, 2, 3, 4);

function start() {
    cm.sendSimple("Well, hello! Welcome to the Lian Hua Hua Skin-Care! Would you like to have a firm, tight, healthy looking skin like mine?  With #b#tCBD Skin Coupon##k, you can let us take care of the rest and have the kind of skin you've always wanted!\r\n\#L1#Sounds Good!#l");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0)
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (selection == 1) 
                cm.sendStyle("With our specialized service, you can see the way you'll look after the treatment in advance. What kind of a skin-treatment would you like to do? Go ahead and choose the style of your liking...", skin);
        }
        else if (status == 1){
            cm.dispose();
            if (cm.haveItem(5153010)){
                cm.gainItem(5153010 , -1);
                cm.setSkin(skin[selection]);
                cm.sendOk("Enjoy your new and improved skin!");
            } else 
                cm.sendOk("It looks like you don't have the coupon you need to receive the treatment. I'm sorry but it looks like we cannot do it fo you.");
        }
    }
}
