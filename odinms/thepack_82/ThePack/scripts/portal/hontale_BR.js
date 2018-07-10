importPackage(net.sf.odinms.server.maps);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);

function enter(pi) {
	if (pi.getPlayer().getMapId() == 240060000) {
		var nextMap = 240060100;
		var eim = pi.getPlayer().getEventInstance()
		var target = eim.getMapInstance(nextMap);
		var targetPortal = target.getPortal("sp");
		// only let people through if the eim is ready
		var avail = eim.getProperty("head1");
		if (avail != "yes") {
			// do nothing; send message to player
			pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Horntail\'s Seal is Blocking this Door."));
			return false;
		}else {
			pi.getPlayer().changeMap(target, targetPortal);
			if (eim.getProperty("head2spawned") != "yes") {
				eim.setProperty("head2spawned", "yes");
				eim.schedule("headTwo", 5000);
			}
			return true;
		}
	} else if (pi.getPlayer().getMapId() == 240060100) {
		var nextMap = 240060200;
		var eim = pi.getPlayer().getEventInstance()
		var target = eim.getMapInstance(nextMap);
		var targetPortal = target.getPortal("sp");
		// only let people through if the eim is ready
		var avail = eim.getProperty("head2");
		if (avail != "yes") {
			// do nothing; send message to player
			pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Horntail\'s Seal is Blocking this Door."));
			return false;
		}else {
			pi.getPlayer().changeMap(target, targetPortal);
			return true;
		}
	}
	return true;	
}