package net.sf.odinms.net.channel.handler;

import java.sql.SQLException;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.net.AbstractMaplePacketHandler;


public class NoteActionHandler extends AbstractMaplePacketHandler {

	public NoteActionHandler() {
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int action = slea.readByte();
		if (action == 1) { //delete
			int num = slea.readByte();
			slea.readByte(); //padding?
			slea.readByte(); //always 62?
			for (int i = 0; i < num; i++) {
				int id = slea.readInt();
				slea.readByte();
				try {
					c.getPlayer().deleteNote(id);
				} catch (SQLException e) {}
			}
		}
	}
}