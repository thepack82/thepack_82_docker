package net.sf.odinms.client;

import java.util.List;

public interface IItem extends Comparable<IItem> {
	public final int ITEM = 2;
	public final int EQUIP = 1;
	
	byte getType();
	byte getPosition();
	void setPosition(byte position);
	int getItemId();
	short getQuantity();
	String getOwner();
	int getPetId();
	IItem copy();
	void setOwner(String owner);
	void setQuantity(short quantity);
	public void log(String msg, boolean fromDB);
	List<String> getLog();
}