package net.sf.odinms.server.movement;

import java.awt.Point;

import net.sf.odinms.tools.data.output.LittleEndianWriter;

public class JumpDownMovement extends AbstractLifeMovement {
	private Point pixelsPerSecond;
	private int unk;
	private int fh;

	public JumpDownMovement(int type, Point position, int duration, int newstate) {
		super(type, position, duration, newstate);
	}

	public Point getPixelsPerSecond() {
		return pixelsPerSecond;
	}

	public void setPixelsPerSecond(Point wobble) {
		this.pixelsPerSecond = wobble;
	}

	public int getUnk() {
		return unk;
	}

	public void setUnk(int unk) {
		this.unk = unk;
	}

	public int getFH() {
		return fh;
	}
	
	public void setFH(int fh) {
		this.fh = fh;
	}
	
	@Override
	public void serialize(LittleEndianWriter lew) {
		lew.write(getType());
		lew.writeShort(getPosition().x);
		lew.writeShort(getPosition().y);
		lew.writeShort(pixelsPerSecond.x);
		lew.writeShort(pixelsPerSecond.y);
		lew.writeShort(unk);
		lew.writeShort(fh);
		lew.write(getNewstate());
		lew.writeShort(getDuration());
	}
}
