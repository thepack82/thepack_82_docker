package net.sf.odinms.scripting.npc;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.scripting.AbstractPlayerInteraction;
import net.sf.odinms.scripting.event.EventManager;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.server.MapleSquad;
import net.sf.odinms.server.MapleSquadType;
import net.sf.odinms.server.maps.MapleMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import net.sf.odinms.client.ExpTable;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleMonsterStats;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.maps.MapleMapFactory;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.Pair;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private MapleClient c;
    private int npc;
    private String getText;
    private ChannelServer cserv;
    private MapleCharacter chr;

    public NPCConversationManager(MapleClient c, int npc) {
        super(c);
        this.c = c;
        this.npc = npc;
    }

    public NPCConversationManager(MapleClient c, int npc, MapleCharacter chr) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.chr = chr;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
    }

    public void sendNext(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01"));
    }

    public void sendPrev(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00"));
    }

    public void sendNextPrev(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01"));
    }

    public void sendOk(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00"));
    }

    public void sendYesNo(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, ""));
    }

    public void sendAcceptDecline(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, ""));
    }

    public void sendSimple(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, ""));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalkText(npc, text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public void closePortal(int mapid, String pName) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).getPortal(pName).setPortalState(false);
    }

    public void openPortal(int mapid, String pName) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).getPortal(pName).setPortalState(true);
    }

    public void makeRing(String partner, int ringId) {
        net.sf.odinms.client.MapleRing.createRing(c, ringId, c.getPlayer().getId(), c.getPlayer().getName(), c.getChannelServer().getPlayerStorage().getCharacterByName(partner).getId(), partner);
    }

    public void closeDoor(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).setReactorState();
    }

    public void openDoor(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void openShop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(getClient());
    }

    public void openNpc(int id) {
        dispose();
        NPCScriptManager.getInstance().start(getClient(), id, null, null);
    }

    public boolean isPartyAllHere() {
        int num = 0;
        int myparty = 0;
        while (getPlayer().getParty().getMembers().iterator().hasNext()) {
            MaplePartyCharacter curChar = getPlayer().getParty().getMembers().iterator().next();
            if (curChar.isOnline()) {
                num += 1;
            }
        }
        while (getPlayer().getMap().getCharacters().iterator().hasNext()) {
            MapleCharacter curChar2 = getPlayer().getMap().getCharacters().iterator().next();
            if (curChar2.getParty() == getPlayer().getParty()) {
                myparty += 1;
            }
        }
        return num == myparty;
    }

    public void changeJob(MapleJob job) {
        getPlayer().changeJob(job);
    }

    public MapleJob getJob() {
        return getPlayer().getJob();
    }

    public void startQuest(int id) {
        MapleQuest.getInstance(id).start(getPlayer(), npc);
    }

    public void completeQuest(int id) {
        MapleQuest.getInstance(id).complete(getPlayer(), npc);
    }

    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    /**
     * use getPlayer().getMeso() instead
     * @return
     */
    @Deprecated
    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain, true, false, true);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getNpc() {
        return npc;
    }

    /**
     * use getPlayer().getLevel() instead
     * @return
     */
    @Deprecated
    public int getLevel() {
        return getPlayer().getLevel();
    }

    public void unequipEverything() {
        MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Byte> ids = new LinkedList<Byte>();
        for (IItem item : equipped.list()) {
            ids.add(item.getPosition());
        }
        for (byte id : ids) {
            MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
        }
    }

    public void teachSkill(int id, int level, int masterlevel) {
        getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }

    public void clearSkills() {
        Map<ISkill, MapleCharacter.SkillEntry> skills = getPlayer().getSkills();
        for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            getPlayer().changeSkillLevel(skill.getKey(), 0, 0);
        }
    }

    /**
     * Use getPlayer() instead (for consistency with MapleClient)
     * @return
     */
    @Deprecated
    public MapleCharacter getChar() {
        return getPlayer();
    }

    public MapleClient getC() {
        return getClient();
    }

    public void rechargeStars() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem stars = getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) 1);
        if (ii.isThrowingStar(stars.getItemId()) || ii.isBullet(stars.getItemId())) {
            stars.setQuantity(ii.getSlotMax(getClient(), stars.getItemId()));
            getC().getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) stars));
        }
    }

    public EventManager getEventManager(String event) {
        return getClient().getChannelServer().getEventSM().getEventManager(event);
    }

    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
    }

    public void playSound(String sound) {
        getClient().getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
    }

    @Override
    public String toString() {
        return "Conversation with NPC: " + npc;
    }

    public void updateBuddyCapacity(int capacity) {
        getPlayer().setBuddyCapacity(capacity);
    }

    public int getBuddyCapacity() {
        return getPlayer().getBuddyCapacity();
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(c.getPlayer().getSkinColor().getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public void warpParty(int mapId) {
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void warpPartyWithExp(int mapId, int exp) {
        warpPartyWithExpMeso(mapId, exp, 0);
    }

    public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
                curChar.gainMeso(meso, false); //which pq has mesos?
            }
        }
    }

    public void warpRandom(int mapid) {
        MapleMap target = c.getChannelServer().getMapFactory().getMap(mapid);
        Random rand = new Random();
        getPlayer().changeMap(target, target.getPortal(rand.nextInt(target.getPortals().size())));
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
    }

    public MapleSquad createMapleSquad(MapleSquadType type) {
        MapleSquad squad = new MapleSquad(c.getChannel(), getPlayer());
        if (getSquadState(type) == 0) {
            c.getChannelServer().addMapleSquad(squad, type);
        } else {
            return null;
        }
        return squad;
    }

    public MapleCharacter getSquadMember(MapleSquadType type, int index) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        MapleCharacter ret = null;
        if (squad != null) {
            ret = squad.getMembers().get(index);
        }
        return ret;
    }

    public void giveWonkyBuff(MapleCharacter chr) {
        int Buffs[] = {2022090, 2022091, 2022092, 2022093};
        MapleItemInformationProvider.getInstance().getItemEffect(Buffs[(int) Math.round(Math.random() * 4)]).applyTo((MapleCharacter) chr);
    }

    public int getSquadState(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.getStatus();
        } else {
            return 0;
        }
    }

    public void setSquadState(MapleSquadType type, int state) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.setStatus(state);
        }
    }

    public boolean checkSquadLeader(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.getLeader().getId() == getPlayer().getId();
        } else {
            return false;
        }
    }

    public void removeMapleSquad(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            if (squad.getLeader().getId() == getPlayer().getId()) {
                squad.clear();
                c.getChannelServer().removeMapleSquad(squad, type);
            }
        }
    }

    public int numSquadMembers(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        int ret = 0;
        if (squad != null) {
            ret = squad.getSquadSize();
        }
        return ret;
    }

    public boolean isSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        boolean ret = false;
        if (squad.containsMember(getPlayer())) {
            ret = true;
        }
        return ret;
    }

    public void addSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.addMember(getPlayer());
        }
    }

    public void removeSquadMember(MapleSquadType type, MapleCharacter chr, boolean ban) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(chr, ban);
        }
    }

    public void removeSquadMember(MapleSquadType type, int index, boolean ban) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            MapleCharacter chr = squad.getMembers().get(index);
            squad.banMember(chr, ban);
        }
    }

    public boolean canAddSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return !squad.isBanned(getPlayer());
        }
        return false;
    }

    public void warpSquadMembers(MapleSquadType type, int mapId) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
        if (squad != null) {
            if (checkSquadLeader(type)) {
                for (MapleCharacter chr : squad.getMembers()) {
                    chr.changeMap(map, map.getPortal(0));
                }
            }
        }
    }

    public static boolean makeRing(MapleClient mc, String partner, int ringId) {
        int partnerId = MapleCharacter.getIdByName(partner, 0);
        int[] ret = net.sf.odinms.client.MapleRing.createRing(mc, ringId, mc.getPlayer().getId(), mc.getPlayer().getName(), partnerId, partner);
        return !(ret[0] == -1 || ret[1] == -1);
    }

    public void WarpTo(String player) {
        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(player);
        c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    public boolean hasGuild() {
        boolean goodMap = getPlayer().getMapId() > 910000008 && getPlayer().getMapId() < 910000012;
        boolean goodStat = getPlayer().getStr() > 31 && getPlayer().getDex() > 4 && getPlayer().getInt() > 15;
        return (goodMap && goodStat && getPlayer().getUpdated());
    }

    public void increaseGuildStatus() {
        getPlayer().increaseGuildStatus();
    }

    public void openDuey() {
        c.getSession().write(MaplePacketCreator.sendDueyAction((byte) 8));
    }

    public void resetStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(5);
        int totAp = getPlayer().getRemainingAp() + getPlayer().getStr() + getPlayer().getDex() + getPlayer().getInt() + getPlayer().getLuk();
        getPlayer().setStr(4);
        getPlayer().setDex(4);
        getPlayer().setInt(4);
        getPlayer().setLuk(4);
        getPlayer().setRemainingAp(totAp - 16);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, Integer.valueOf(4)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.DEX, Integer.valueOf(4)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LUK, Integer.valueOf(4)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.INT, Integer.valueOf(4)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(getPlayer().getRemainingAp())));
        getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
    }

    public void changeSex() {
        getPlayer().setGender(1 - getPlayer().getGender());
        getClient().getSession().write(MaplePacketCreator.updateCharLook(getPlayer()));
    }

    public boolean eventOn() {
        return getPlayer().getClient().getChannelServer().eventOn;
    }

    public void doEvent() {
        if (eventOn()) {
            this.warp(getPlayer().getClient().getChannelServer().eventMap);
            getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have been warped to the event map!"));
        }
    }

    public int getPetLevel() {
        for (MaplePet pet : getPlayer().getPets()) {
            return pet.getLevel();
        }
        return 0;
    }

    public int getFullness() {
        for (MaplePet pet : getPlayer().getPets()) {
            return pet.getFullness();
        }
        return 0;
    }

    public String getPetName() {
        for (MaplePet pet : getPlayer().getPets()) {
            return pet.getName();
        }
        return "";
    }

    public void setPetName(String petname) {
        for (MaplePet pet : getPlayer().getPets()) {
            pet.setName(petname);
        }
    }

    public ChannelServer getChannelServer() {
        return cserv;
    }

    public void startQuest(int id, MapleClient cg) {
        MapleQuest.getInstance(id).start(cg.getPlayer(), npc);
    }

    public void completeQuest(int id, MapleClient cg) {
        MapleQuest.getInstance(id).complete(cg.getPlayer(), npc);
    }

    public void forfeitQuest(int id, MapleClient cg) {
        MapleQuest.getInstance(id).forfeit(cg.getPlayer());
    }

    @Override
    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    public void summonMob(int mobid, int customHP, int customEXP, int amount) {
        spawnMonster(mobid, customHP, -1, -1, customEXP, 0, 0, amount, getPlayer().getPosition().x, getPlayer().getPosition().y);
    }

    public void setLevel(int level) {
        getPlayer().setLevel(level);
        getPlayer().updateSingleStat(MapleStat.LEVEL, Integer.valueOf(level));
    }

    public void spawnMonster(int mobid, int HP, int MP, int level, int EXP, int boss, int undead, int amount, int x, int y) {
        MapleMonsterStats newStats = new MapleMonsterStats();
        if (HP >= 0) {
            newStats.setHp(HP);
        }
        if (MP >= 0) {
            newStats.setMp(MP);
        }
        if (level >= 0) {
            newStats.setLevel(level);
        }
        if (EXP >= 0) {
            newStats.setExp(EXP);
        }
        newStats.setBoss(boss == 1);
        newStats.setUndead(undead == 1);
        for (int i = 0; i < amount; i++) {
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
            npcmob.setOverrideStats(newStats);
            npcmob.setHp(npcmob.getMaxHp());
            npcmob.setMp(npcmob.getMaxMp());
            getPlayer().getMap().spawnMonsterOnGroundBelow(npcmob, new Point(x, y));
        }
    }

    public void summonMob(int mobid) {
        getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(mobid), getNPCPosition());
    }

    public void summonMobAtPosition(int mobid, int customHP, int customEXP, int amount, int posx, int posy) {
        MapleMonsterStats newStats = new MapleMonsterStats();
        if (customHP > 0) {
            newStats.setHp(customHP);
        }
        if (customEXP >= 0) {
            newStats.setExp(customEXP);
        }
        if (amount <= 1) {
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
            npcmob.setOverrideStats(newStats);
            npcmob.setHp(npcmob.getMaxHp());
            getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
        } else {
            for (int i = 0; i < amount; i++) {
                MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
                npcmob.setOverrideStats(newStats);
                npcmob.setHp(npcmob.getMaxHp());
                getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
            }
        }
    }

    public void summonMobAtPosition(int mobid, int amount, int posx, int posy) {
        if (amount <= 1) {
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
            npcmob.setHp(npcmob.getMaxHp());
            getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
        } else {
            for (int i = 0; i < amount; i++) {
                MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
                npcmob.setHp(npcmob.getMaxHp());
                getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
            }
        }
    }

    public void killAllMobs() {
        MapleMap map = getPlayer().getMap();
        double range = Double.POSITIVE_INFINITY;
        List<MapleMapObject> monsters = map.getMapObjectsInRange(getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            map.killMonster((MapleMonster) monstermo, getPlayer(), false);
        }
    }

    @Override
    public void changeMusic(String songName) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public void levelUp() {
        getPlayer().levelUp();
        getPlayer().gainExp(-getPlayer().getExp(), false, false);
    }

    public void setAP(int AP) {
        getPlayer().setRemainingAp(AP);
        getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, Integer.valueOf(AP));
    }

    public void gainAP(int g) {
        getPlayer().setRemainingAp(g + getPlayer().getRemainingAp());
        getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, Integer.valueOf(g + getPlayer().getRemainingAp()));
    }

    public void gainReborns(int reborns) {
        getPlayer().setReborns(reborns + getPlayer().getReborns());
    }

    public void setSP(int SP) {
        getPlayer().setRemainingSp(SP);
        getPlayer().updateSingleStat(MapleStat.AVAILABLESP, Integer.valueOf(SP));
    }

    public void setStr(int str) {
        getPlayer().setStr(str);
        getPlayer().updateSingleStat(MapleStat.STR, Integer.valueOf(str));
    }

    public void setDex(int dex) {
        getPlayer().setDex(dex);
        getPlayer().updateSingleStat(MapleStat.DEX, Integer.valueOf(dex));
    }

    public void setInt(int inte) {
        getPlayer().setInt(inte);
        getPlayer().updateSingleStat(MapleStat.INT, Integer.valueOf(inte));
    }

    public void setLuk(int luk) {
        getPlayer().setLuk(luk);
        getPlayer().updateSingleStat(MapleStat.LUK, Integer.valueOf(luk));
    }

    public void spawnMob(int mapid, int mid, int xpos, int ypos) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(mid), new Point(xpos, ypos));
    }

    public boolean isPartyLeader() {
        return getPlayer().isPartyLeader();
    }

    public void setFame(int fame) {
        getPlayer().setFame(fame);
        getPlayer().updateSingleStat(MapleStat.FAME, Integer.valueOf(fame));
    }

    public void gainFame(int fame) {
        if (fame > 0) {
            getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "You have gained fame. (" + fame + ")"));
        } else if (fame < 0) {
            getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "You have lost fame. (" + fame + ")"));
        }
        getPlayer().gainFame(fame);
        getPlayer().updateSingleStat(MapleStat.FAME, Integer.valueOf(getPlayer().getFame()));
    }

    public void setExp(int exp) {
        getPlayer().updateSingleStat(MapleStat.EXP, Integer.valueOf(exp));
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void environmentChange(String env, int mode) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, mode));
    }

    public int getCharsOnMap() {
        return getPlayer().getMap().getCharacters().size();
    }

    public int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public int getMin() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public int getSec() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    public void killAllMonster(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).killAllMonsters(true);
    }

    public void giveBuff(int buff, int level) {
        SkillFactory.getSkill(buff).getEffect(level).applyTo(getPlayer());
    }

    public int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public int getDonatorPoints() {
        return getPlayer().getDonatorPoints();
    }

    public void gainDonatorPoints(int gain) {
        getPlayer().gainDonatorPoints(gain);
    }

    public void setGMLevel(int l) {
        getPlayer().setGMLevel(l);
    }

    public void modifyNX(int amount, int type) {
        getPlayer().modifyCSPoints(type, amount);
        if (amount > 0) {
            getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have gained NX Cash (" + amount + ")."));
        } else {
            getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have lost NX Cash (" + amount + ")."));
        }
    }

    public void reloadChar() {
        getPlayer().getClient().getSession().write(MaplePacketCreator.getCharInfo(getPlayer()));
        getPlayer().getMap().removePlayer(getPlayer());
        getPlayer().getMap().addPlayer(getPlayer());
    }

    public void gainCloseness(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet.getCloseness() < 30000 || pet.getLevel() < 30) {
                if ((pet.getCloseness() + closeness) > 30000) {
                    pet.setCloseness(30000);
                } else {
                    pet.setCloseness(pet.getCloseness() + closeness);
                }
                while (pet.getCloseness() > ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    getClient().getSession().write(MaplePacketCreator.showOwnPetLevelUp(getPlayer().getPetIndex(pet)));
                }
                getPlayer().getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
            }
        }
    }

    public void doReborn() {
        getPlayer().doReborn();
    }

    public Point getNPCPosition() {
        MapleNPC thenpc = MapleLifeFactory.getNPC(this.npc);
        Point pos = thenpc.getPosition();
        return pos;
    }

    public Point getPosition() {
        Point pos = getPlayer().getPosition();
        return pos;
    }

    public void giveItemBuff(int itemId) {
        getPlayer().setItemEffect(itemId);
    }

    public boolean isMorphed() {
        boolean morph = false;
        Integer morphed = getPlayer().getBuffedValue(MapleBuffStat.MORPH);
        if (morphed != null) {
            morph = true;
        }
        return morph;
    }

    public int getMorphValue() { // 1= mushroom, 2= pig, 3= alien, 4= cornian, 5= arab retard
        try {
            int morphid = getPlayer().getBuffedValue(MapleBuffStat.MORPH).intValue();
            return morphid;
        } catch (NullPointerException n) {
            return -1;
        }
    }

    public MapleSquad getSquad(MapleSquadType Type) {
        return c.getChannelServer().getMapleSquad(Type);
    }

    public void setReborns(int r) {
        c.getPlayer().setReborns(r);
    }

    public int getReborns() {
        return c.getPlayer().getReborns();
    }

    public void resetReactors() {
        c.getPlayer().getMap().resetReactors();
    }

    public void resetReactorsFull() {
        c.getPlayer().getMap().killAllMonster();
        c.getPlayer().getMap().resetReactors();
    }

    public void setHp(int hp) {
        c.getPlayer().setHp(hp);
    }

    public void setMp(int mp) {
        c.getPlayer().setMp(mp);
    }

    public void addHp(int hp) {
        c.getPlayer().addHP(hp);
    }

    public void addMp(int mp) {
        c.getPlayer().addMP(mp);
    }

    public void addCloseness(int increase) {
        MaplePet pet = c.getPlayer().getPet(0);
        if (pet.getCloseness() < 30000) {
            int newCloseness = pet.getCloseness() + increase;
            if (newCloseness > 30000) {
                newCloseness = 30000;
            }
            pet.setCloseness(newCloseness);
            if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                pet.setLevel(pet.getLevel() + 1);
                c.getSession().write(MaplePacketCreator.showPetLevelUp(c.getPlayer(), 0));
            }
            c.getSession().write(MaplePacketCreator.updatePet(pet, true));
        }
    }

    public String getName() {
        return c.getPlayer().getName();
    }

    public int getGender() {
        return c.getPlayer().getGender();
    }

    public int countMonster() {
        return c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER)).size();
    }

    public int countReactor() {
        return c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR)).size();
    }

    public int getDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public void giveNPCBuff(MapleCharacter chr, int itemID) {
        MapleItemInformationProvider.getInstance().getItemEffect(itemID).applyTo(chr);
    }

    public void warpAllInMap(int mapid, int portal) {
        MapleMapFactory mapFactory = ChannelServer.getInstance(c.getChannel()).getMapFactory();
        MapleMap outMap = mapFactory.getMap(mapid);
        for (MapleCharacter aaa : outMap.getCharacters()) {
            mapFactory = ChannelServer.getInstance(aaa.getClient().getChannel()).getMapFactory();//hmmm needed?
            aaa.getClient().getPlayer().changeMap(outMap, outMap.getPortal(portal));
            outMap = mapFactory.getMap(mapid);//hmmm needed?
            aaa.getClient().getPlayer().getEventInstance().unregisterPlayer(aaa.getClient().getPlayer());
        }
    }

    public MapleCharacter getCharByName(String namee) {
        try {
            return getClient().getChannelServer().getPlayerStorage().getCharacterByName(namee);
        } catch (Exception e) {
            return null;
        }
    }

    public int getKarma() {
        return getPlayer().getKarma();
    }

    public void gainKarma(int gain) {
        getPlayer().setKarma(getKarma() + gain);
    }
}