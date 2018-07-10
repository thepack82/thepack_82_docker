package net.sf.odinms.net.channel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import net.sf.odinms.client.BuddyList.BuddyAddResult;
import net.sf.odinms.client.BuddyList.BuddyOperation;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.world.MapleMessenger;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.guild.MapleGuildSummary;
import net.sf.odinms.net.world.remote.WorldChannelCommonOperations;

/**
 *
 * @author Matze
 */
public interface ChannelWorldInterface extends Remote, WorldChannelCommonOperations {
    public void setChannelId(int id) throws RemoteException;
    public int getChannelId() throws RemoteException;
    public String getIP() throws RemoteException;
    public boolean isConnected(int characterId) throws RemoteException;
    public int getConnected() throws RemoteException;
    public int getLocation(String name) throws RemoteException;
    public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;
    public void partyChat(MapleParty party, String chattext, String namefrom) throws RemoteException;
    public boolean isAvailable() throws RemoteException;
    public BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int cidFrom, String nameFrom) throws RemoteException;
    public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation op) throws RemoteException;
    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException;
    public void sendPacket(List<Integer> targetIds, MaplePacket packet, int exception) throws RemoteException;
    public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException;
    public void setOfflineGuildStatus(int guildid, byte guildrank, int cid) throws RemoteException;
    public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) throws RemoteException;
    public void reloadGuildCharacters() throws RemoteException;
    public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) throws RemoteException;
    public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) throws RemoteException;
    public void removeMessengerPlayer(MapleMessenger messenger, int position) throws RemoteException;
    public void messengerChat(MapleMessenger messenger, String chattext, String namefrom) throws RemoteException;
    public void sendSpouseChat(String sender, String target, String message) throws RemoteException;
    public void declineChat(String target, String namefrom) throws RemoteException;
    public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) throws RemoteException;
}
