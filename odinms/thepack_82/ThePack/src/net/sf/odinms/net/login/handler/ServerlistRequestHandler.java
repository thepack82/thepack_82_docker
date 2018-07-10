package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ServerlistRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        for (int i = 0; i < LoginServer.getInstance().numberOfWorlds(); i++) {
            c.getSession().write(MaplePacketCreator.getServerList(i, LoginServer.getInstance().getServerName() + " World " + i, LoginServer.getInstance().getLoad()));
            c.getSession().write(MaplePacketCreator.getEndOfServerList());
        }
    }
}
