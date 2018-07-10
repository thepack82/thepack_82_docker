package net.sf.odinms.net.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public final class LoginRequiringNoOpHandler implements MaplePacketHandler {
	private static LoginRequiringNoOpHandler instance = new LoginRequiringNoOpHandler();

	private LoginRequiringNoOpHandler() {
	}

	public static LoginRequiringNoOpHandler getInstance() {
		return instance;
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
	}

	@Override
	public boolean validateState(MapleClient c) {
		return c.isLoggedIn();
	}
}
