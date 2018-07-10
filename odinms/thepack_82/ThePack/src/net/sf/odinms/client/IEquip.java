package net.sf.odinms.client;

public interface IEquip extends IItem {
	public enum ScrollResult {
		SUCCESS, FAIL, CURSE
	}
	byte getUpgradeSlots();
	byte getLocked();
	byte getLevel();
	public int getRingId();
	public short getStr();
	public short getDex();
	public short getInt();
	public short getLuk();
	public short getHp();
	public short getMp();
	public short getWatk();
	public short getMatk();
	public short getWdef();
	public short getMdef();
	public short getAcc();
	public short getAvoid();
	public short getHands();
	public short getSpeed();
	public short getJump();
}