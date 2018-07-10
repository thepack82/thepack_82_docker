package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class RelogRequestHandler extends AbstractMaplePacketHandler {
	@Override
	public boolean validateState(MapleClient c) {
		return !c.isLoggedIn();
	}
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		c.getSession().write(MaplePacketCreator.getRelogResponse());
	}
}
