package net.sf.odinms.scripting.portal;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.scripting.AbstractPlayerInteraction;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.maps.MapleReactor;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.server.maps.MapMonitor;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private MaplePortal portal;

    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
        super(c);
        this.portal = portal;
    }

    public void sendMessage(String message) {
        new ServernoticeMapleClientMessageCallback(0, c).dropMessage(message);
    }

    public void createMapMonitor(int mapId, boolean closePortal, int reactorMap, int reactor) {
        if (closePortal) {
            portal.setPortalStatus(MaplePortal.CLOSED);
        }
        MapleReactor r = null;
        if (reactor > -1) {
            r = c.getChannelServer().getMapFactory().getMap(reactorMap).getReactorById(reactor);
            r.setState((byte) 1);
            c.getChannelServer().getMapFactory().getMap(reactorMap).broadcastMessage(MaplePacketCreator.triggerReactor(r, 1));
        }
        new MapMonitor(c.getChannelServer().getMapFactory().getMap(mapId), closePortal ? portal : null, c.getChannel(), r);
    }

    public MaplePortal getPortal() {
        return portal;
    }
}