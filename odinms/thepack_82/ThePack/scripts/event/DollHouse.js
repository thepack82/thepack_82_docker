/**
	Hak, travel between Orbis, Mu Lung and Herb Town
**/

importPackage(net.sf.odinms.world);
importPackage(net.sf.odinms.tools);

var returnMap;
var map;
var eim;

function init() {
	em.setProperty("noEntry","false");
}

function playerEntry(eim, player) {
	returnMap = em.getChannelServer().getMapFactory().getMap(221024400);
	eim = em.getInstance("DollHouse");
	map = eim.getMapFactory().getMap(922000010);
	player.changeMap(map, map.getPortal(0));
	map.shuffleReactors();
	em.setProperty("noEntry","true");
	em.schedule("timeOut", 600000);
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(600));
}

function playerExit(eim, player) {
	em.setProperty("noEntry","false");
	player.changeMap(returnMap, returnMap.getPortal(4));
	eim.unregisterPlayer(player);
	em.cancel();
	em.disposeInstance("DollHouse");
	eim.dispose();
}

function timeOut() {
	em.setProperty("noEntry","false");
	var player = eim.getPlayers().get(0);
	player.changeMap(returnMap, returnMap.getPortal(4));
	eim.unregisterPlayer(player);
	em.cancel();
	em.disposeInstance("DollHouse");
	eim.dispose();
}

function playerDisconnected(eim, player) {
	em.setProperty("noEntry","false");
	player.getMap().removePlayer(player);
	player.setMap(returnMap);
	eim.unregisterPlayer(player);
	em.cancel();
	em.disposeInstance("DollHouse");
	eim.dispose();
}

function clear(eim) {
	em.setProperty("noEntry","false");
	var player = eim.getPlayers().get(0);
	player.changeMap(returnMap, returnMap.getPortal(4));
	eim.unregisterPlayer(player);
	em.cancel();
	em.disposeInstance("DollHouse");
	eim.dispose();
}

function cancelSchedule() {
}
