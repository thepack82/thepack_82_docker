package net.sf.odinms.client.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.StringUtil;

/**
 * @author Moogra
 */
public class AdminCommand {

    private static class ShutdownAnnouncer implements Runnable {

        private ChannelServer cserv;
        private long startTime,  time;

        public ShutdownAnnouncer(ChannelServer cs, long t) {
            cserv = cs;
            time = t;
            startTime = System.currentTimeMillis();
        }

        public void run() {
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "The world will be shut down in " + ((time - System.currentTimeMillis() + startTime) / 60000) + " minutes, please log off safely."));
        }
    }

    public static boolean executeAdminCommand(MapleClient c, MessageCallback mc, String line, org.slf4j.Logger log, List<Pair<MapleCharacter, String>> gmlog, Runnable persister) {
        ChannelServer cserv = c.getChannelServer();
        MapleCharacter player = c.getPlayer();
        String[] splitted = line.split(" ");
        if (splitted[0].equals("!pmob")) {
            int npcId = Integer.parseInt(splitted[1]);
            int mobTime = Integer.parseInt(splitted[2]);
            int xpos = player.getPosition().x;
            int ypos = player.getPosition().y;
            int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
            if (splitted[2] == null) {
                mobTime = 0;
            }
            MapleMonster mob = MapleLifeFactory.getMonster(npcId);
            if (mob != null) {
                mob.setPosition(player.getPosition());
                mob.setCy(ypos);
                mob.setRx0(xpos + 50);
                mob.setRx1(xpos - 50);
                mob.setFh(fh);
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, npcId);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "m");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, player.getMapId());
                    ps.setInt(11, mobTime);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    mc.dropMessage("Failed to save MOB to the database");
                }
                player.getMap().addMonsterSpawn(mob, mobTime);
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equals("!pnpc")) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            int xpos = player.getPosition().x;
            int ypos = player.getPosition().y;
            int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(player.getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos + 50);
                npc.setRx1(xpos - 50);
                npc.setFh(fh);
                npc.setCustom(true);
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, npcId);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "n");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, player.getMapId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    mc.dropMessage("Failed to save NPC to the database");
                }
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equals("!setGMlevel")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setGMLevel(Integer.parseInt(splitted[2]));
            mc.dropMessage("Done.");
        } else if (splitted[0].equals("!shutdown") || splitted[0].equals("!shutdownnow")) {
            for (MapleCharacter everyone : cserv.getPlayerStorage().getAllCharacters()) {
                everyone.saveToDB(true);
            }
            int time = 60000;
            if (splitted.length > 1) {
                time *= Integer.parseInt(splitted[1]);
                TimerManager.getInstance().register(new ShutdownAnnouncer(cserv, (long) time), 300000, 300000);
            }
            if (splitted[0].equals("!shutdownnow")) {
                time = 1;
            }
            persister.run();
            cserv.shutdown(time);
        } else if (splitted[0].equals("!sql")) {
            try {
                String sql = StringUtil.joinStringFrom(splitted, 1);
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
                ps.executeUpdate();
                ps.close();
                mc.dropMessage("Done statement " + sql);
            } catch (SQLException e) {
                mc.dropMessage("Something wrong happened.");
            }
        } else {
            mc.dropMessage("Command " + splitted[0] + " does not exist");
            return false;
        }
        return true;
    }
}