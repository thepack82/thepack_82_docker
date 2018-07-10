/* Sex Changing NPC
 *  Performs changeSex() function.
 *    - Offers two pacakages:
 *      VIP
 *      REG
 *      
 *   REG Package - Sex change, random eyes, hair.
 *   VIP Pacakge - Sex change, eye/hair of choice.
 *  
 *   Fruity Candy for successful operations.~
 */

/*
 * @author Goofy/iGoofy <kinggoofy1@hotmail.com>
 */

var status = 0;
var vipcont;
var haircolor = Math.floor(Math.random() * 6 + 1);
var eyecolor = Math.floor(Math.random() * 6 + 1);
//Prices for REG and VIP sex changes.
var regprice = 20000000;
var vipprice = 50000000;
//If the player clicks yes while status is 2, reg/vip = true.
var reg;
var vip;
//If reg/vip = true, doreg/vip = true when player presses next while status is 3.
var dovip;
var doreg;
//Hairs/eyes a male player can get upon changing to a female and buying a regular package.
var regfemale = new Array (31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200);
var possible = Math.floor(Math.random() * regfemale.length); //Generates a number, that number is the hair the player receives.
var regeyesf = new Array (21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014, 21016, 21017, 21018, 21019, 21020, 21022);
var regpossiblef = Math.floor(Math.random() * regeyesf.length);
//Hairs a female player can getupon changing to a male and buying a regular package
var regmale = new Array (30000, 30020, 30030, 30040, 30050, 30060, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240);
var possibletwo = Math.floor(Math.random() * regmale.length);
var regeyesm = new Array (20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023);
var regpossiblem = Math.floor(Math.random() * regeyesm.length);
//Hairs/eyes a male can get upon changing to a female & a VIP package is purchased.
var vipfemale = new Array (31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31410, 31420, 31430, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740);
var vipeyesf = new Array (21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014, 21016, 21017, 21018, 21019, 21020, 21022);
//Hairs/eyes a female can get upon changing to a male & a VIP package is purchased.
var vipmale = new Array (30000, 30020, 30030, 30040, 30050, 30060, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30430, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30700, 30710);
var vipeyesm = new Array (20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.sendOk("You'll be back.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendYesNo("Ah hello, hello! I'm #bDr. #p" + cm.getNpc() + "##k. I'm ready to perform plastic surgery! Don't worry, I performed over 50 successful surgeries while still in school! So, what do you say? Do you want to change your sex?");
        }
        if (status == 1) {
            cm.sendSimple("Fabulous! I'm offering #b2#k packages for sex changes, so pick one and I'll get started!\r\n#b#L0#Regular Package (" + regprice + " mesos).#l\r\n#L1#VIP Package (" + vipprice +"mesos).#l#k ");
        }
        if (status == 2) {
            if (selection == 0) {
                cm.sendYesNo("You want the #bRegular Package#k, correct? For #b" + regprice + "#k mesos, I can change your sex and I'll even throw in a nice hairstyle and eyes as a small gift from me! Ready to #ego under the knife#k? ");
                reg = true;
            }
            if (selection == 1) {
                cm.sendYesNo("You want the #bVIP Package#k, correct? For #b" + vipprice + "#k mesos I'll change your sex and I'll let you choose any new hairstyle or eyes that you want to have afterwards! Alright, are you ready? ");
                vip = true;
            }
        }
        if (status == 3 && reg == true) {
            if (cm.getMeso() >= regprice) {
                cm.sendNext("Alright, #b#h ##k. Take a deep breath and breath in this gas. You'll begin to feel sleepy a...\r\n#p " + cm.getNpc() + "#'s voice fades... ");
                doreg = true;
            } else {
                cm.sendOk("I'm sorry, #h #, I don't think you have enough mesos for the #bRegular Package#k. Come back with #b" + regprice + "#k mesos and we'll talk. ");
            }
        }
        if (status == 3 && vip == true) {
            if (cm.getMeso() >= vipprice) {
                cm.sendNext("Alright, #b#h ##k. Take a deep breath and breath in this gas. You'll begin to feel sleepy a...\r\n#p " + cm.getNpc() + "#'s voice fades... ");
                dovip = true;
            } else {
                cm.sendOk("I'm sorry, #h #, I don't think you have enough mesos for the #bVIP Package#k. Come back with #b" + vipprice + "#k mesos and we'll talk. ");
            }
        }
        if (status == 4 && doreg) {
            cm.getPlayer().setGender(1-cm.getPlayer().getGender());
            cm.gainMeso(-regprice);
            cm.gainItem(2022043, 10);
            if (cm.getPlayer().getGender() == 0) {
                cm.setHair(regmale[possible] + haircolor);
                cm.setFace(regeyesm[regpossiblem]);
            } else {
                cm.setHair(regfemale[possibletwo] + haircolor);
                cm.setFace(regeyesf[regpossiblef]);
            }
            cm.sendNext("The operation was a success! Congratulations, #h #, you are now the opposite sex! I had one of the local hair stylists come in and give you an awesome new hairdo and I gave you a new face while I was waiting for the sleeping gas to wear off. Enjoy your new body! Also, before I forget, here's a sack of #bFruity Candy#k as a little treat for not dying! ");
            status = 20;
        } else if (status == 4 && dovip == true) {
            cm.getPlayer().setGender(1-cm.getPlayer().getGender());
            cm.gainMeso(-vipprice);
            cm.gainItem(2022043, 20);
            cm.sendNext("Eureka! The operation was a complete success! Here you are, #h #, a sack of #bFruity Candy#k as a special treat for lining my wallet. Get ready, it's time to #eselect your new look#k.");
            vipcont = true;
        }
                
        if (status == 5 && vipcont == true) {
            if (cm.getPlayer().getGender() == 1) {
                cm.sendStyle("Pick your new hairstyle. Try to go with one that suits your new sex. One that screams 'I'M A TRANNY!'", vipfemale);
            } else {
                cm.sendStyle("Pick your new hairstyle. Try to go with one that suits your new sex. One that screams 'I'M A TRANNY!'", vipmale);
            }
        }
        if (status == 6) {
            if (cm.getPlayer().getGender() == 1) {
                cm.setHair(vipfemale[selection] + haircolor);
            } else {
                cm.setHair(vipmale[selection] + haircolor);
            }
            cm.sendNext("I hope you're happy with your new hairstyle! Time to select your new face.")
        }
        if (status == 7) {
            if (cm.getPlayer().getGender() == 1) {
                cm.sendStyle("Time for you to select a new face. Choose any you want, free of charge. Make sure you find one that suits the new you!", vipeyesf);
            } else {
                cm.sendStyle("Time for you to select a new face. Choose any you want, free of charge. Make sure you find one that suits the new you!", vipeyesm);
            }
        }
        if (status == 8) {
            if (cm.getPlayer().getGender() == 1) {
                cm.setFace(vipeyesf[selection]);
            } else {
                cm.setFace(vipeyesm[selection]);
            }
            cm.sendNext("Alright then, I believe we're all finished here! Come back if you ever feel like changing your sex again. I could use some more money! Goodbye now!");
        }
        if (status == 9) {
            cm.unequipEverything();
            cm.disconnect();
            cm.dispose();
        }
        if (status == 20) {
            cm.unequipEverything();
            cm.disconnect();
            cm.dispose();
        } 
    
    }
}  