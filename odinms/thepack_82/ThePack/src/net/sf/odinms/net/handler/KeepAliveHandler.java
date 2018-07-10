package net.sf.odinms.net.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class KeepAliveHandler implements MaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		c.pongReceived();
	}

	@Override
	public boolean validateState(MapleClient c) {
		return true;
	}
}
