function enter(pi) {
        if (pi.getPlayer().getEventInstance() != null) {
                pi.getPlayer().getEventInstance().unregisterPlayer(pi.getPlayer());
        }
        pi.warp(101030104);
        return true;
}