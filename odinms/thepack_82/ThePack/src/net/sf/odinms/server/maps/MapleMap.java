package net.sf.odinms.server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Calendar;
import net.sf.odinms.net.channel.pvp.MaplePvp;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.status.MonsterStatus;
import net.sf.odinms.client.status.MonsterStatusEffect;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.life.SpawnPoint;
import net.sf.odinms.tools.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleMap {

    private static final int MAX_OID = 20000;
    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.ITEM, MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR);
    private Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<Integer, MapleMapObject>();
    private Collection<SpawnPoint> monsterSpawn = new LinkedList<SpawnPoint>();
    private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private Collection<MapleCharacter> characters = new LinkedHashSet<MapleCharacter>();
    private Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
    private List<Rectangle> areas = new ArrayList<Rectangle>();
    private MapleFootholdTree footholds = null;
    private int mapid;
    private int runningOid = 100;
    private int returnMapId;
    private int channel;
    private float monsterRate;
    private boolean dropsDisabled = false;
    private boolean clock;
    private boolean boat;
    private boolean docked;
    private String mapName;
    private String streetName;
    private MapleMapEffect mapEffect = null;
    private boolean everlast = false;
    private int forcedReturnMap = 999999999;
    private int timeLimit;
    private static Logger log = LoggerFactory.getLogger(MapleMap.class);
    private MapleMapTimer mapTimer = null;
    private int dropLife = 180000;
    private int decHP = 0;
    private int protectItem = 0;
    private boolean town;

    public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = channel;
        this.returnMapId = returnMapId;
        if (monsterRate > 0) {
            this.monsterRate = monsterRate;
            boolean greater1 = monsterRate > 1.0;
            this.monsterRate = (float) Math.abs(1.0 - this.monsterRate);
            this.monsterRate = this.monsterRate / 2.0f;
            if (greater1) {
                this.monsterRate = 1.0f + this.monsterRate;
            } else {
                this.monsterRate = 1.0f - this.monsterRate;
            }
            TimerManager.getInstance().register(new RespawnWorker(), 5001 + (int) (30.0 * Math.random()));
        }
    }

    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public Collection<MapleCharacter> getNearestPvpChar(Point attacker, double maxRange, double maxHeight, Collection<MapleCharacter> chr) {
        Collection<MapleCharacter> character = new LinkedList<MapleCharacter>();
        for (MapleCharacter a : characters) {
            if (chr.contains(a.getClient().getPlayer())) {
                Point attackedPlayer = a.getPosition();
                MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
                Point nearestPort = Port.getPosition();
                double safeDis = attackedPlayer.distance(nearestPort);
                double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());
                if (MaplePvp.isLeft) {
                    if (attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 2 &&
                            attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight && safeDis > 2) {
                        character.add(a);
                    }
                }
                if (MaplePvp.isRight) {
                    if (attacker.x < attackedPlayer.x && distanceX < maxRange && distanceX > 2 &&
                            attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight && safeDis > 2) {
                        character.add(a);
                    }
                }
            }
        }
        return character;
    }

    public void toggleDrops() {
        dropsDisabled = !dropsDisabled;
    }

    public int getId() {
        return mapid;
    }

    /**
     * Adds a player to this map and sends nescessary data
     *
     * @param chr
     */
    public void addBotPlayer(MapleCharacter chr) {
        synchronized (characters) {
            this.characters.add(chr);
        }
        synchronized (this.mapobjects) {
            if (!chr.isHidden()) {
                broadcastMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
            }
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        }
    }

    public MapleMap getReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }

    public void setReactorState() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 1);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
                }
            }
        }
    }

    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getCurrentPartyId() {
        for (MapleCharacter chr : this.getCharacters()) {
            if (chr.getPartyId() != -1) {
                return chr.getPartyId();
            }
        }
        return -1;
    }

    public void addMapObject(MapleMapObject mapobject) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);
            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        spawnAndAddRangedMapObject(mapobject, packetbakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);

            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (condition == null || condition.canSpawn(chr)) {
                        if (chr.getPosition().distanceSq(mapobject.getPosition()) <= MapleCharacter.MAX_VIEW_RANGE_SQ && !chr.isfake) {
                            packetbakery.sendPackets(chr.getClient());
                            chr.addVisibleMapObject(mapobject);
                        }
                    }
                }
            }

            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

    private void incrementRunningOid() {
        runningOid++;
        for (int numIncrements = 1; numIncrements < MAX_OID; numIncrements++) {
            if (runningOid > MAX_OID) {
                runningOid = 100;
            }
            if (this.mapobjects.containsKey(Integer.valueOf(runningOid))) {
                runningOid++;
            } else {
                return;
            }
        }
        throw new RuntimeException("Out of OIDs on map " + mapid + " (channel: " + channel + ")");
    }

    public void removeMapObject(int num) {
        synchronized (this.mapobjects) {
            this.mapobjects.remove(Integer.valueOf(num));
        }
    }

    public void removeMapObject(MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s4 = Math.abs(initial.x - fh.getX1());
            double alpha = Math.atan(s2 / s1);
            double beta = Math.atan(s1 / s2);
            double s5 = Math.cos(alpha) * (s4 / Math.cos(beta));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    private Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 99));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(MapleCharacter dropOwner, MapleMonster monster) {
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int card = dropOwner.getDropMod();
        if (dropsDisabled || monster.dropsDisabled() || cserv.getDropRate() < 1 || cserv.getBossDropRate() < 1) {
            return;
        }
        int maxDrops;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final boolean isBoss = monster.isBoss();
        if (isBoss) {
            if (dropOwner.getKarma() > 30) {
                maxDrops = 15 * cserv.getBossDropRate() * card;
            } else {
                maxDrops = 10 * cserv.getBossDropRate() * card;
            }
        } else {
            if (dropOwner.getKarma() > 30) {
                maxDrops = 8 * cserv.getDropRate() * card;
            } else {
                maxDrops = 5 * cserv.getDropRate() * card;
            }
        }
        List<Integer> toDrop = new ArrayList<Integer>();
        for (int i = 0; i < maxDrops; i++) {
            toDrop.add(monster.getDrop());
        }
        Set<Integer> alreadyDropped = new HashSet<Integer>();
        int htpendants = 0;
        int htstones = 0;
        for (int i = 0; i < toDrop.size(); i++) {
            if (toDrop.get(i) == 1122000) {
                if (htpendants > 2) {
                    toDrop.set(i, -1);
                } else {
                    htpendants++;
                }
            } else if (toDrop.get(i) == 2041200) {
                if (htstones > 2) {
                    toDrop.set(i, -1);
                } else {
                    htstones++;
                }
            } else if (alreadyDropped.contains(toDrop.get(i)) && !isBoss) {
                toDrop.remove(i);
                i--;
            } else {
                alreadyDropped.add(toDrop.get(i));
            }
        }
        if (toDrop.size() > maxDrops) {
            toDrop = toDrop.subList(0, maxDrops);
        }
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25), footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        while (shiftDirection < 3 && shiftCount < 1000) {
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            for (int i = 0; i < toDrop.size(); i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                if (drop == -1) { // meso
                    final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.98, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    int tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                            (1.0 + r.nextInt(20)) / 10.0));
                    if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                        tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                    }
                    final int meso = tempmeso * mesoRate * card;
                    if (meso > 0) {
                        final MapleMonster dropMonster = monster;
                        final MapleCharacter dropChar = dropOwner;
                        TimerManager.getInstance().schedule(new Runnable() {

                            public void run() {
                                spawnMesoDrop(meso, meso, dropPos, dropMonster, dropChar, isBoss);
                            }
                        }, monster.getAnimationTime("die1"));
                    }
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop));
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop)) {
                            idrop.setQuantity((short) (1 + 100 * Math.random()));
                        } else if (ii.isThrowingStar(drop) || ii.isBullet(drop)) {
                            idrop.setQuantity((short) (1));
                        }
                    }
                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster " + monster.getObjectId() + " (" + monster.getId() + ") at " + dropPos.toString() + " on map " + mapid);
                    idrop.log(logMsg.toString(), false);
                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), isBoss ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });
                            tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
                        }
                    }, monster.getAnimationTime("die1"));
                }
            }
        }
    }

    public boolean damageMonster(MapleCharacter chr, MapleMonster monster, int damage) {
        if (monster.getId() == 8800000) {
            Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
            for (MapleMapObject object : objects) {
                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        return true;
                    }
                }
            }
        }
        if (monster.isAlive()) {
            boolean killMonster = false;
            synchronized (monster) {
                if (!monster.isAlive()) {
                    return false;
                }
                if (damage > 0) {
                    int monsterhp = monster.getHp();
                    monster.damage(chr, damage, true);
                    if (!monster.isAlive()) { // monster just died
                        killMonster(monster, chr, true);
                        if (monster.getId() >= 8810002 && monster.getId() <= 8810009) {
                            Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
                            for (MapleMapObject object : objects) {
                                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                                if (mons != null) {
                                    if (mons.getId() == 8810018) {
                                        damageMonster(chr, mons, monsterhp);
                                    }
                                }
                            }
                        }
                    } else {
                        if (monster.getId() == 8810002 || monster.getId() == 8810003 || monster.getId() == 8810004 || monster.getId() == 8810005 || monster.getId() == 8810006 || monster.getId() == 8810007 || monster.getId() == 8810008 || monster.getId() == 8810009) {
                            Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
                            for (MapleMapObject object : objects) {
                                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                                if (mons != null) {
                                    if (mons.getId() == 8810018) {
                                        damageMonster(chr, mons, damage);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (killMonster) {
                killMonster(monster, chr, true);
            }
            return true;
        }
        return false;
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        killMonster(monster, chr, withDrops, false, 1);
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime) {
        killMonster(monster, chr, withDrops, secondTime, 1);
    }

    @SuppressWarnings("static-access")
    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime, int animation) {
        if (monster.getId() == 8810018 && !secondTime) {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    killMonster(monster, chr, withDrops, true, 1);
                    killAllMonsters(false);
                }
            }, 3000);
            return;
        }
        if (monster.getBuffToGive() > -1) {
            broadcastMessage(MaplePacketCreator.showOwnBuffEffect(monster.getBuffToGive(), 11));
            MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
            MapleStatEffect statEffect = mii.getItemEffect(monster.getBuffToGive());
            synchronized (this.characters) {
                for (MapleCharacter character : this.characters) {
                    if (character.isAlive()) {
                        statEffect.applyTo(character);
                        broadcastMessage(MaplePacketCreator.showBuffeffect(character.getId(), monster.getBuffToGive(), 11, (byte) 3));
                    }
                }
            }
        }
        if (monster.getId() == 8810018) {
            MaplePacket packet = MaplePacketCreator.serverNotice(6, "To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!");
            try {
                chr.getClient().getChannelServer().getInstance(chr.getClient().getChannel()).getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
            } catch (RemoteException e) {
                chr.getClient().getChannelServer().reconnectWorld();
            }
        }
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
        removeMapObject(monster);
        if (monster.getId() >= 8800003 && monster.getId() <= 8800010) {
            boolean makeZakReal = true;
            Collection<MapleMapObject> objects = getMapObjects();
            for (MapleMapObject object : objects) {
                MapleMonster mons = getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        makeZakReal = false;
                    }
                }
            }
            if (makeZakReal) {
                for (MapleMapObject object : objects) {
                    MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                    if (mons != null) {
                        if (mons.getId() == 8800000) {
                            makeMonsterReal(mons);
                            updateMonsterController(mons);
                        }
                    }
                }
            }
        }
        MapleCharacter dropOwner = monster.killBy(chr);
        if (withDrops && !monster.dropsDisabled()) {
            if (dropOwner == null) {
                dropOwner = chr;
            }
            dropFromMonster(dropOwner, monster);
        }
    }

    public void killAllMonsters(boolean drop) {
        List<MapleMapObject> players = null;
        if (drop) {
            players = getAllPlayer();
        }
        List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
            if (drop) {
                int random = (int) Math.random() * (players.size());
                dropFromMonster((MapleCharacter) players.get(random), monster);
            }
        }
    }

    public void killMonster(int monsId) {
        for (MapleMapObject mmo : getMapObjects()) {
            if (mmo instanceof MapleMonster) {
                if (((MapleMonster) mmo).getId() == monsId) {
                    this.killMonster((MapleMonster) mmo, (MapleCharacter) getAllPlayer().get(0), false);
                }
            }
        }
    }

    public List<MapleMapObject> getAllPlayer() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public void destroyReactor(int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        TimerManager tMan = TimerManager.getInstance();
        broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);
        if (reactor.getDelay() > 0) {
            tMan.schedule(new Runnable() {

                @Override
                public void run() {
                    respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }

    public void resetReactors() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 0);
                    ((MapleReactor) o).setTimerActive(false);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 0));
                }
            }
        }
    }

    public void shuffleReactors() {
        List<Point> points = new ArrayList<Point>();
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    points.add(((MapleReactor) o).getPosition());
                }
            }
            Collections.shuffle(points);
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
                }
            }
        }
    }

    public void killAllMonster() {
        killAllmonster();
    }

    public void killAllMonsters() {
        List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            removeMapObject(monster);
        }
    }

    public MapleReactor getReactorById(int Id) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getId() == Id) {
                        return (MapleReactor) obj;
                    }
                }

            }
            return null;
        }
    }

    public void killAllMonster(MapleClient c) {
        killAllmonster(c);
    }

    public void killAllmonster(MapleClient c) {
        List<MapleMapObject> monsters = getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            killMonster(monster, c.getPlayer(), false);
        }
    }

    public void killAllmonster() {
        List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
        }
    }

    public void killAllMonster(boolean drop) {
        List<MapleMapObject> players = null;
        if (drop) {
            players = getAllPlayer();
        }
        List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
            if (drop) {
                int random = (int) (Math.random() * (playerCount() - 0)) + 0;
                dropFromMonster((MapleCharacter) players.get(random), monster);
            }
        }
    }

    public int playerCount() {
        List<MapleMapObject> players = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
        int count = players.size();
        return count;
    }

    /**
     * Automagically finds a new controller for the given monster from the chars on the map...
     *
     * @param monster
     */
    public void updateMonsterController(MapleMonster monster) {
        synchronized (monster) {
            if (!monster.isAlive()) {
                return;
            }
            if (monster.getController() != null) {
                // monster has a controller already, check if he's still on this map
                if (monster.getController().getMap() != this) {
                    log.warn("Monstercontroller wasn't on same map");
                    monster.getController().stopControllingMonster(monster);
                } else {
                    // controller is on the map, monster has an controller, everything is fine
                    return;
                }
            }
            int mincontrolled = -1;
            MapleCharacter newController = null;
            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
                        if (!chr.getName().equals("FaekChar")) { // TODO remove me for production release
                            mincontrolled = chr.getControlledMonsters().size();
                            newController = chr;
                        }
                    }
                }
            }
            if (newController != null) { // was a new controller found? (if not no one is on the map)
                if (monster.isFirstAttack()) {
                    newController.controlMonster(monster, true);
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                } else {
                    newController.controlMonster(monster, false);
                }
            }
        }
    }

    public Collection<MapleMapObject> getMapObjects() {
        return Collections.unmodifiableCollection(mapobjects.values());
    }

    public boolean containsNPC(int npcid) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    if (((MapleNPC) obj).getId() == npcid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public MapleMapObject getMapObject(int oid) {
        return mapobjects.get(oid);
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns null
     *
     * @param oid
     * @return
     */
    public MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.MONSTER) {
            return (MapleMonster) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.REACTOR) {
            return (MapleReactor) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByName(String name) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getName().equals(name)) {
                        return (MapleReactor) obj;
                    }
                }
            }
        }
        return null;
    }
    //backwards compatible

    public void spawnMonsterOnGroudBelow(MapleMonster mob, Point pos) {
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        mob.setPosition(spos);
        spawnMonster(mob);
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    public void spawnRevives(final MapleMonster monster) {
        monster.setMap(this);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnMonster(monster, false));
                }
            });
            updateMonsterController(monster);
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonster(final MapleMonster monster) {
        monster.setMap(this);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));

                    Random rand = new Random();

                    if (monster.getRemoveAfter() > 0) {
                        ScheduledFuture<?> scheduleDeath = TimerManager.getInstance().schedule(new Runnable() {

                            @Override
                            public void run() {
                                if (monster != null) {
                                    killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false, false, 3);
                                }
                            }
                        }, monster.getRemoveAfter());
                    }
                }
            }, null);
            updateMonsterController(monster);
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        try {
            monster.setMap(this);
            Point spos = new Point(pos.x, pos.y - 1);
            spos = calcPointBelow(spos);
            spos.y -= 1;
            monster.setPosition(spos);
            monster.disableDrops();
            synchronized (this.mapobjects) {
                spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                    public void sendPackets(MapleClient c) {
                        c.getSession().write(MaplePacketCreator.spawnMonster(monster, true, effect));
                    }
                });
                updateMonsterController(monster);
            }
            spawnedMonstersOnMap.incrementAndGet();
        } catch (Exception e) {
        }
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnFakeMonster(monster, 0));
                }
            });
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void makeMonsterReal(final MapleMonster monster) {
        monster.setFake(false);
        broadcastMessage(MaplePacketCreator.makeMonsterReal(monster));
        updateMonsterController(monster);
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(reactor.makeSpawnData());
                }
            });
        }
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public void spawnDoor(final MapleDoor door) {
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnDoor(door.getOwner().getId(), door.getTargetPosition(), false));
                    if (door.getOwner().getParty() != null && (door.getOwner() == c.getPlayer() || door.getOwner().getParty().containsMembers(new MaplePartyCharacter(c.getPlayer())))) {
                        c.getSession().write(MaplePacketCreator.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                    }
                    c.getSession().write(MaplePacketCreator.spawnPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
            }, new SpawnCondition() {

                public boolean canSpawn(MapleCharacter chr) {
                    return chr.getMapId() == door.getTarget().getId() ||
                            chr == door.getOwner() && chr.getParty() == null;
                }
            });
        }
    }

    public List<MapleCharacter> getPlayersInRange(Rectangle box, List<MapleCharacter> chr) {
        List<MapleCharacter> character = new LinkedList<MapleCharacter>();
        for (MapleCharacter a : characters) {
            if (chr.contains(a.getClient().getPlayer())) {
                if (box.contains(a.getPosition())) {
                    character.add(a);
                }
            }
        }
        return character;
    }

    public void spawnSummon(final MapleSummon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                int skillLevel = summon.getOwner().getSkillLevel(SkillFactory.getSkill(summon.getSkill()));
                c.getSession().write(MaplePacketCreator.spawnSpecialMapObject(summon, skillLevel, true));
            }
        });
    }

    public void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake) {
        addMapObject(mist);
        broadcastMessage(fake ? mist.makeFakeSpawnData(30) : mist.makeSpawnData());
        TimerManager tMan = TimerManager.getInstance();
        final ScheduledFuture<?> poisonSchedule;
        if (poison) {
            Runnable poisonTask = new Runnable() {

                @Override
                public void run() {
                    List<MapleMapObject> affectedMonsters = getMapObjectsInBox(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                    for (MapleMapObject mo : affectedMonsters) {
                        if (mist.makeChanceResult()) {
                            MonsterStatusEffect poisonEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), false);
                            ((MapleMonster) mo).applyStatus(mist.getOwner(), poisonEffect, true, duration);
                        }
                    }
                }
            };
            poisonSchedule = tMan.register(poisonTask, 2000, 2500);
        } else {
            poisonSchedule = null;
        }
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                broadcastMessage(mist.makeDestroyData());
            }
        }, duration);
    }

    public void disappearingItemDrop(final MapleMapObject dropper,
            final MapleCharacter owner, final IItem item, Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, 0, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
    }

    public void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean expire) {
        TimerManager tMan = TimerManager.getInstance();
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner);
        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0 : owner.getId(),
                        dropper.getPosition(), droppos, (byte) 1));
            }
        });
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0
                : owner.getId(), dropper.getPosition(), droppos, (byte) 0), drop.getPosition());
        if (expire) {
            tMan.schedule(new ExpireMapItemJob(drop), dropLife);
        }
        activateItemReactors(drop);
    }

    private class TimerDestroyWorker implements Runnable {

        @Override
        public void run() {
            if (mapTimer != null) {
                int warpMap = mapTimer.warpToMap();
                int minWarp = mapTimer.minLevelToWarp();
                int maxWarp = mapTimer.maxLevelToWarp();
                mapTimer = null;
                if (warpMap != -1) {
                    MapleMap map2wa2 = ChannelServer.getInstance(channel).getMapFactory().getMap(warpMap);
                    String warpmsg = "You will now be warped to " + map2wa2.getStreetName() + " : " + map2wa2.getMapName();
                    broadcastMessage(MaplePacketCreator.serverNotice(6, warpmsg));
                    for (MapleCharacter chr : getCharacters()) {
                        try {
                            if (chr.getLevel() >= minWarp && chr.getLevel() <= maxWarp) {
                                chr.changeMap(map2wa2, map2wa2.getPortal(0));
                            } else {
                                chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You are not at least level " + minWarp + " or you are higher than level " + maxWarp + "."));
                            }
                        } catch (Exception ex) {
                            String errormsg = "There was a problem warping you. Please contact a GM";
                            chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, errormsg));
                        }
                    }
                }
            }
        }
    }

    public void addMapTimer(int duration) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, -1, -1, -1);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void addMapTimer(int duration, int mapToWarpTo) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, 0, 256);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void addMapTimer(int duration, int mapToWarpTo, int minLevelToWarp) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, minLevelToWarp, 256);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void addMapTimer(int duration, int mapToWarpTo, int minLevelToWarp, int maxLevelToWarp) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, minLevelToWarp, maxLevelToWarp);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void clearMapTimer() {
        if (mapTimer != null) {
            mapTimer.getSF0F().cancel(true);
        }
        mapTimer = null;
    }

    private void activateItemReactors(MapleMapItem drop) {
        IItem item = drop.getItem();
        final TimerManager tMan = TimerManager.getInstance();
        //check for reactors on map that might use this item
        for (MapleMapObject o : mapobjects.values()) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                if (((MapleReactor) o).getReactorType() == 100) {
                    if (((MapleReactor) o).getReactItem().getLeft() == item.getItemId() && ((MapleReactor) o).getReactItem().getRight() <= item.getQuantity()) {
                        Rectangle area = ((MapleReactor) o).getArea();
                        if (area.contains(drop.getPosition())) {
                            MapleClient ownerClient = null;
                            if (drop.getOwner() != null) {
                                ownerClient = drop.getOwner().getClient();
                            }
                            MapleReactor reactor = (MapleReactor) o;
                            if (!reactor.isTimerActive()) {
                                tMan.schedule(new ActivateItemReactor(drop, reactor, ownerClient), 5000);
                                reactor.setTimerActive(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public void AriantPQStart() {
        int i = 1;
        for (MapleCharacter chars2 : this.getCharacters()) {
            broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false));
            broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false).toString()));
            if (this.getCharacters().size() > i) {
                broadcastMessage(MaplePacketCreator.updateAriantPQRanking(null, 0, true));
                broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, true).toString()));
            }
            i++;
        }
    }

    public void spawnMesoDrop(final int meso, final int displayMeso, Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean ffaLoot) {
        TimerManager tMan = TimerManager.getInstance();
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, displayMeso, droppos, dropper, owner);
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropMesoFromMapObject(displayMeso, mdrop.getObjectId(), dropper.getObjectId(),
                        ffaLoot ? 0 : owner.getId(), dropper.getPosition(), droppos, (byte) 1));
            }
        });
        tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
    }

    public void startMapEffect(String msg, int itemId) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, 30000);
    }

    /**
     * Adds a player to this map and sends nescessary data
     *
     * @param chr
     */
    public void addPlayer(MapleCharacter chr) {
        synchronized (characters) {
            this.characters.add(chr);
        }
        synchronized (this.mapobjects) {
            if (!chr.isHidden()) {
                broadcastMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
                MaplePet[] pets = chr.getPets();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        broadcastMessage(chr, MaplePacketCreator.showPet(chr, pets[i], false, false), false);
                    }
                }
                if (chr.getChalkboard() != null) {
                    broadcastMessage(chr, (MaplePacketCreator.useChalkboard(chr, false)), false);
                }
            }
            sendObjectPlacement(chr.getClient());
            if (this.getId() == 1 || this.getId() == 2 || this.getId() == 809000101 || this.getId() == 809000201) {
                chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect());
            }
            MaplePet[] pets = chr.getPets();
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.showPet(chr, pets[i], false, false));
                }
            }
            if (chr.getChalkboard() != null) {
                chr.getClient().getSession().write((MaplePacketCreator.useChalkboard(chr, false)));
            }
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        }

        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE map = ?");
            ps.setInt(1, chr.getMapId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                chr.getClient().getSession().write(MaplePacketCreator.getPlayerNPC(rs.getInt("id")));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            //Dont swallow please
        }

        if (chr.getPlayerShop() != null) {
            addMapObject(chr.getPlayerShop());
        }

        MapleStatEffect summonStat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        if (summonStat != null) {
            MapleSummon summon = chr.getSummons().get(summonStat.getSourceId());
            summon.setPosition(chr.getPosition());
            summon.sendSpawnData(chr.getClient());
            chr.addVisibleMapObject(summon);
            addMapObject(summon);
            chr.getMap().spawnSummon(summon);
            updateMapObjectVisibility(chr, summon);
        }

        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }

        if (getTimeLimit() > 0 && getForcedReturnMap() != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock(getTimeLimit()));
            chr.startMapTimeLimitTask(this, this.getForcedReturnMap());
        }

        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
        }

        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            chr.getClient().getSession().write((MaplePacketCreator.getClockTime(hour, min, second)));
        }

        if (hasBoat() == 2) {
            chr.getClient().getSession().write((MaplePacketCreator.boatPacket(true)));
        } else if (hasBoat() == 1 && (chr.getMapId() != 200090000 || chr.getMapId() != 200090010)) {
            chr.getClient().getSession().write(MaplePacketCreator.boatPacket(false));
        }

        chr.receivePartyMemberHP();
    }

    public void removePlayer(MapleCharacter chr) {
        //log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });
        synchronized (characters) {
            characters.remove(chr);
        }
        removeMapObject(Integer.valueOf(chr.getObjectId()));
        broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        for (MapleMonster monster : chr.getControlledMonsters()) {
            monster.setController(null);
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
            updateMonsterController(monster);
        }
        chr.leaveMap();
        chr.cancelMapTimeLimitTask();

        for (MapleSummon summon : chr.getSummons().values()) {
            if (summon.isPuppet()) {
                chr.cancelBuffStats(MapleBuffStat.PUPPET);
            } else {
                chr.cancelBuffStats(MapleBuffStat.SUMMON);
//removeMapObject(summon);
            }
        }
    }

    public void broadcastMessage(MaplePacket packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource, boolean ranged) {
        broadcastMessage(repeatToSource ? null : source, packet, ranged ? MapleCharacter.MAX_VIEW_RANGE_SQ : Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MaplePacket packet, Point rangedFrom) {
        broadcastMessage(null, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom);
    }

    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param source
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, Point rangedFrom) {
        broadcastMessage(source, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom);
    }

    private void broadcastMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isfake) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    private boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case PLAYER:
            case MIST:
                //case REACTOR:
                return true;
        }
        return false;
    }

    private void sendObjectPlacement(MapleClient mapleClient) {
        for (MapleMapObject o : mapobjects.values()) {
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(mapleClient);
            } else if (o.getType() == MapleMapObjectType.MONSTER) {
                updateMonsterController((MapleMonster) o);
            }
        }
        MapleCharacter chr = mapleClient.getPlayer();

        if (chr != null) {
            for (MapleMapObject o : getMapObjectsInRange(chr.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ, rangedMapobjectTypes)) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).isAlive()) {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                } else {
                    o.sendSpawnData(chr.getClient());
                    chr.addVisibleMapObject(o);
                }
            }
        } else {
            log.info("sendObjectPlacement invoked with null char");
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        for (MapleMapObject l : mapobjects.values()) {
            if (types.contains(l.getType())) {
                if (from.distanceSq(l.getPosition()) <= rangeSq) {
                    ret.add(l);
                }
            }
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInBox(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (l.getType() == MapleMapObjectType.ITEM) {
                    if (from.distanceSq(l.getPosition()) <= rangeSq) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public void addPortal(MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(String portalname) {
        for (MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public MaplePortal getPortal(int portalid) {
        return portals.get(portalid);
    }

    public void addMapleArea(Rectangle rec) {
        areas.add(rec);
    }

    public List<Rectangle> getAreas() {
        return new ArrayList<Rectangle>(areas);
    }

    public Rectangle getArea(int index) {
        return areas.get(index);
    }

    public void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public MapleFootholdTree getFootholds() {
        return footholds;
    }

    /**
     * not threadsafe, please synchronize yourself
     *
     * @param monster
     */
    public void addMonsterSpawn(MapleMonster monster, int mobTime) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime);
        monsterSpawn.add(sp);
        if (sp.shouldSpawn() || mobTime == -1) { // -1 does not respawn and should not either but force ONE spawn
            sp.spawnMonster(this);
        }
    }

    public float getMonsterRate() {
        return monsterRate;
    }

    public Collection<MapleCharacter> getCharacters() {
        return Collections.unmodifiableCollection(this.characters);
    }

    public MapleCharacter getCharacterById(int id) {
        for (MapleCharacter c : this.characters) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (chr.isfake) {
            return;
        }
        if (!chr.isMapObjectVisible(mo)) { // monster entered view range
            if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= MapleCharacter.MAX_VIEW_RANGE_SQ) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else { // monster left view range
            if (mo.getType() != MapleMapObjectType.SUMMON && (mo.getPosition().distanceSq(chr.getPosition()) > MapleCharacter.MAX_VIEW_RANGE_SQ)) {
                chr.removeVisibleMapObject(mo);
                mo.sendDestroyData(chr.getClient());
            }
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, monster);
            }
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        if (player.isfake) {
            return;
        }
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);
        for (MapleMapObject mo : visibleObjectsNow) {
            if (mapobjects.get(mo.getObjectId()) == mo) {
                updateMapObjectVisibility(player, mo);
            } else {
                player.removeVisibleMapObject(mo);
            }
        }
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ,
                rangedMapobjectTypes)) {
            if (!player.isMapObjectVisible(mo)) {
                mo.sendSpawnData(player.getClient());
                player.addVisibleMapObject(mo);
            }
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public void spawnDebug(MessageCallback mc) {
        mc.dropMessage("Spawndebug...");
        synchronized (mapobjects) {
            mc.dropMessage("Mapobjects in map: " + mapobjects.size() + " \"spawnedMonstersOnMap\": " +
                    spawnedMonstersOnMap + " spawnpoints: " + monsterSpawn.size() +
                    " maxRegularSpawn: " + getMaxRegularSpawn());
            int numMonsters = 0;
            for (MapleMapObject mo : mapobjects.values()) {
                if (mo instanceof MapleMonster) {
                    numMonsters++;
                }
            }
            mc.dropMessage("actual monsters: " + numMonsters);
        }
    }

    private int getMaxRegularSpawn() {
        return (int) (monsterSpawn.size() / monsterRate);
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setClock(boolean hasClock) {
        this.clock = hasClock;
    }

    public boolean hasClock() {
        return clock;
    }

    public void setTown(boolean isTown) {
        this.town = isTown;
    }

    public boolean isTown() {
        return town;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public boolean getEverlast() {
        return everlast;
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    private class ExpireMapItemJob implements Runnable {

        private MapleMapItem mapitem;

        public ExpireMapItemJob(MapleMapItem mapitem) {
            this.mapitem = mapitem;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0),
                            mapitem.getPosition());
                    MapleMap.this.removeMapObject(mapitem);
                    mapitem.setPickedUp(true);
                }
            }
        }
    }

    private class ActivateItemReactor implements Runnable {

        private MapleMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    TimerManager tMan = TimerManager.getInstance();
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0),
                            mapitem.getPosition());
                    MapleMap.this.removeMapObject(mapitem);
                    reactor.hitReactor(c);
                    reactor.setTimerActive(false);
                    //reactor.increaseState();
                    if (reactor.getDelay() > 0) { //This shit is negative.. Fix?
                        tMan.schedule(new Runnable() {

                            @Override
                            public void run() {
                                reactor.setState((byte) 0);
                                broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
                            }
                        }, reactor.getDelay());
                    }
                }
            }
        }
    }

    private class RespawnWorker implements Runnable {

        @Override
        public void run() {
            int playersOnMap = characters.size();
            if (playersOnMap == 0) {
                return;
            }
            int ispawnedMonstersOnMap = spawnedMonstersOnMap.get();
            int getMaxSpawn = getMaxRegularSpawn() * 2;

            int numShouldSpawn = getMaxSpawn - ispawnedMonstersOnMap;
            if (numShouldSpawn + ispawnedMonstersOnMap >= getMaxSpawn) {
                numShouldSpawn = getMaxSpawn - ispawnedMonstersOnMap;
            }
            if (numShouldSpawn <= 0) {
                return;
            }
            List<SpawnPoint> randomSpawn = new ArrayList<SpawnPoint>(monsterSpawn);
            Collections.shuffle(randomSpawn);
            int spawned = 0;
            for (SpawnPoint spawnPoint : randomSpawn) {
                if (spawnPoint.shouldSpawn()) {
                    spawnPoint.spawnMonster(MapleMap.this);
                    spawned++;
                }
                if (spawned >= numShouldSpawn) {
                    break;
                }
            }
        }
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    private static interface SpawnCondition {

        boolean canSpawn(MapleCharacter chr);
    }

    public int getHPDec() {
        return decHP;
    }

    public void setHPDec(int delta) {
        decHP = delta;
    }

    public int getHPDecProtect() {
        return this.protectItem;
    }

    public void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }

    public int hasBoat() {
        if (boat && docked) {
            return 2;
        } else if (boat) {
            return 1;
        }
        return 0;
    }

    public void setBoat(boolean hasBoat) {
        this.boat = hasBoat;
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }
}