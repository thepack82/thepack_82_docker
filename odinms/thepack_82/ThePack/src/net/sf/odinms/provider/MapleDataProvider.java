package net.sf.odinms.provider;

public interface MapleDataProvider {
	MapleData getData(String path);
	MapleDataDirectoryEntry getRoot();
}
