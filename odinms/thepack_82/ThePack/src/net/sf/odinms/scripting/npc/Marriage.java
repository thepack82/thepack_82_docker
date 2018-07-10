/*
 * @Author Jvlaple
 */

package net.sf.odinms.scripting.npc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.database.DatabaseConnection;

public class Marriage {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Marriage.class);

	public static void createMarriage(MapleCharacter player, MapleCharacter partner) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO marriages (husbandid, wifeid) VALUES (?, ?)");
			ps.setInt(1, player.getId());
			ps.setInt(2, partner.getId());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			log.warn("Problem marrying " + player.getName() + " and " + partner.getName(), ex);
		}
	}

	public static void createEngagement(MapleCharacter player, MapleCharacter partner) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO engagements (husbandid, wifeid) VALUES (?, ?)");
			ps.setInt(1, player.getId());
			ps.setInt(2, partner.getId());
			ps.executeUpdate();
		} catch (SQLException ex) {
			log.warn("Problem announcing engagement with " + player.getName() + " and " + partner.getName(), ex);
		}
	}

	public static void divorceEngagement(MapleCharacter player, MapleCharacter partner) {
		try {
			Connection con = DatabaseConnection.getConnection();
			int pid = 0;
			if (player.getGender() == 0) {
				pid = player.getId();
			} else {
				pid = partner.getId();
			}
			PreparedStatement get = con.prepareStatement("SELECT FROM engagements WHERE husbandid = ?");
			get.setInt(1, pid);
			ResultSet rs = get.executeQuery();
			PreparedStatement ps = con.prepareStatement("DELETE FROM engagements WHERE husbandid = ?");
			if (rs.next()) {
				ps.setInt(1, pid);
			} else {
				return;
			}
			ps.executeUpdate();
			PreparedStatement ps1 = con.prepareStatement("UPDATE characters SET marriagequest = 0 WHERE id = ?, and WHERE id = ?");
			ps1.setInt(1, player.getId());
			ps1.setInt(2, partner.getId());
			ps1.executeUpdate();
		} catch (SQLException ex) {
			log.warn("Problem breaking engagement with " + player.getName() + " and " + partner.getName(), ex);
		}
	}

	public static void divorceMarriage(MapleCharacter player, MapleCharacter partner) {
		try {
			Connection con = DatabaseConnection.getConnection();
			int pid = 0;
			if (player.getGender() == 0) {
				pid = player.getId();
			} else {
				pid = partner.getId();
			}
			PreparedStatement get = con.prepareStatement("SELECT partnerid FROM characters WHERE id = ?");
			get.setInt(1, player.getId());
			ResultSet rs = get.executeQuery();
			if (rs.next()) {
				PreparedStatement ps = con.prepareStatement("DELETE FROM marriages WHERE husbandid = ?");
				ps.setInt(1, pid);
				ps.executeUpdate();
				PreparedStatement ps1 = con.prepareStatement("UPDATE characters SET married = 0 WHERE id = ?, and WHERE id = ?");
				ps1.setInt(1, player.getId());
				ps1.setInt(2, partner.getId());
				ps1.executeUpdate();
				ps.close();
				ps1.close();
			} else {
				return;
			}
			get.close();
		} catch (SQLException ex) {
			log.warn("Problem divorcing" + player.getName() + " and " + partner.getName(), ex);
		}
	}
}
