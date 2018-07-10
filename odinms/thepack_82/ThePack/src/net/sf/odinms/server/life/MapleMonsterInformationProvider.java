package net.sf.odinms.server.life;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.odinms.database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matze
 */

public class MapleMonsterInformationProvider {
	public static class DropEntry {
		public DropEntry(int itemId, int chance) {
			this.itemId = itemId;
			this.chance = chance;
		}
		
		public int itemId;
		public int chance;
		public int assignedRangeStart;
		public int assignedRangeLength;
		
		@Override
		public String toString() {
			return itemId + " chance: " + chance;
		}
	}
	
	public static final int APPROX_FADE_DELAY = 90;
	private static MapleMonsterInformationProvider instance = null;
	private Map<Integer,List<DropEntry>> drops = new HashMap<Integer, List<DropEntry>>();
	private static final Logger log = LoggerFactory.getLogger(MapleMonsterInformationProvider.class);
	
	private MapleMonsterInformationProvider() {
	}
	
	public static MapleMonsterInformationProvider getInstance() {
		if (instance == null) instance = new MapleMonsterInformationProvider();
		return instance;
	}
	
	public List<DropEntry> retrieveDropChances(int monsterId) {
		if (drops.containsKey(monsterId)) return drops.get(monsterId);
		List<DropEntry> ret = new LinkedList<DropEntry>();
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT itemid, chance, monsterid FROM monsterdrops WHERE (monsterid = ? AND chance >= 0) OR (monsterid <= 0)");
			ps.setInt(1, monsterId);
			ResultSet rs = ps.executeQuery();
			MapleMonster theMonster = null;
			while (rs.next()) {
				int rowMonsterId = rs.getInt("monsterid");
				int chance = rs.getInt("chance");
				if (rowMonsterId != monsterId && rowMonsterId != 0) {
					if (theMonster == null) {
						theMonster = MapleLifeFactory.getMonster(monsterId);
					}
					chance += theMonster.getLevel() * rowMonsterId;
				}
				ret.add(new DropEntry(rs.getInt("itemid"), chance));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			log.error("Error retrieving drop", e);
		}
		drops.put(monsterId, ret);
		return ret;
	}
	
	public void clearDrops() {
		drops.clear();
	}
}
