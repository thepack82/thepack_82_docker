package net.sf.odinms.net.channel.handler;

import static net.sf.odinms.client.BuddyList.BuddyOperation.ADDED;
import static net.sf.odinms.client.BuddyList.BuddyOperation.DELETED;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.odinms.client.BuddyList;
import net.sf.odinms.client.BuddylistEntry;
import net.sf.odinms.client.CharacterNameAndId;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.BuddyList.BuddyAddResult;
import net.sf.odinms.client.BuddyList.BuddyOperation;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.remote.ChannelWorldInterface;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class BuddylistModifyHandler extends AbstractMaplePacketHandler {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BuddylistModifyHandler.class);

    private static class CharacterIdNameBuddyCapacity extends CharacterNameAndId {

        private int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, int buddyCapacity) {
            super(id, name);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return buddyCapacity;
        }
    }

    private void nextPendingRequest(MapleClient c) {
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getName()));
        }
    }

    private CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT id, name, buddyCapacity FROM characters WHERE name LIKE ?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        CharacterIdNameBuddyCapacity ret = null;
        if (rs.next()) {
            ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), rs.getInt("buddyCapacity"));
        }
        rs.close();
        ps.close();
        return ret;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int mode = slea.readByte();
        MapleCharacter player = c.getPlayer();
        WorldChannelInterface worldInterface = c.getChannelServer().getWorldInterface();
        BuddyList buddylist = player.getBuddylist();
        if (mode == 1) { // add
            String addName = slea.readMapleAsciiString();
            BuddylistEntry ble = buddylist.get(addName);
            if (ble != null && !ble.isVisible()) {
                c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 13));
            } else if (buddylist.isFull()) {
                c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 11));
            } else {
                try {
                    CharacterIdNameBuddyCapacity charWithId = null;
                    int channel;
                    MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterByName(addName);
                    if (otherChar != null) {
                        channel = c.getChannel();
                        charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), otherChar.getBuddylist().getCapacity());
                    } else {
                        channel = worldInterface.find(addName);
                        charWithId = getCharacterIdAndNameFromDatabase(addName);
                    }

                    if (charWithId != null) {
                        BuddyAddResult buddyAddResult = null;
                        if (channel != -1) {
                            ChannelWorldInterface channelInterface = worldInterface.getChannelInterface(channel);
                            buddyAddResult = channelInterface.requestBuddyAdd(addName, c.getChannel(), player.getId(), player.getName());
                        } else {
                            Connection con = DatabaseConnection.getConnection();
                            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
                            ps.setInt(1, charWithId.getId());
                            ResultSet rs = ps.executeQuery();
                            if (!rs.next()) {
                                throw new RuntimeException("Result set expected");
                            } else {
                                int count = rs.getInt("buddyCount");
                                if (count >= charWithId.getBuddyCapacity()) {
                                    buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
                                }
                            }
                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
                            ps.setInt(1, charWithId.getId());
                            ps.setInt(2, player.getId());
                            rs = ps.executeQuery();
                            if (rs.next()) {
                                buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
                            }
                            rs.close();
                            ps.close();
                        }
                        if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
                            c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 12));
                        } else {
                            int displayChannel = -1;
                            int otherCid = charWithId.getId();
                            if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1) {
                                displayChannel = channel;
                                notifyRemoteChannel(c, channel, otherCid, ADDED);
                            } else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
                                Connection con = DatabaseConnection.getConnection();
                                PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 1)");
                                ps.setInt(1, charWithId.getId());
                                ps.setInt(2, player.getId());
                                ps.executeUpdate();
                                ps.close();
                            }
                            buddylist.put(new BuddylistEntry(charWithId.getName(), otherCid, displayChannel, true));
                            c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 15));
                    }
                } catch (RemoteException e) {
                    log.error("REMOTE THROW", e);
                } catch (SQLException e) {
                    log.error("SQL THROW", e);
                }
            }
        } else if (mode == 2) { // accept buddy
            int otherCid = slea.readInt();
            if (!buddylist.isFull()) {
                try {
                    int channel = worldInterface.find(otherCid);
                    String otherName = null;
                    MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(otherCid);
                    if (otherChar == null) {
                        Connection con = DatabaseConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?");
                        ps.setInt(1, otherCid);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            otherName = rs.getString("name");
                        }
                        rs.close();
                        ps.close();
                    } else {
                        otherName = otherChar.getName();
                    }
                    if (otherName != null) {
                        buddylist.put(new BuddylistEntry(otherName, otherCid, channel, true));
                        c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
                        notifyRemoteChannel(c, channel, otherCid, ADDED);
                    }
                } catch (RemoteException e) {
                    log.error("REMOTE THROW", e);
                } catch (SQLException e) {
                    log.error("SQL THROW", e);
                }
            }
            nextPendingRequest(c);
        } else if (mode == 3) { // delete
            int otherCid = slea.readInt();
            if (buddylist.containsVisible(otherCid)) {
                try {
                    notifyRemoteChannel(c, worldInterface.find(otherCid), otherCid, DELETED);
                } catch (RemoteException e) {
                    log.error("REMOTE THROW", e);
                }
            }
            buddylist.remove(otherCid);
            c.getSession().write(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
            nextPendingRequest(c);
        }
    }

    private void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyOperation operation)
            throws RemoteException {
        WorldChannelInterface worldInterface = c.getChannelServer().getWorldInterface();
        MapleCharacter player = c.getPlayer();

        if (remoteChannel != -1) {
            ChannelWorldInterface channelInterface = worldInterface.getChannelInterface(remoteChannel);
            channelInterface.buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
        }
    }
}
