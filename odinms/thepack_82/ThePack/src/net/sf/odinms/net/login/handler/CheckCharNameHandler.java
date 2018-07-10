package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CheckCharNameHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String name = slea.readMapleAsciiString();
		c.getSession().write(MaplePacketCreator.charNameResponse(name, !MapleCharacterUtil.canCreateChar(name, c.getWorld())));
	}
}
