/* 	
Irene - Warp to Singapore (from Kerning City)
Credits to Cody(shotdownsoul)/AAron from FlowsionMS Forums
Credits to Angel-SL on helping me @_@
Credits to Info for helping me @_@
*/

function start() {
    cm.sendSimple("Hello there~ I am Irene from Kerning City. I can assist you in getting to Singapore in no time. How can I help you?\r\n#L0##bI would like to go to Singapore.#k#l");
}

function action(mode, type, selection) {
    switch (selection) {
        case 0:
            cm.warp(540010000, 0);
            break;
    }
    cm.dispose();
}