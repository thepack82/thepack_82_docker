package net.sf.odinms.scripting.event;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapFactory;

/**
 *
 * @author Matze
 */
public class EventInstanceManager {

    private List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
    private List<MapleMonster> mobs = new LinkedList<MapleMonster>();
    private Map<MapleCharacter, Integer> killCount = new HashMap<MapleCharacter, Integer>();
    private EventManager em;
    private MapleMapFactory mapFactory;
    private String name;
    private Properties props = new Properties();
    private long timeStarted = 0;
    private long eventTime = 0;

    public EventInstanceManager(EventManager em, String name) {
        this.em = em;
        this.name = name;
        mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz")));
        mapFactory.setChannel(em.getChannelServer().getChannel());
    }

    public void registerPlayer(MapleCharacter chr) {
        try {
            chars.add(chr);
            chr.setEventInstance(this);
            em.getIv().invokeFunction("playerEntry", this, chr);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getInstanceId() {
        return ChannelServer.getInstance(1).getInstanceId();
    }

    public void addInstanceId() {
        ChannelServer.getInstance(1).addInstanceId();
    }

    public void startEventTimer(long time) {
        timeStarted = System.currentTimeMillis();
        eventTime = time;
    }

    public boolean isTimerStarted() {
        return eventTime > 0 && timeStarted > 0;
    }

    public long getTimeLeft() {
        return eventTime - (System.currentTimeMillis() - timeStarted);
    }

    public void registerParty(MapleParty party, MapleMap map) {
        for (MaplePartyCharacter pc : party.getMembers()) {
            MapleCharacter c = map.getCharacterById(pc.getId());
            registerPlayer(c);
        }
    }

    public void unregisterPlayer(MapleCharacter chr) {
        chars.remove(chr);
        chr.setEventInstance(null);
    }

    public int getPlayerCount() {
        return chars.size();
    }

    public List<MapleCharacter> getPlayers() {
        return new ArrayList<MapleCharacter>(chars);
    }

    public void registerMonster(MapleMonster mob) {
        mobs.add(mob);
        mob.setEventInstance(this);
    }

    public void unregisterMonster(MapleMonster mob) {
        mobs.remove(mob);
        mob.setEventInstance(null);
        if (mobs.size() == 0) {
            try {
                em.getIv().invokeFunction("allMonstersDead", this);
            } catch (ScriptException ex) {
                Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void playerKilled(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("playerDead", this, chr);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean revivePlayer(MapleCharacter chr) {
        try {
            Object b = em.getIv().invokeFunction("playerRevive", this, chr);
            if (b instanceof Boolean) {
                return (Boolean) b;
            }
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public void playerDisconnected(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("playerDisconnected", this, chr);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param chr
     * @param mob
     */
    public void monsterKilled(MapleCharacter chr, MapleMonster mob) {
        try {
            Integer kc = killCount.get(chr);
            int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mob.getId())).intValue();
            if (kc == null) {
                kc = inc;
            } else {
                kc += inc;
            }
            killCount.put(chr, kc);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getKillCount(MapleCharacter chr) {
        Integer kc = killCount.get(chr);
        if (kc == null) {
            return 0;
        } else {
            return kc;
        }
    }

    public void dispose() {
        chars.clear();
        mobs.clear();
        killCount.clear();
        mapFactory = null;
        em.disposeInstance(name);
        em = null;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public void schedule(final String methodName, long delay) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                try {
                    em.getIv().invokeFunction(methodName, EventInstanceManager.this);
                } catch (NullPointerException npe) {
                } catch (ScriptException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }

    public String getName() {
        return name;
    }

    public void saveWinner(MapleCharacter chr) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO eventstats (event, instance, characterid, channel) VALUES (?, ?, ?, ?)");
            ps.setString(1, em.getName());
            ps.setString(2, getName());
            ps.setInt(3, chr.getId());
            ps.setInt(4, chr.getClient().getChannel());
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MapleMap getMapInstance(int mapId) {
        boolean wasLoaded = mapFactory.isMapLoaded(mapId);
        MapleMap map = mapFactory.getMap(mapId);
        if (!wasLoaded) {
            if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
                map.shuffleReactors();
            }
        }
        return map;
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public Object setProperty(String key, String value, boolean prev) {
        return props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void leftParty(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("leftParty", this, chr);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disbandParty() {
        try {
            em.getIv().invokeFunction("disbandParty", this);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Separate function to warp players to a "finish" map, if applicable
    public void finishPQ() {
        try {
            em.getIv().invokeFunction("clearPQ", this);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removePlayer(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("playerExit", this, chr);
        } catch (ScriptException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isLeader(MapleCharacter chr) {
        return (chr.getParty().getLeader().getId() == chr.getId());
    }
}
