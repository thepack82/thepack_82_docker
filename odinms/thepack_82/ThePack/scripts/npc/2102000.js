var status = 0;

function start() {
    cm.sendYesNo("Hello there. Welcome to the entrance to #bValvendale#k. A brand new town that just opened up. Ariant was hit by a severe sandstorm that covered the entire city, so now, we made Valvendale right here where the Amorian Challenge! Party Quest is suppose to be. But I've rambled, would you like to go in?");
}

function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(670010100, 1);
    cm.dispose();
}
