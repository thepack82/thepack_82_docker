package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ServerStatusRequestHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		c.getSession().write(MaplePacketCreator.getServerStatus(0));
	}
}
