package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xterminator
 */

public class InnerPortalHandler extends AbstractMaplePacketHandler {
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
/*
		// TODO Need to code this check somehow
		slea.readByte();
		String portal = slea.readMapleAsciiString();
		int toX = slea.readShort();
		int toY = slea.readShort();
		int X = slea.readShort();
		int Y = slea.readShort();			
		log.info("[Hacks] Player {} is trying to jump to a different map portal rather than the correct one");
 */
	}
}