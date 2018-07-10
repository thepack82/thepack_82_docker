importPackage(net.sf.odinms.client);

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) { // first interaction with NPC
            cm.sendNext("Oh, hello #h #!");
        } else if (status == 1) {
            cm.sendSimple("So, how can I make your day?\r\n#b#L0#I hear you're selling Equips for Pets?!#l#k");
        } else if (status == 2) {
            if (selection == 0) {
                cm.sendNext("Of course I am! Pets are a great companion to your daily adventures! And yet, equips can make them even better!");
            }
        } else {
            var items = new Array (1802000, 1802001, 1802002, 1802003, 1802004, 1802005, 1802006, 1802007, 1802008, 1802009, 1802010, 1802011, 1802012, 1802013, 1802014, 1802015, 1802016, 1802017, 1802018, 1802019, 1802020, 1802021, 1802022, 1802023, 1802024, 1802025, 1802026, 1802027, 1802028, 1802029, 1802030, 1802031, 1802032, 1802033, 1802034, 1802035, 1802036, 1802037, 1802038, 1802042, 1802044, 1802048, 1802049, 1802050, 1802051, 1802052, 1802053, 1802055);
            if (status == 3) {
                var selStr = "So, what is your choice? Remember, it'll cost you #b10,000,000 mesos#k for each pet equip I sell.";
                for (var i = 0; i < items.length; i++){
                    selStr += "\r\n#b#L" + i + "# #v" + items[i] + "# #t" + items[i] + "##l#k";
                }
                cm.sendSimple(selStr);
            }
            if (status == 4) {
                if (cm.getMeso() < 10000000) {
                    cm.sendOk("You do not have enough mesos.");
                    cm.dispose();
                } else {
                    cm.gainMeso(-10000000);
                    cm.gainItem(items[selection], 1);
                    cm.dispose();
		}
            }
        }
    }
}