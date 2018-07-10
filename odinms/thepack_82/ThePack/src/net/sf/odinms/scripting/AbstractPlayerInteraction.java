package net.sf.odinms.scripting;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.InventoryException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;

public class AbstractPlayerInteraction {

    public MapleClient c;

    public AbstractPlayerInteraction(MapleClient c) {
        this.c = c;
    }

    protected MapleClient getClient() {
        return c;
    }

    public MapleCharacter getPlayer() {
        return c.getPlayer();
    }

    public void worldMessage(int type, String message) {
        net.sf.odinms.net.MaplePacket packet = MaplePacketCreator.serverNotice(type, message);
        MapleCharacter chr = c.getPlayer();
        try {
            ChannelServer.getInstance(chr.getClient().getChannel()).getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
        } catch (RemoteException e) {
            chr.getClient().getChannelServer().reconnectWorld();
        }
    }

    public void warp(int map) {
        MapleMap target = getWarpMap(map);
        Random rand = new Random();
        MaplePortal portal = target.getPortal(rand.nextInt(target.getPortals().size())); //generate random portal
        c.getPlayer().changeMap(target, portal);
    }

    public void warp(int map, int portal) {
        MapleMap target = getWarpMap(map);
        c.getPlayer().changeMap(target, target.getPortal(portal));
    }

    public void warp(int map, String portal) {
        MapleMap target = getWarpMap(map);
        c.getPlayer().changeMap(target, target.getPortal(portal));
    }

    private MapleMap getWarpMap(int map) {
        MapleMap target;
        if (getPlayer().getEventInstance() == null) {
            target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(map);
        } else {
            target = getPlayer().getEventInstance().getMapInstance(map);
        }
        return target;
    }

    public MapleMap getMap(int map) {
        return getWarpMap(map);
    }

    public boolean haveItem(int itemid) {
        return haveItem(itemid, 1);
    }

