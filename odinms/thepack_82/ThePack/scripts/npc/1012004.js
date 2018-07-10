importPackage(net.sf.odinms.client);

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendOk("See ya next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
        if (status == 0)
            cm.sendNext("Hello #h #! I'm selling almost everything for pets! Go to my friend Cloy for pets themselves though.");
        else {
            var items = new Array(5170000, 5380000, 2120000, 1812000, 1812001, 4160000, 4160001, 4160002, 4160003, 4160004, 4160005, 4160006, 4160007, 4160008, 4160009, 4160010, 4160012, 4160013, 4160014, 4160015, 4160016, 4160017, 4160019, 4160020, 4160022, 4160023, 4160024, 4160026, 4160027, 4160029, 4160032, 5240000, 5240001, 5240002, 5240003, 5240004, 5240006, 5240007, 5240008, 5240009, 5240010, 5240011, 5240012, 5240013, 5240015, 5240017, 5240020);
            if (status == 1) {
                var selStr = "So, what is your choice? Remember, it'll cost you for each item I sell. 10000000 mesos for the name tag, 20000 mesos for the pet food(bundle of 20), 5000000 mesos for the special pet food, 10000 mesos for the command guides and 2000000 for everything else.";
                for (var i = 0; i < items.length; i++)
                    selStr += "\r\n#b#L" + i + "# #v" + items[i] + "# #t" + items[i] + "##l#k";
                cm.sendSimple(selStr);
            } 
            if (status == 2) {
                var price = 0;
                var sele = items[selection];
                price = 1;
                if (cm.getMeso() < price) {
                    cm.sendOk("You do not have enough mesos.");
                    cm.dispose();
                } else {
                    cm.gainMeso(-price);
                    if (sele != 2120000) 
                        cm.gainItem(sele, 1);
                    else
                        cm.gainItem(sele, 20);
                    cm.dispose();
                }
            } 
        }
    }
}