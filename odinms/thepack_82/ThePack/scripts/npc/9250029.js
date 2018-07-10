//By Moogra

var pet = Array(5000048, 5000049, 5000050, 5000051, 5000052, 5000053);
var cost = 10000000;

function start() {
    cm.sendSimple("#rAre you ready to adopt a pet Robo?\r\n#bEach Robo costs "+ cost +" mesos#n#k\r\n#L1##fItem/Pet/5000048.img/angry/8##t5000048##l\r\n#L2##fItem/Pet/5000049.img/angry/7##t5000049##l\r\n#L3##fItem/Pet/5000050.img/rise/22##t5000050##l\r\n#L4##fItem/Pet/5000051.img/angry/12##t5000051##l\r\n#L5##fItem/Pet/5000052.img/angry_short/4##t5000052##l\r\n#L6##fItem/Pet/5000053.img/love/3##t5000053##l#k");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.sendOk("Come back if you're ready to adopt a Robo!");
    else {
        if (cm.getMeso() < cost)
            cm.sendOk("You do not have enough mesos.");
        else {
            cm.gainMeso(-cost);
            cm.gainItem(pet[selection-1]);
        }
    }
    cm.dispose();
}  