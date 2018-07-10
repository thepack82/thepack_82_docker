package net.sf.odinms.net.channel.handler;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import net.sf.odinms.client.BuddylistEntry;
import net.sf.odinms.client.CharacterNameAndId;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.CharacterIdChannelPair;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.PlayerBuffValueHolder;
import net.sf.odinms.net.world.PlayerCoolDownValueHolder;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerLoggedinHandler.class);

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int cid = slea.readInt();
        MapleCharacter player = null;
        try {
            player = MapleCharacter.loadCharFromDB(cid, c, true);
            c.setPlayer(player);
        } catch (SQLException e) {
            log.error("Loading the char failed", e);
        }
        c.setAccID(player.getAccountID());
        int state = c.getLoginState();
        boolean allowLogin = true;
        ChannelServer channelServer = c.getChannelServer();
        synchronized (this) {
            try {
                WorldChannelInterface worldInterface = channelServer.getWorldInterface();
                if (state == MapleClient.LOGIN_SERVER_TRANSITION) {
                    for (String charName : c.loadCharacterNames(c.getWorld())) {
                        if (worldInterface.isConnected(charName)) {
                            log.warn(MapleClient.getLogMessage(player, "Attempting to double login with " + charName));
                            allowLogin = false;
                            break;
                        }
                    }
                }
            } catch (RemoteException e) {
                channelServer.reconnectWorld();
                allowLogin = false;
            }
            if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
                c.setPlayer(null);
                c.getSession().close();
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
        }
        ChannelServer cserv = ChannelServer.getInstance(c.getChannel());
        cserv.addPlayer(player);
        try {
            WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
            List<PlayerBuffValueHolder> buffs = wci.getBuffsFromStorage(cid);
            if (buffs != null) {
                c.getPlayer().silentGiveBuffs(buffs);
            }
            List<PlayerCoolDownValueHolder> cooldowns = wci.getCooldownsFromStorage(cid);
            if (cooldowns != null) {
                c.getPlayer().giveCoolDowns(cooldowns);
            }
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getSession().write(MaplePacketCreator.getCharInfo(player));
        if (player.isGM()) {
            int[] skills = {4201003, 2301004, 1301007, 2311003, 9101004};
            for (int i : skills) {
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
            }
        }
        player.getMap().addPlayer(player);
        try {
            Collection<BuddylistEntry> buddies = player.getBuddylist().getBuddies();
            int buddyIds[] = player.getBuddylist().getBuddyIds();

            cserv.getWorldInterface().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            if (player.getParty() != null) {
                channelServer.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
            }
            CharacterIdChannelPair[] onlineBuddies = cserv.getWorldInterface().multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                player.getBuddylist().put(ble);
            }
            c.getSession().write(MaplePacketCreator.updateBuddylist(buddies));
            c.getPlayer().sendMacros();
            try {
                c.getPlayer().showNote();
            } catch (SQLException e) {
                log.error("LOADING NOTE", e);
            }
            if (player.getGuildId() > 0) {
                c.getChannelServer().getWorldInterface().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(MaplePacketCreator.showGuildInfo(player));
            }
        } catch (RemoteException e) {
            log.info("REMOTE THROW", e);
            channelServer.reconnectWorld();
        }
        player.updatePartyMemberHP();
        player.sendKeymap();
        for (MapleQuestStatus status : player.getStartedQuests()) {
            if (status.hasMobKills()) {
                c.getSession().write(MaplePacketCreator.updateQuestMobKills(status));
            }
        }
        CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), pendingBuddyRequest.getId(), -1, false));
            c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getName()));
        }
        int petid1 = -1, petid2 = -1, petid3 = -1;
        byte petpos1 = 0, petpos2 = 0, petpos3 = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM petsaves WHERE characterid = ? LIMIT 1");
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                petid1 = rs.getInt("petid1");
                petid2 = rs.getInt("petid2");
                petid3 = rs.getInt("petid3");
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT position FROM inventoryitems WHERE petid = ?");
            ps.setInt(1, petid1);
            rs = ps.executeQuery();
            if (rs.next()) {
                petpos1 = (byte) rs.getInt("position");
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT position FROM inventoryitems WHERE petid = ?");
            ps.setInt(1, petid2);
            rs = ps.executeQuery();
            if (rs.next()) {
                petpos2 = (byte) rs.getInt("position");
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT position FROM inventoryitems WHERE petid = ?");
            ps.setInt(1, petid3);
            rs = ps.executeQuery();
            if (rs.next()) {
                petpos3 = (byte) rs.getInt("position");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        if (petid1 != -1) {
            c.getPlayer().spawnPet(petpos1, true);
            if (petid2 != -1) {
                c.getPlayer().spawnPet(petpos2, false);
                if (petid3 != -1) {
                    c.getPlayer().spawnPet(petpos3, false);
                }
            }
        }
        player.checkMessenger();
        player.checkBerserk();
    }
}