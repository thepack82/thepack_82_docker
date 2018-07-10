package net.sf.odinms.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.sf.odinms.client.MapleClient;

/**
 *
 * @author Matze
 */
public class AutobanManager implements Runnable {

    private static class ExpirationEntry implements Comparable<ExpirationEntry> {

        public long time;
        public int acc, points;

        public ExpirationEntry(long time, int acc, int points) {
            this.time = time;
            this.acc = acc;
            this.points = points;
        }

        public int compareTo(AutobanManager.ExpirationEntry o) {
            return (int) (time - o.time);
        }
    }
    private Map<Integer, Integer> points = new HashMap<Integer, Integer>();
    private Map<Integer, List<String>> reasons = new HashMap<Integer, List<String>>();
    private Set<ExpirationEntry> expirations = new TreeSet<ExpirationEntry>();
    private static final int AUTOBAN_POINTS = 2147483647;
    private static AutobanManager instance = null;

    public static AutobanManager getInstance() {
        if (instance == null) {
            instance = new AutobanManager();
        }
        return instance;
    }

    public void autoban(MapleClient c, String reason) {
        if (c.getPlayer().isGM()) {
            return;
        }
        addPoints(c, AUTOBAN_POINTS, 0, reason);
    }

    public synchronized void addPoints(MapleClient c, int points, long expiration, String reason) {
        if (c.getPlayer().isGM()) {
            return;
        }
        int acc = c.getPlayer().getAccountID();
        List<String> reasonList;
        if (this.points.containsKey(acc)) { //uncomment if you want to enable autoban
          //  if (this.points.get(acc) >= AUTOBAN_POINTS) {
                return;
          //  }
          //  this.points.put(acc, this.points.get(acc) + points);
          //  reasonList = this.reasons.get(acc);
          //  reasonList.add(reason);
        } else {
            this.points.put(acc, points);
            reasonList = new LinkedList<String>();
            reasonList.add(reason);
            this.reasons.put(acc, reasonList);
        }
//        if (this.points.get(acc) >= AUTOBAN_POINTS) {
//            String name = c.getPlayer().getName();
//            StringBuilder banReason = new StringBuilder("Autoban for Character ");
//            banReason.append(name);
//            banReason.append(" (IP ");
//            banReason.append(c.getSession().getRemoteAddress().toString());
//            banReason.append("): ");
//            for (String s : reasons.get(acc)) {
//                banReason.append(s);
//                banReason.append(", ");
//            }
//
//            if (!c.getPlayer().isGM()) {
//                c.getPlayer().ban(banReason.toString());
//                try {
//                    c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "" + name + " has been banned by the system. (Last reason: " + reason + ")").getBytes());
//                } catch (RemoteException e) {
//                    c.getChannelServer().reconnectWorld();
//                }
//            }
//            return;
//        }
        if (expiration > 0) {
            expirations.add(new ExpirationEntry(System.currentTimeMillis() + expiration, acc, points));
        }
    }

    public void run() {
        long now = System.currentTimeMillis();
        for (ExpirationEntry e : expirations) {
            if (e.time <= now) {
                this.points.put(e.acc, this.points.get(e.acc) - e.points);
            } else {
                return;
            }
        }
    }
}
