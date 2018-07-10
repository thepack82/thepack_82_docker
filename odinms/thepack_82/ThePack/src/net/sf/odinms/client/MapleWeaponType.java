package net.sf.odinms.client;

public enum MapleWeaponType {
	NOT_A_WEAPON(0),
	BOW(3.4),
	CLAW(3.6),
	DAGGER(4),
	CROSSBOW(3.6),
	AXE1H(4.4),
	SWORD1H(4.0),
	BLUNT1H(4.4),
	AXE2H(4.8),
	SWORD2H(4.6),
	BLUNT2H(4.8),
	POLE_ARM(5.0),
	SPEAR(5.0),
	STAFF(3.6),
	WAND(3.6),
	KNUCKLE(4.0),
	GUN(5.0);
	
	private double damageMultiplier;
	
	private MapleWeaponType(double maxDamageMultiplier) {
		this.damageMultiplier = maxDamageMultiplier;
	}
	
	public double getMaxDamageMultiplier() {
		return damageMultiplier;
	}
};