    public boolean haveItem(int itemid, int quantity) {
        return haveItem(itemid, quantity, false, true);
    }

    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        return c.getPlayer().haveItem(itemid, quantity, checkEquipped, greaterOrEquals);
    }

    public boolean canHold(int itemid) {
        return c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public MapleQuestStatus.Status getQuestStatus(int id) {
        return c.getPlayer().getQuest(MapleQuest.getInstance(id)).getStatus();
    }

    public void gainItem(int id, short quantity) {
        gainItem(id, quantity, false);
    }

    public void gainItem(int id) {
        gainItem(id, (short) 1, false);
    }

    public void gainItem(int id, short quantity, boolean randomStats) {
        if (id >= 5000000 && id <= 5000100) {
            MapleInventoryManipulator.addById(c, id, (short) 1, null, null, MaplePet.createPet(id));
        }
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            MapleInventoryType type = ii.getInventoryType(id);
            if (!MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
                c.getSession().write(MaplePacketCreator.serverNotice(1, "Your inventory is full. Please remove an item from your " + type.name() + " inventory."));
                return;
            }
            if (type.equals(MapleInventoryType.EQUIP) && !ii.isThrowingStar(item.getItemId()) && !ii.isBullet(item.getItemId())) {
                if (randomStats) {
                    MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats((Equip) item), "", false);
                } else {
                    MapleInventoryManipulator.addFromDrop(c, (Equip) item, "", false);
                }
            } else {
                MapleInventoryManipulator.addById(c, id, quantity, "");
            }
        } else {
            MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
        }
        c.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }

    public void changeMusic(String songName) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public void gainRandEquip(int id) {
        Equip randequip = MapleItemInformationProvider.getInstance().randomizeStats((Equip) MapleItemInformationProvider.getInstance().getEquipById(id));
        if (id >= 1000000 && id < 2000000) {
            byte newSlot = c.getPlayer().getInventory(MapleInventoryType.EQUIP).addItem(randequip);
            if (newSlot == -1) {
                c.getSession().write(MaplePacketCreator.getInventoryFull());
                c.getSession().write(MaplePacketCreator.getShowInventoryFull());
            } else {
                c.getSession().write(MaplePacketCreator.addInventorySlot(MapleInventoryType.EQUIP, randequip));
                c.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) 1, true));
            }
        } else {
            throw new InventoryException("Trying to randomize something that's not an equip: " + id);
        }
    }
    // default playerMessage and mapMessage to use type 5

    public void playerMessage(String message) {
        playerMessage(5, message);
    }

    public void mapMessage(String message) {
        mapMessage(5, message);
    }

    public void guildMessage(String message) {
        guildMessage(5, message);
    }

    public void playerMessage(int type, String message) {
        c.getSession().write(MaplePacketCreator.serverNotice(type, message));
    }

    public void mapMessage(int type, String message) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void guildMessage(int type, String message) {
        if (getGuild() != null) {
            getGuild().guildMessage(MaplePacketCreator.serverNotice(type, message));
        }
    }

    public MapleGuild getGuild() {
        try {
            return c.getChannelServer().getWorldInterface().getGuild(getPlayer().getGuildId(), null);
        } catch (RemoteException ex) {
            Logger.getLogger(AbstractPlayerInteraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public MapleParty getParty() {
        return (c.getPlayer().getParty());
    }

    public boolean isLeader() {
        return (getParty().getLeader().equals(new MaplePartyCharacter(c.getPlayer())));
    }
    //PQ methods: give items/exp to all party members

    public void givePartyItems(int id, short quantity, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            MapleClient cl = chr.getClient();
            if (quantity >= 0) {
                StringBuilder logInfo = new StringBuilder(cl.getPlayer().getName()+" received "+quantity+" from event "+chr.getEventInstance().getName());
                MapleInventoryManipulator.addById(cl, id, quantity, logInfo.toString());
            } else {
                MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
            }
            cl.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
        }
    }
    //PQ gain EXP: Multiplied by channel rate here to allow global values to be input direct into NPCs

    public void givePartyExp(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.gainExp(amount * c.getChannelServer().getExpRate(), true, true);
        }
    }
    //remove all items of type from party
    //combination of haveItem and gainItem

    public void removeFromParty(int id, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            MapleClient cl = chr.getClient();
            MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(id);
            MapleInventory iv = cl.getPlayer().getInventory(type);
            int possesed = iv.countById(id);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possesed, true, false);
                cl.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possesed, true));
            }
        }
    }

    public void removeAll(int id) {
        removeAll(id, c);
    }

    public void removeAll(int id, MapleClient cl) {
        int possessed = cl.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(id)).countById(id);
        if (possessed > 0) {
            MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false);
            cl.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
        }
    }

    public void gainCloseness(int closeness, int index) {
        MaplePet pet = getPlayer().getPet(index);
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + closeness);
            getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
        }
    }

    public void gainClosenessAll(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet != null) {
                pet.setCloseness(pet.getCloseness() + closeness);
                getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
            }
        }
    }

    public int getMapId() {
        return c.getPlayer().getMap().getId();
    }

    public int getPlayerCount(int mapid) {
        return c.getChannelServer().getMapFactory().getMap(mapid).getCharacters().size();
    }

    public int getCurrentPartyId(int mapid) {
        return getMap(mapid).getCurrentPartyId();
    }

    public void showInstruction(String msg, int width, int height) {
        c.getSession().write(MaplePacketCreator.sendHint(msg, width, height));
    }

    public void resetMap(int mapid) {
        getMap(mapid).resetReactors();
        getMap(mapid).killAllMonsters(false);
        List<MapleMapObject> items = getMap(mapid).getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
        for (MapleMapObject i : items) {
            getMap(mapid).removeMapObject(i);
            getMap(mapid).broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, c.getPlayer().getId()));
        }
    }
}
