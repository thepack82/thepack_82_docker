package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CharlistRequestHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int server = slea.readByte();
		int channel = slea.readByte() + 1;
		c.setWorld(server);
//		log.info("Client is connecting to server {} channel {}", server, channel);
		c.setChannel(channel);
		c.sendCharList(server);
	}
}
