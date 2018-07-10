function enter(pi) {
	if (pi.getPlayer().getMapId() == 222020200) {
		pi.warp(222020100, "in00");
		return true;
	} else if (pi.getPlayer().getMapId() == 222020100) {
		pi.warp(222020200, "in00");
		return true;
	}
}