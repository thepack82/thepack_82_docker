package net.sf.odinms.client;

import net.sf.odinms.net.LongValueHolder;

public enum MapleDisease implements LongValueHolder {
	NULL(0x0),
	SLOW(0x1),
	SEDUCE(0x80),
	STUN(0x2000000000000L),
	POISON(0x4000000000000L),
	SEAL(0x8000000000000L),
	DARKNESS(0x10000000000000L),
	WEAKEN(0x4000000000000000L)
	;
	
	private long i;

	private MapleDisease(long i) {
		this.i = i;
	}

	@Override
	public long getValue() {
		return i;
	}
}