package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CancelItemEffectHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int sourceid = slea.readInt();
		MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect(-sourceid);
		c.getPlayer().cancelEffect(effect, false, -1);
	}
}
