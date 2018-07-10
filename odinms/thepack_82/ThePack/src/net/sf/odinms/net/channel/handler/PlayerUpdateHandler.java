package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Mats
 */

public class PlayerUpdateHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		try {
			c.getPlayer().saveToDB (true);
		} catch (Exception ex) {
			org.slf4j.LoggerFactory.getLogger(PlayerUpdateHandler.class).error("Error updating player", ex);
		}
	}
}
