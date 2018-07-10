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
 * @author Matze
 */
public class UseItemHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive() || c.getPlayer().isPvPMap()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId) {
                return;
            }
            if (itemId == 2022178 || itemId == 2050004) {
                c.getPlayer().dispelDebuffs();
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            } 
            if (ii.isTownScroll(itemId)) {
                if (ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer())) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                }
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer());
            c.getPlayer().checkBerserk();
        } else {
            return;
        }
    }
}