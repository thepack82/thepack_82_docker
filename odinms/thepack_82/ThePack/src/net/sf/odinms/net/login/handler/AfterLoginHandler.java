package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AfterLoginHandler extends AbstractMaplePacketHandler {
	private static Logger log = LoggerFactory.getLogger(AfterLoginHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		byte c2 = slea.readByte();
		byte c3 = slea.readByte();
		if (c2 == 1 && c3 == 1) {
			// Official requests the pin here - but pins suck so we just accept
			c.getSession().write(MaplePacketCreator.pinAccepted());
		} else if (c2 == 1 && c3 == 0) {
			slea.seek(8);
			String pin = slea.readMapleAsciiString();
			log.info("Received Pin: " + pin);
			if (pin.equals("1234")) {
				c.getSession().write(MaplePacketCreator.pinAccepted());
			} else {
				c.getSession().write(MaplePacketCreator.requestPinAfterFailure());
			}
		}
	}
}
