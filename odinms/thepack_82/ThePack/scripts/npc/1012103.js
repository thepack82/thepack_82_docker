//Author: Moogra
var mob = Array(8500001, 8510000, 9400014, 9400121, 9400112);

function start() {
    cm.sendSimple("I am the boss summoner! Would you like me to spawn some bosses for you? The price to summon monsters is " + 25000 * cm.getC().getChannelServer().getMesoRate() + " mesos. \r\n Please choose #b\r\n#L0#Papulatus clock#l\r\n#L1#Pianus#l\r\n#L2#Black Crow#l\r\n#L3#Anego#l\r\n#L4#BodyGuard A#l#k");
}

function action(mode, type, selection) {
    if (cm.getMeso() > 25000* cm.getC().getChannelServer().getMesoRate()) {
        cm.summonMob(mob[selection]);
        cm.gainMeso(-25000* cm.getC().getChannelServer().getMesoRate());
    } else {
        cm.sendOk("You do not have enough mesos.");
    }
    cm.dispose();
}