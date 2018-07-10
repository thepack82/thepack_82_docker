/* Roodolph
Happyville Warp NPC
*/

function start() {
    cm.sendSimple ("Do you want to go to the Extra Frosty Snow Zone or Happyville?\r\n#L0#I want to go to Extra Frosty Snow Zone!#l\r\n\#L1#I want to go to Happyville !#l");
}

function action(mode, type, selection) {
    cm.dispose();
    if (selection == 0) {
        cm.gainItem(1472063, 1);
        cm.warp(209080000, 0);
    } else if (selection == 1) {
        cm.sendOk("Ok.");
        cm.warp(209000000, 0);
    }
}