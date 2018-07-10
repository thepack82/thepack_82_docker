package net.sf.odinms.server.movement;

import java.awt.Point;

import net.sf.odinms.tools.data.output.LittleEndianWriter;


public class ChairMovement extends AbstractLifeMovement  {
	
	private int unk;

	public ChairMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	public int getUnk() {
		return unk;
	}

	public void setUnk(int unk) {
		this.unk = unk;
	}

	@Override
	public void serialize(LittleEndianWriter lew) {
		lew.write(getType());
		lew.writeShort(getPosition().x);
		lew.writeShort(getPosition().y);
		lew.writeShort(unk);
		lew.write(getNewstate());
		lew.writeShort(getDuration());
	}
}

