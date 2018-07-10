package net.sf.odinms.client;

public enum MapleJob {
	BEGINNER(0),
	WARRIOR(100),
	FIGHTER(110),
	CRUSADER(111),
	HERO(112),
	PAGE(120),
	WHITEKNIGHT(121),
	PALADIN(122),
	SPEARMAN(130),
	DRAGONKNIGHT(131),
	DARKKNIGHT(132),
	MAGICIAN(200),
	FP_WIZARD(210),
	FP_MAGE(211),
	FP_ARCHMAGE(212),
	IL_WIZARD(220),
	IL_MAGE(221),
	IL_ARCHMAGE(222),
	CLERIC(230),
	PRIEST(231),
	BISHOP(232),
	BOWMAN(300),
	HUNTER(310),
	RANGER(311),
	BOWMASTER(312),
	CROSSBOWMAN(320),
	SNIPER(321),
	MARKSMAN(322),
	THIEF(400),
	ASSASSIN(410),
	HERMIT(411),
	NIGHTLORD(412),
	BANDIT(420),
	CHIEFBANDIT(421),
	SHADOWER(422),
	PIRATE(500),
	BRAWLER(510),
	MARAUDER(511),
	BUCCANEER(512),
	GUNSLINGER(520),
	OUTLAW(521),
	CORSAIR(522),
	GM(900),
	SUPERGM(910)
	;

	final int jobid;

	private MapleJob(int id) {
		jobid = id;
	}

	public int getId() {
		return jobid;
	}

	public static MapleJob getById(int id) {
		for (MapleJob l : MapleJob.values()) {
			if (l.getId() == id) {
				return l;
			}
		}
		return null;
	}
	
	public static MapleJob getBy5ByteEncoding(int encoded) {
		switch (encoded) {
			case 2:
				return WARRIOR;
			case 4:
				return MAGICIAN;
			case 8:
				return BOWMAN;
			case 16:
				return THIEF;
			case 32:
				return PIRATE;
			default:
				return BEGINNER;
		}
	}
	
	public boolean isA (MapleJob basejob) {		
		return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
	}
}
