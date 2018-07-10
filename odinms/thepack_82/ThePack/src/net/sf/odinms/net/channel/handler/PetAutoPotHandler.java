package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Patrick
 */
public class PetAutoPotHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive() || c.getPlayer().isPvPMap()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte type = slea.readByte();
        slea.readLong();
        slea.readInt();
        byte slot = slea.readByte();
        slea.readByte();
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId) {
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(c.getPlayer());
        }
    }
}