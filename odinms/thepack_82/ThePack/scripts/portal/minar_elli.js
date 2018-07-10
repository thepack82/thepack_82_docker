function enter(pi) {
	if (pi.getPlayer().getMapId() == 240010100) {
		pi.warp(101010000, "minar00");
		return true;
	} else if (pi.getPlayer().getMapId() == 101010000) {
		pi.warp(240010100, "elli00");
		return true;
	}
}