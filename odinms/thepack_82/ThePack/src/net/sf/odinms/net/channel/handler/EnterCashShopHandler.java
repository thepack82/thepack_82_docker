package net.sf.odinms.net.channel.handler;

import java.rmi.RemoteException;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
*
* @author Acrylic
*/

public class EnterCashShopHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (c.getChannelServer().allowCashshop()) {
			if (c.getPlayer().getNoPets() > 0) {
				c.getPlayer().unequipAllPets();
			}
			try {
				WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
				wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
				wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
			c.getPlayer().getMap().removePlayer(c.getPlayer());
			c.getSession().write(MaplePacketCreator.warpCS(c));
			c.getPlayer().setInCS(true);
			c.getSession().write(MaplePacketCreator.enableCSorMTS());
			c.getSession().write(MaplePacketCreator.enableCSUse1());
			c.getSession().write(MaplePacketCreator.enableCSUse2());
			c.getSession().write(MaplePacketCreator.enableCSUse3());
			c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
			c.getSession().write(MaplePacketCreator.sendWishList(c.getPlayer().getId()));
			c.getPlayer().saveToDB(true);
		} else {
			c.getPlayer().dropMessage("The Cash Shop is not available.");
			c.getSession().write(MaplePacketCreator.enableActions());
		}
	}
}
