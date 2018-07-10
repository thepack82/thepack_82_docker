package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PetChatHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int petId = slea.readInt();
		slea.readInt();
		int unknownShort = slea.readShort();
		String text = slea.readMapleAsciiString();
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.petChat(c.getPlayer().getId(), unknownShort, text, c.getPlayer().getPetIndex(petId)), true);
	}
}
