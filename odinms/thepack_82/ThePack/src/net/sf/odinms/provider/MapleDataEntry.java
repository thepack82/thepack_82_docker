package net.sf.odinms.provider;

/**
 *
 * @author Matze
 */
public interface MapleDataEntry extends MapleDataEntity {
	public String getName();
	public int getSize();
	public int getChecksum();
	public int getOffset();
}
