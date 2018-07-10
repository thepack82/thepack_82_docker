package net.sf.odinms.server.maps;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;

/*
 * MapleTVEffect
 * Created by Lios
 * All credits to Cheetah and MrMysterious for creating
 * the MapleTV Method!  Good job guys~!
 */

public class MapleTVEffect {

    private static List<String> message = new LinkedList<String>();
    private static MapleCharacter user;
    private static boolean active;
    private static int type;
    private static MapleCharacter partner = null;
    private static MapleClient c;

    public MapleTVEffect(MapleCharacter user_, MapleCharacter partner_, List<String> msg, int type_) {
        message = msg;
        user = user_;
        type = type_;
        partner = partner_;
        broadCastTV(true);
    }

    public static boolean isActive() {
        return active;
    }

    private static void setActive(boolean set) {
        active = set;
    }

    private static MaplePacket removeTV() {
        return MaplePacketCreator.removeTV();
    }

    public static MaplePacket startTV() {
        return MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner);
    }

    public static void broadCastTV(boolean active_) {
        WorldChannelInterface wci = user.getClient().getChannelServer().getWorldInterface();
        setActive(active_);
        try {
            if (active_) {
                wci.broadcastMessage(null, MaplePacketCreator.enableTV().getBytes());
                wci.broadcastMessage(null, startTV().getBytes());
                scheduleCancel();
            } else {
                wci.broadcastMessage(null, removeTV().getBytes());
            }

        } catch (RemoteException noob) {
            c.getChannelServer().reconnectWorld();
        }
    }

    private static void scheduleCancel() {
        int delay = 15000; // default. cbf adding it to switch
        switch (type) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
                delay = 15000;
                break;
            case 4:
                delay = 30000;
                break;
            case 5:
                delay = 60000;
                break;
        }
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                broadCastTV(false);
            }
        }, delay);
    }
}  