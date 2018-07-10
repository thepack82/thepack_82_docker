package net.sf.odinms.provider;

import java.util.List;

/**
 *
 * @author Matze
 */
public interface MapleDataDirectoryEntry extends MapleDataEntry {
	public List<MapleDataDirectoryEntry> getSubdirectories();
	public List<MapleDataFileEntry> getFiles();
	public MapleDataEntry getEntry(String name);
}
