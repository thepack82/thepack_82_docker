package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class UseChairHandler extends AbstractMaplePacketHandler {

    public UseChairHandler() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int itemId = slea.readInt();
        if (c.getPlayer().getInventory(MapleInventoryType.SETUP).findById(itemId) == null) {
            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(itemId));
            return;
        }
        c.getPlayer().setChair(itemId);
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showChair(c.getPlayer().getId(), itemId), false);
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}