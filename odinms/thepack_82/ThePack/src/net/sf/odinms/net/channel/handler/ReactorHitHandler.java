package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.maps.MapleReactor;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Lerk
 */
public class ReactorHitHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        int charPos = slea.readInt();
        short stance = slea.readShort();
        MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
        if (reactor != null && reactor.isAlive())
            reactor.hitReactor(charPos, stance, c);
    }
}
