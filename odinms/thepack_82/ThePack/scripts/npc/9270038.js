/* 	
Shalon - Warp to Kerning (From Singapore)
Credits to Cody(shotdownsoul)/AAron from FlowsionMS Forums
Credits to Angel-SL on helping me @_@
Credits to Info for helping me @_@
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            cm.sendSimple("Hello there. I am Shalon from Singapore Airport. I can assist you in getting back to Kerning City in no time. How can I help you?\r\n#L0##bI would like to go back to Kerning City.#k#l");
        else if (status == 1) {
            if (selection == 0)
                cm.warp(103000000, 0);
            cm.dispose();
        }
    }
}