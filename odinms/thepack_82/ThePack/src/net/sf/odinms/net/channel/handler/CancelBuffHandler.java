package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.MaplePacketCreator;

public class CancelBuffHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int sourceid = slea.readInt();
        switch (sourceid) {
            case 3121004:
            case 3221001:
            case 2121001:
            case 2221001:
            case 2321001:
            case 5221004:
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillCancel(c.getPlayer(), sourceid), false);
                break;
        }
        MapleStatEffect effect = SkillFactory.getSkill(sourceid).getEffect(1);
        c.getPlayer().cancelEffect(effect, false, -1);
    }
}