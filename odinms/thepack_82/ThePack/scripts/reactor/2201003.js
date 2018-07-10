/* Ludi PQ Crack Reactor ^_^
 *@Author Jvlaple
  *2201003.js
 */
 
function act() {
	if (rm.getPlayer().getMapId() == 922010900) {
		rm.mapMessage(5, "Alishar has been summoned.");
		rm.spawnMonster(9300012, 941, 184);
	} else if(rm.getPlayer().getMapId() == 922010700) {
		rm.mapMessage(5, "Rombard has been summoned somewhere in the map.");
		rm.spawnMonster(9300010, 1, -211);
	}
}