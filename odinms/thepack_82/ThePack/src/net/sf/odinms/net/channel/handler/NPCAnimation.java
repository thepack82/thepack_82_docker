package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.SendPacketOpcode;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.data.output.MaplePacketLittleEndianWriter;

public class NPCAnimation extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		int length = (int) slea.available();
		if (length == 6) { // NPC Talk
			mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
			mplew.writeInt(slea.readInt());
			mplew.writeShort(slea.readShort());
			c.getSession().write(mplew.getPacket());
		} else if (length > 6) { // NPC Move
			byte[] bytes = slea.read(length - 9);
			mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
			mplew.write(bytes);
			c.getSession().write(mplew.getPacket());
		}
	}
}
