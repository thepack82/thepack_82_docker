package net.sf.odinms.client;

import net.sf.odinms.net.IntValueHolder;

public enum MapleStat implements IntValueHolder {
	SKIN(0x1),
	FACE(0x2),
	HAIR(0x4),
	LEVEL(0x10),
	JOB(0x20),
	STR(0x40),
	DEX(0x80),
	INT(0x100),
	LUK(0x200),
	HP(0x400),
	MAXHP(0x800),
	MP(0x1000),
	MAXMP(0x2000),
	AVAILABLEAP(0x4000),
	AVAILABLESP(0x8000),
	EXP(0x10000),
	FAME(0x20000),
	MESO(0x40000),
	PET(0x180008);
	
	private final int i;

	private MapleStat(int i) {
		this.i = i;
	}

	@Override
	public int getValue() {
		return i;
	}
	
	public static MapleStat getByValue (int value) {
		for (MapleStat stat : MapleStat.values()) {
			if (stat.getValue() == value) {
				return stat;
			}
		}
		return null;
	}
}
