package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class MesoDropHandler extends AbstractMaplePacketHandler {

    public MesoDropHandler() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        int meso = slea.readInt();
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (meso <= c.getPlayer().getMeso()) {
            c.getPlayer().gainMeso(-meso, false, true);
            c.getPlayer().getMap().spawnMesoDrop(meso, meso, c.getPlayer().getPosition(), c.getPlayer(), c.getPlayer(), false);
        }
    }
}
