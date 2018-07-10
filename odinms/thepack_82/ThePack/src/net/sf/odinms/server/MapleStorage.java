package net.sf.odinms.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IEquip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.database.DatabaseException;
import net.sf.odinms.tools.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matze
 */
public class MapleStorage {

    private int id;
    private List<IItem> items;
    private int meso;
    private byte slots;
    //private Set<MapleInventoryType> updatedTypes = new HashSet<MapleInventoryType>();
    private Map<MapleInventoryType, List<IItem>> typeItems = new HashMap<MapleInventoryType, List<IItem>>();
    private static Logger log = LoggerFactory.getLogger(MapleStorage.class);

    private MapleStorage(int id, byte slots, int meso) {
        this.id = id;
        this.slots = slots;
        this.items = new LinkedList<IItem>();
        this.meso = meso;
    }

    public static MapleStorage create(int id) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO storages (accountid, slots, meso) VALUES (?, ?, ?)");
            ps.setInt(1, id);
            ps.setInt(2, 16);
            ps.setInt(3, 0);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error creating storage", ex);
        }
        return loadOrCreateFromDB(id);
    }

    public static MapleStorage loadOrCreateFromDB(int id) {
        MapleStorage ret = null;
        int storeId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM storages WHERE accountid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return create(id);
            } else {
                storeId = rs.getInt("storageid");
                ret = new MapleStorage(storeId, (byte) rs.getInt("slots"), rs.getInt("meso"));
                rs.close();
                ps.close();
                String sql = "SELECT * FROM inventoryitems " + "LEFT JOIN inventoryequipment USING (inventoryitemid) " + "WHERE storageid = ?";
                ps = con.prepareStatement(sql);
                ps.setInt(1, storeId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    MapleInventoryType type = MapleInventoryType.getByType((byte) rs.getInt("inventorytype"));
                    if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
                        int itemid = rs.getInt("itemid");
                        Equip equip = new Equip(itemid, (byte) rs.getInt("position"), rs.getInt("ringid"));
                        equip.setOwner(rs.getString("owner"));
                        equip.setQuantity((short) rs.getInt("quantity"));
                        equip.setAcc((short) rs.getInt("acc"));
                        equip.setAvoid((short) rs.getInt("avoid"));
                        equip.setDex((short) rs.getInt("dex"));
                        equip.setHands((short) rs.getInt("hands"));
                        equip.setHp((short) rs.getInt("hp"));
                        equip.setInt((short) rs.getInt("int"));
                        equip.setJump((short) rs.getInt("jump"));
                        equip.setLuk((short) rs.getInt("luk"));
                        equip.setMatk((short) rs.getInt("matk"));
                        equip.setMdef((short) rs.getInt("mdef"));
                        equip.setMp((short) rs.getInt("mp"));
                        equip.setSpeed((short) rs.getInt("speed"));
                        equip.setStr((short) rs.getInt("str"));
                        equip.setWatk((short) rs.getInt("watk"));
                        equip.setWdef((short) rs.getInt("wdef"));
                        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                        equip.setLocked((byte) rs.getInt("locked"));
                        equip.setLevel((byte) rs.getInt("level"));
                        ret.items.add(equip);
                    } else {
                        Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                        item.setOwner(rs.getString("owner"));
                        ret.items.add(item);
                    }
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException ex) {
            log.error("Error loading storage", ex);
        }
        return ret;
    }

    public void saveToDB() {
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?");
            ps.setInt(1, slots);
            ps.setInt(2, meso);
            ps.setInt(3, id);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM inventoryitems WHERE storageid = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO inventoryitems (storageid, itemid, inventorytype, position, quantity, owner) VALUES (?, ?, ?, ?, ?, ?)");
            PreparedStatement pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            MapleInventoryType type;
            for (IItem item : items) {
                ps.setInt(1, id);
                ps.setInt(2, item.getItemId());
                type = ii.getInventoryType(item.getItemId());
                ps.setInt(3, type.getType());
                ps.setInt(4, item.getPosition());
                ps.setInt(5, item.getQuantity());
                ps.setString(6, item.getOwner());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                int itemid;
                if (rs.next()) {
                    itemid = rs.getInt(1);
                } else {
                    throw new DatabaseException("Inserting char failed.");
                }
                if (type.equals(MapleInventoryType.EQUIP)) {
                    pse.setInt(1, itemid);
                    IEquip equip = (IEquip) item;
                    pse.setInt(2, equip.getUpgradeSlots());
                    pse.setInt(3, equip.getLevel());
                    pse.setInt(4, equip.getStr());
                    pse.setInt(5, equip.getDex());
                    pse.setInt(6, equip.getInt());
                    pse.setInt(7, equip.getLuk());
                    pse.setInt(8, equip.getHp());
                    pse.setInt(9, equip.getMp());
                    pse.setInt(10, equip.getWatk());
                    pse.setInt(11, equip.getMatk());
                    pse.setInt(12, equip.getWdef());
                    pse.setInt(13, equip.getMdef());
                    pse.setInt(14, equip.getAcc());
                    pse.setInt(15, equip.getAvoid());
                    pse.setInt(16, equip.getHands());
                    pse.setInt(17, equip.getSpeed());
                    pse.setInt(18, equip.getJump());
                    pse.setInt(19, equip.getRingId());
                    pse.setInt(20, equip.getLocked());
                    pse.executeUpdate();
                }
            }
            ps.close();
            pse.close();
        } catch (SQLException ex) {
            log.error("Error saving storage", ex);
        }
    }

    public IItem takeOut(byte slot) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem ret = items.remove(slot);
        MapleInventoryType type = ii.getInventoryType(ret.getItemId());
        typeItems.put(type, new ArrayList<IItem>(filterItems(type)));
        return ret;
    }

    public void store(IItem item) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        items.add(item);
        MapleInventoryType type = ii.getInventoryType(item.getItemId());
        typeItems.put(type, new ArrayList<IItem>(filterItems(type)));
    }

    public List<IItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private List<IItem> filterItems(MapleInventoryType type) {
        List<IItem> ret = new LinkedList<IItem>();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (IItem item : items) {
            if (ii.getInventoryType(item.getItemId()) == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) {
        // MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte ret = 0;
        for (IItem item : items) {
            if (item == typeItems.get(type).get(slot)) {
                return ret;
            }
            ret++;
        }
        return -1;
    }

    public void sendStorage(MapleClient c, int npcId) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        // sort by inventorytype to avoid confusion
        Collections.sort(items, new Comparator<IItem>() {

            public int compare(IItem o1, IItem o2) {
                if (ii.getInventoryType(o1.getItemId()).getType() < ii.getInventoryType(o2.getItemId()).getType()) {
                    return -1;
                } else if (ii.getInventoryType(o1.getItemId()) == ii.getInventoryType(o2.getItemId())) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, new ArrayList<IItem>(items));
        }
        c.getSession().write(MaplePacketCreator.getStorage(npcId, slots, items, meso));
    }

    public void sendStored(MapleClient c, MapleInventoryType type) {
        c.getSession().write(MaplePacketCreator.storeStorage(slots, type, typeItems.get(type)));
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) {
        c.getSession().write(MaplePacketCreator.takeOutStorage(slots, type, typeItems.get(type)));
    }

    public int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        if (meso < 0) {
            throw new RuntimeException();
        }
        this.meso = meso;
    }

    public void sendMeso(MapleClient c) {
        c.getSession().write(MaplePacketCreator.mesoStorage(slots, meso));
    }

    public boolean isFull() {
        return items.size() >= slots;
    }

    public void close() {
        typeItems.clear();
    }
}
