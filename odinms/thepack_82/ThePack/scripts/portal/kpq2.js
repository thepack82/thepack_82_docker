importPackage(net.sf.odinms.server.maps);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.client);

/*
Kerning PQ: 2nd stage to 3rd stage portal
*/

function enter(pi) {
	var nextMap = 103000802;
	var eim = pi.getPlayer().getEventInstance()
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("st00");
	// only let people through if the eim is ready
	var avail = eim.getProperty("2stageclear");
	if (avail == null || pi.getPlayer().isGM()) {
		// do nothing; send message to player
		pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "The warp is currently unavailable."));
		return false;
	}
	else {
		pi.getPlayer().changeMap(target, targetPortal);
		return true;
	}
}