package net.sf.odinms.net.world.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sf.odinms.net.channel.remote.ChannelWorldInterface;
import net.sf.odinms.net.world.CharacterIdChannelPair;
import net.sf.odinms.net.world.MapleMessenger;
import net.sf.odinms.net.world.MapleMessengerCharacter;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.PlayerBuffValueHolder;
import net.sf.odinms.net.world.PlayerCoolDownValueHolder;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.net.world.guild.MapleGuildCharacter;

/**
 *
 * @author Matze
 */
public interface WorldChannelInterface extends Remote, WorldChannelCommonOperations {

    public Properties getDatabaseProperties() throws RemoteException;
    public Properties getGameProperties() throws RemoteException;
    public void serverReady() throws RemoteException;
    public String getIP(int channel) throws RemoteException;
    public int find(String charName) throws RemoteException;
    public int find(int characterId) throws RemoteException;
    public Map<Integer, Integer> getConnected() throws RemoteException;
    MapleParty createParty(MaplePartyCharacter chrfor) throws RemoteException;
    MapleParty getParty(int partyid) throws RemoteException;
    public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;
    public void partyChat(int partyid, String chattext, String namefrom) throws RemoteException;
    public boolean isAvailable() throws RemoteException;
    public ChannelWorldInterface getChannelInterface(int channel) throws RemoteException;
    public WorldLocation getLocation(String name) throws RemoteException;
    public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException;
    public MapleGuild getGuild(int id, MapleGuildCharacter mgc) throws RemoteException;
    public void clearGuilds() throws RemoteException;
    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException;
    public int addGuildMember(MapleGuildCharacter mgc) throws RemoteException;
    public void leaveGuild(MapleGuildCharacter mgc) throws RemoteException;
    public void guildChat(int gid, String name, int cid, String msg) throws RemoteException;
    public void changeRank(int gid, int cid, int newRank) throws RemoteException;
    public void expelMember(MapleGuildCharacter initiator, String name, int cid) throws RemoteException;
    public void setGuildNotice(int gid, String notice) throws RemoteException;
    public void memberLevelJobUpdate(MapleGuildCharacter mgc) throws RemoteException;
    public void changeRankTitle(int gid, String[] ranks) throws RemoteException;
    public int createGuild(int leaderId, String name) throws RemoteException;
    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) throws RemoteException;
    public void disbandGuild(int gid) throws RemoteException;
    public boolean increaseGuildCapacity(int gid) throws RemoteException;
    public void gainGP(int gid, int amount) throws RemoteException;
    MapleMessenger createMessenger(MapleMessengerCharacter chrfor) throws RemoteException;
    MapleMessenger getMessenger(int messengerid) throws RemoteException;
    public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException;
    public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException;
    public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) throws RemoteException;
    public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException;
    public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException;
    public void declineChat(String target, String namefrom) throws RemoteException;
    public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException;
    public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) throws RemoteException;
    public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) throws RemoteException;
    public void addCooldownsToStorage(int chrid, List<PlayerCoolDownValueHolder> toStore) throws RemoteException;
    public List<PlayerCoolDownValueHolder> getCooldownsFromStorage(int chrid) throws RemoteException;
    public void sendSpouseChat(String sender, String target, String message) throws RemoteException;
}
