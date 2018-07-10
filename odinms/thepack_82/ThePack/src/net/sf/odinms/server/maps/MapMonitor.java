package net.sf.odinms.server.maps;

import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;

public class MapMonitor {

    private ScheduledFuture<?> monitorSchedule;
    private MapleMap map;
    private MaplePortal portal;
    private int ch;
    private MapleReactor reactor;

    public MapMonitor(final MapleMap map, MaplePortal portal, int ch, MapleReactor reactor) {
        this.map = map;
        this.portal = portal;
        this.ch = ch;
        this.reactor = reactor;
        this.monitorSchedule = TimerManager.getInstance().register(
                new Runnable() {

                    @Override
                    public void run() {
                        if (map.getCharacters().size() <= 0) {
                            cancelAction();
                        }
                    }
                }, 5000);
    }

    public void cancelAction() {
        monitorSchedule.cancel(false);
        map.killAllMonsters();
        if (portal != null) {
            portal.setPortalStatus(MaplePortal.OPEN);
        }
        if (reactor != null) {
            reactor.setState((byte) 0);
            reactor.getMap().broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
        }
        map.resetReactors();
    }
}