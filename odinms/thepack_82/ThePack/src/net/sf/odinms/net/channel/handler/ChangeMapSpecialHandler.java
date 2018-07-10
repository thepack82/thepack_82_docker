package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapSpecialHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readByte();
		String startwp = slea.readMapleAsciiString();
		slea.readByte();
		slea.readByte();
		MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);
		if (portal != null) {
			portal.enterPortal(c);
		} else {
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}
}
