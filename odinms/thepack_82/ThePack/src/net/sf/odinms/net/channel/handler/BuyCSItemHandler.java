package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.server.CashItemFactory;
import net.sf.odinms.server.CashItemInfo;

/**
 *
 * @author NIGHTCOFFEE based on Acrylic
 */
public class BuyCSItemHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.readByte() != 3) {
            c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            c.getSession().write(MaplePacketCreator.enableCSorMTS());
            c.getSession().write(MaplePacketCreator.enableCSUse1());
            c.getSession().write(MaplePacketCreator.enableCSUse2());
            c.getSession().write(MaplePacketCreator.enableCSUse3());
            return;
        }
        slea.skip(1);
        int way = slea.readByte();
        slea.skip(3);
        int snCS = slea.readInt();
        slea.skip(1);
        CashItemInfo item = CashItemFactory.getItem(snCS);
        if (!c.getPlayer().inCS() || c.getPlayer().getCSPoints(way) < 0 || c.getPlayer().getCSPoints(way) < item.getPrice()) {
            c.getPlayer().ban("Trying to packet edit.");
        }
        if (item.getId() >= 5000000 && item.getId() <= 5000100) {
            MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "Cash Item was purchased.", null, MaplePet.createPet(item.getId()));
        } else {
            MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "Cash Item was purchased.");
        }
        c.getSession().write(MaplePacketCreator.showBoughtCSItem(item.getId()));
        c.getPlayer().modifyCSPoints(way, -item.getPrice());
        c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MaplePacketCreator.enableCSorMTS());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
        c.getSession().write(MaplePacketCreator.enableCSUse3());
    }
}