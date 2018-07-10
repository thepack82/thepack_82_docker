package net.sf.odinms.net.channel.handler;

import java.sql.SQLException;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Penguins (Acrylic)
 */
public class CouponCodeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        boolean validcode = false;
        int type = -1;
        int item = -1;
        try {
            validcode = c.getPlayer().getNXCodeValid(code.toUpperCase(), validcode);
        } catch (SQLException e) {
        }
        if (validcode) {
            try {
                type = c.getPlayer().getNXCodeType(code);
            } catch (SQLException e) {
            }
            try {
                item = c.getPlayer().getNXCodeItem(code);
            } catch (SQLException e) {
            }
            if (type != 5) {
                try {
                    c.getPlayer().setNXCodeUsed(code);
                } catch (SQLException e) {
                }
            }
            switch (type) {
                case 0:
                case 1:
                case 2:
                    c.getPlayer().modifyCSPoints(type, item);
                    break;
                case 3:
                    c.getPlayer().modifyCSPoints(0, item);
                    c.getPlayer().modifyCSPoints(2, (item / 5000));
                    break;
                case 4:
                    MapleInventoryManipulator.addById(c, item, (short) 1, "An item was obtain from a coupon.", null, -1);
                    c.getSession().write(MaplePacketCreator.showCouponRedeemedItem(item));
                    break;
                case 5:
                    c.getPlayer().modifyCSPoints(0, item);
                    break;
            }
            c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        } else {
            c.getSession().write(MaplePacketCreator.wrongCouponCode());
        }
        c.getSession().write(MaplePacketCreator.enableCSorMTS());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
    }
}