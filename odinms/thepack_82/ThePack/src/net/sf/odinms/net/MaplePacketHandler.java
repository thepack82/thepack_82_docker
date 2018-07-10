package net.sf.odinms.net;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public interface MaplePacketHandler {
	void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c);
	boolean validateState(MapleClient c);
}
