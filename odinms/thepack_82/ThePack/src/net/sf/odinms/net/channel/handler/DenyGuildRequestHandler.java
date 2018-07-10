package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xterminator
 */

public class DenyGuildRequestHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readByte();
		String from = slea.readMapleAsciiString();
		MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
		if (cfrom != null) {
			cfrom.getClient().getSession().write(MaplePacketCreator.denyGuildInvitation(c.getPlayer().getName()));
		}
	}
}