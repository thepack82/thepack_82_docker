package net.sf.odinms.tools;

import java.awt.Point;
import java.awt.Rectangle;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.odinms.server.MTSItemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.odinms.client.MapleMount;
import net.sf.odinms.client.BuddylistEntry;
import net.sf.odinms.client.IEquip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleKeyBinding;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.IEquip.ScrollResult;
import net.sf.odinms.client.MapleDisease;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MapleRing;
import net.sf.odinms.client.SkillMacro;
import net.sf.odinms.client.status.MonsterStatus;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.ByteArrayMaplePacket;
import net.sf.odinms.net.LongValueHolder;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.SendPacketOpcode;
import net.sf.odinms.net.channel.handler.SummonDamageHandler.SummonAttackEntry;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePlayerShop;
import net.sf.odinms.server.MaplePlayerShopItem;
import net.sf.odinms.server.MapleShopItem;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleReactor;
import net.sf.odinms.server.movement.LifeMovementFragment;
import net.sf.odinms.tools.data.output.LittleEndianWriter;
import net.sf.odinms.tools.data.output.MaplePacketLittleEndianWriter;
import net.sf.odinms.net.world.guild.*;
import net.sf.odinms.server.MapleDueyActions;
import net.sf.odinms.server.MapleMiniGame;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.maps.MapleSummon;

/**
 * Provides all MapleStory packets needed in one place.
 *
 * @author Frz
 * @since Revision 259
 * @version 1.0
 */
public class MaplePacketCreator {

    private static Logger log = LoggerFactory.getLogger(MaplePacketCreator.class);
    private final static byte[] CHAR_INFO_MAGIC = new byte[]{(byte) 0xff, (byte) 0xc9, (byte) 0x9a, 0x3b};
    private final static byte[] ITEM_MAGIC = new byte[]{(byte) 0x80, 5};
    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
    private final static long FT_UT_OFFSET = 116444592000000000L; // EDT

    public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage) {
        return damagePlayer(skill, monsteridfrom, cid, damage, 0, 0, false, 0, true, 0, 0, 0);
    }

    public static MaplePacket giveBuffTest(int buffid, int bufflength, long mask) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.writeShort(1);
        mplew.writeInt(buffid);
        mplew.writeInt(bufflength);
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.write(0); // combo 600, too
        mplew.write(0); // new in v0.56
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        if (bufflength == 1004 || bufflength == 5221006) {
            mplew.writeInt(0x2F258DB9);
        } else {
            mplew.writeShort(0); // ??? wk charges have 600 here o.o
        }
        mplew.write(0); // combo 600, too
        mplew.write(0); // new in v0.56
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket sendGMPolice() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString("59 00 40 42 0F 00 04 00 04 00 4C 75 6C 7A"));
        return mplew.getPacket();
    }

    private static long getKoreanTimestamp(long realTimestamp) {
        long time = (realTimestamp / 1000 / 60); // convert to minutes
        return ((time * 600000000) + FT_UT_OFFSET);
    }

    private static long getTime(long realTimestamp) {
        long time = (realTimestamp / 1000); // convert to seconds
        return ((time * 10000000) + FT_UT_OFFSET);
    }

    /**
     * Sends a hello packet.
     *
     * @param mapleVersion The maple client version.
     * @param sendIv the IV used by the server for sending
     * @param recvIv the IV used by the server for receiving
     * @param testServer
     * @return
     */
    public static MaplePacket getHello(short mapleVersion, byte[] sendIv, byte[] recvIv, boolean testServer) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(0x0d);
        mplew.writeShort(mapleVersion);
        mplew.write(new byte[]{0, 0});
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(testServer ? 5 : 8);

        return mplew.getPacket();
    }

    /**
     * Sends a ping packet.
     *
     * @return The packet.
     */
    public static MaplePacket getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(SendPacketOpcode.PING.getValue());

        return mplew.getPacket();
    }

    /**
     * Gets a login failed packet.
     *
     * Possible values for <code>reason</code>:<br>
     * 3: ID deleted or blocked<br>
     * 4: Incorrect password<br>
     * 5: Not a registered id<br>
     * 6: System error<br>
     * 7: Already logged in<br>
     * 8: System error<br>
     * 9: System error<br>
     * 10: Cannot process so many connections<br>
     * 11: Only users older than 20 can use this channel<br>
     * 13: Unable to log on as master at this ip<br>
     * 14: Wrong gateway or personal info and weird korean button<br>
     * 15: Processing request with that korean button!<br>
     * 16: Please verify your account through email...<br>
     * 17: Wrong gateway or personal info<br>
     * 21: Please verify your account through email...<br>
     * 23: License agreement<br>
     * 25: Maple Europe notice =[<br>
     * 27: Some weird full client notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static MaplePacket getLoginFailed(int reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeInt(reason);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket getPermBan(byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(0x02); // Account is banned
        mplew.write(0x0);
        mplew.write(reason);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));
        return mplew.getPacket();
    }

    public static MaplePacket getTempBan(long timestampTill, byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(0x02);
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00")); // Account is banned
        mplew.write(reason);
        mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.
        return mplew.getPacket();
    }

    /**
     * Gets a successful authentication and PIN Request packet.
     *
     * @param account The account name.
     * @return The PIN request packet.
     */
    public static MaplePacket getAuthSuccessRequestPin(String account) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(new byte[]{0, 0, 0, 0, 0, 0, (byte) 0xFF, 0x6A, 1, 0, 0, 0, 0x4E});
        mplew.writeMapleAsciiString(account);
        mplew.write(new byte[]{3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xDC, 0x3D, 0x0B, 0x28, 0x64, (byte) 0xC5, 1, 8, 0, 0, 0});
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a PIN operation.
     *
     * Possible values for <code>mode</code>:<br>
     * 0 - PIN was accepted<br>
     * 1 - Register a new PIN<br>
     * 2 - Invalid pin / Reenter<br>
     * 3 - Connection failed due to system error<br>
     * 4 - Enter the pin
     *
     * @param mode The mode.
     * @return
     */
    public static MaplePacket pinOperation(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PIN_OPERATION.getValue());
        mplew.write(mode);
        return mplew.getPacket();
    }

    /**
     * Gets a packet requesting the client enter a PIN.
     *
     * @return The request PIN packet.
     */
    public static MaplePacket requestPin() {
        return pinOperation((byte) 4);
    }

    /**
     * Gets a packet requesting the PIN after a failed attempt.
     *
     * @return The failed PIN packet.
     */
    public static MaplePacket requestPinAfterFailure() {
        return pinOperation((byte) 2);
    }

    /**
     * Gets a packet saying the PIN has been accepted.
     *
     * @return The PIN accepted packet.
     */
    public static MaplePacket pinAccepted() {
        return pinOperation((byte) 0);
    }

    /**
     * Gets a packet detailing a server and its channels.
     *
     * @param serverIndex The index of the server to create information about.
     * @param serverName The name of the server.
     * @param channelLoad Load of the channel - 1200 seems to be max.
     * @return The server info packet.
     */
    public static MaplePacket getServerList(int serverId, String serverName, Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(serverId);
        mplew.writeMapleAsciiString(serverName);
        mplew.write(LoginServer.getInstance().getFlag());
        mplew.writeMapleAsciiString(LoginServer.getInstance().getEventMessage());
        mplew.write(0x64); // rate modifier, don't ask O.O!
        mplew.write(0x0); // event xp * 2.6 O.O!
        mplew.write(0x64); // rate modifier, don't ask O.O!
        mplew.write(0x0); // drop rate * 2.6
        mplew.write(0x0);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);
        int load;
        for (int i = 1; i <= lastChannel; i++) {
            if (channels.contains(i)) {
                load = channelLoad.get(i);
            } else {
                load = 1200;
            }
            mplew.writeMapleAsciiString(serverName + "-" + i);
            mplew.writeInt(load);
            mplew.write(1);
            mplew.writeShort(i - 1);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet saying that the server list is over.
     *
     * @return The end of server list packet.
     */
    public static MaplePacket getEndOfServerList() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a server status message.
     *
     * Possible values for <code>status</code>:<br>
     * 0 - Normal<br>
     * 1 - Highly populated<br>
     * 2 - Full
     *
     * @param status The server status.
     * @return The server status packet.
     */
    public static MaplePacket getServerStatus(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the channel server.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @param clientId The ID of the client.
     * @return The server IP packet.
     */
    public static MaplePacket getServerIP(InetAddress inetAddr, int port, int clientId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[]{0, 0, 0, 0, 0});
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the new channel.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @return The server IP packet.
     */
    public static MaplePacket getChannelChange(InetAddress inetAddr, int port) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        return mplew.getPacket();
    }

    /**
     * Gets a packet with a list of characters.
     *
     * @param c The MapleClient to load characters of.
     * @param serverId The ID of the server requested.
     * @return The character list packet.
     */
    public static MaplePacket getCharList(MapleClient c, int serverId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        List<MapleCharacter> chars = c.loadCharacters(serverId);
        mplew.write((byte) chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr);
        }
        mplew.writeInt(LoginServer.getInstance().getMaxCharacters());
        return mplew.getPacket();
    }

    /**
     * Adds character stats to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add the stats of.
     */
    private static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId()); // character id
        mplew.writeAsciiString(chr.getName());
        for (int x = chr.getName().length(); x < 13; x++) { // fill to maximum name length
            mplew.write(0);
        }
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.write(chr.getLevel()); // level
        mplew.writeShort(chr.getJob().getId()); // job
        mplew.writeShort(chr.getStr()); // str
        mplew.writeShort(chr.getDex()); // dex
        mplew.writeShort(chr.getInt()); // int
        mplew.writeShort(chr.getLuk()); // luk
        mplew.writeShort(chr.getHp()); // hp (?)
        mplew.writeShort(chr.getMaxHp()); // maxhp
        mplew.writeShort(chr.getMp()); // mp (?)
        mplew.writeShort(chr.getMaxMp()); // maxmp
        mplew.writeShort(chr.getRemainingAp()); // remaining ap
        mplew.writeShort(chr.getRemainingSp()); // remaining sp
        mplew.writeInt(chr.getExp()); // current exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(chr.isMarried());
        mplew.writeInt(chr.getMapId()); // current map id
        mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
        mplew.writeInt(0);
    }

    public static MaplePacket enableTV() {
        // [0F 01] [00 00 00 00] [00] <-- 0x112 in v63,
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENABLE_TV.getValue()); // enableTV = 0x10F
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket removeTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_TV.getValue()); // removeTV = 0x111 <-- v63
        return mplew.getPacket();
    }

    public static MaplePacket sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_TV.getValue()); // SEND_TV = 0x11D
        mplew.write(partner != null ? 2 : 0);
        mplew.write(type); // type   Heart = 2  Star = 1  Normal = 0
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        if (partner != null) {
            mplew.writeMapleAsciiString(partner.getName());
        } else {
            mplew.writeShort(0); // could be partner
        }
        for (int i = 0; i < messages.size(); i++) { // for (String message : messages) {
            if (i == 4 && messages.get(4).length() > 15) {
                mplew.writeMapleAsciiString(messages.get(4).substring(0, 15)); // hmm ?
            } else {
                mplew.writeMapleAsciiString(messages.get(i));
            }
        }
        mplew.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
        if (partner != null) {
            addCharLook(mplew, partner, false);
        }
        return mplew.getPacket();
    }

    /**
     * Adds the aesthetic aspects of a character to an existing
     * MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add the looks of.
     * @param mega Unknown
     */
    private static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        // variable length
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair()); // hair
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (IItem item : equip.list()) {
            byte pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if ((pos > 100 || pos == -128) && pos != 111) {
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        IItem cWeapon = equip.getItem((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon.getItemId());
        } else {
            mplew.writeInt(0); // cashweapon

        }
        mplew.writeInt(0);
        mplew.writeLong(0);
    }

    /**
     * Adds an entry for a character to an existing
     * MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add.
     */
    private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        if (chr.getJob().isA(MapleJob.GM)) {
            mplew.write(0);
            return;
        }
        mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled)
        mplew.writeInt(chr.getRank()); // world rank
        mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
        mplew.writeInt(chr.getJobRank()); // job rank
        mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
    }

    /**
     * Adds a quest info entry for a character to an existing
     * MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add quest info about.
     */
    private static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        List<MapleQuestStatus> started = chr.getStartedQuests();
        mplew.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mplew.writeInt(q.getQuest().getId());
        }
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeInt(KoreanDateUtil.getQuestTimestamp(q.getCompletionTime()));
            mplew.writeInt(KoreanDateUtil.getQuestTimestamp(q.getCompletionTime()));
        }
    }

    /**
     * Gets character info for a character.
     *
     * @param chr The character to get info about.
     * @return The character info packet.
     */
    public static MaplePacket getCharInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue()); // 0x49
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        mplew.writeInt(new Random().nextInt()); // seed the maplestory rng with a random number <3
        mplew.write(HexTool.getByteArrayFromHexString("F8 17 D7 13 CD C5 AD 78"));
        mplew.writeLong(-1);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
        mplew.writeInt(chr.getMeso()); // mesos
        mplew.write(100); // equip slots
        mplew.write(100); // use slots
        mplew.write(100); // set-up slots
        mplew.write(100); // etc slots
        mplew.write(100); // cash slots
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        for (Item item : equipped) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0); // start of equip inventory
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of use inventory
        // addItemInfo(mplew, new Item(2020028, (byte) 8, (short) 1));
        iv = chr.getInventory(MapleInventoryType.USE);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of set-up inventory
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of etc inventory
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of cash inventory
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of skills
        Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            if (skill.getKey().isFourthJob()) {
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }
        mplew.writeShort(0);
        addQuestInfo(mplew, chr);
        List<MapleRing> rings = new ArrayList<MapleRing>();
        for (Item item : equipped) {
            if (((IEquip) item).getRingId() > -1) {
                rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
            }
        }
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (IItem item : iv.list()) {
            if (((IEquip) item).getRingId() > -1) {
                rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
            }
        }
        Collections.sort(rings);
        boolean FR_last = false;
        for (MapleRing ring : rings) {
            if ((ring.getItemId() >= 1112800 && ring.getItemId() <= 1112803 || ring.getItemId() <= 1112806 || ring.getItemId() <= 1112807 || ring.getItemId() <= 1112809) && rings.indexOf(ring) == 0) {
                mplew.writeShort(0);
            }
            mplew.writeShort(0);
            mplew.writeShort(1);
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            if (ring.getItemId() >= 1112800 && ring.getItemId() <= 1112803 || ring.getItemId() <= 1112806 || ring.getItemId() <= 1112807 || ring.getItemId() <= 1112809) {
                FR_last = true;
                mplew.writeInt(0);
                mplew.writeInt(ring.getItemId());
                mplew.writeShort(0);
            } else {
                if (rings.size() > 1) {
                    mplew.writeShort(0);
                }
                FR_last = false;
            }
        }
        if (!FR_last) {
            mplew.writeLong(0);
        }
        for (int x = 0; x < 15; x++) {

            mplew.write(CHAR_INFO_MAGIC);
        }
        mplew.writeInt(0);
        mplew.writeLong(getTime((long) System.currentTimeMillis()));
        return mplew.getPacket();
    }

    /**
     * Gets an empty stat update.
     *
     * @return The empy stat update packet.
     */
    public static MaplePacket enableActions() {
        return updatePlayerStats(EMPTY_STATUPDATE, true);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The stats to update.
     * @return The stat update packet.
     */
    public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats) {
        return updatePlayerStats(stats, false);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The list of stats to update.
     * @param itemReaction Result of an item reaction(?)
     * @return The stat update packet.
     */
    public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        if (itemReaction) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        int updateMask = 0;
        for (Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {

                @Override
                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    mplew.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    mplew.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate.getRight().intValue());
                }
            }
        }

        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to change maps.
     *
     * @param to The <code>MapleMap</code> to warp to.
     * @param spawnPoint The spawn portal number to spawn at.
     * @param chr The character warping to <code>to</code>
     * @return The map change packet.
     */
    public static MaplePacket getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue()); // 0x49
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.writeShort(0x2);
        mplew.writeShort(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getHp()); // hp (???)
        mplew.write(0);
        long questMask = 0x1ffffffffffffffL;
        mplew.writeLong(questMask);

        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a portal.
     *
     * @param townId The ID of the town the portal goes to.
     * @param targetId The ID of the target.
     * @param pos Where to put the portal.
     * @return The portal spawn packet.
     */
    public static MaplePacket spawnPortal(int townId, int targetId, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (pos != null) {
            mplew.writeShort(pos.x);
            mplew.writeShort(pos.y);
        }

        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a door.
     *
     * @param oid The door's object ID.
     * @param pos The position of the door.
     * @param town
     * @return The remove door packet.
     */
    public static MaplePacket spawnDoor(int oid, Point pos, boolean town) {
        // [D3 00] [01] [93 AC 00 00] [6B 05] [37 03]
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(town ? 1 : 0);
        mplew.writeInt(oid);
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);

        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a door.
     *
     * @param oid The door's ID.
     * @param town
     * @return The remove door packet.
     */
    public static MaplePacket removeDoor(int oid, boolean town) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (town) {
            mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
        } else {
            mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
            mplew.write(/*town ? 1 : */0);
            mplew.writeInt(oid);
        }

        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a special map object.
     *
     * @param summon
     * @param skillLevel The level of the skill used.
     * @param animated Animated spawn?
     * @return The spawn packet for the map object.
     */
    public static MaplePacket spawnSpecialMapObject(MapleSummon summon, int skillLevel, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId()); // Supposed to be Object ID, but this works too! <3
        mplew.writeInt(summon.getSkill());
        mplew.write(skillLevel);
        mplew.writeShort(summon.getPosition().x);
        mplew.writeShort(summon.getPosition().y);
        mplew.write(3); // test
        mplew.write(0); // test
        mplew.write(0); // test
        mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
        mplew.write(1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
        mplew.write(animated ? 0 : 1);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a special map object.
     *
     * @param summon
     * @param animated Animated removal?
     * @return The packet removing the object.
     */
    public static MaplePacket removeSpecialMapObject(MapleSummon summon, boolean animated) {
        // [86 00] [6A 4D 27 00] 33 1F 00 00 02
        // 92 00 36 1F 00 00 0F 65 85 01 84 02 06 46 28 00 06 81 02 01 D9 00 BD FB D9 00 BD FB 38 04 2F 21 00 00 10 C1 2A 00 06 00 06 01 00 01 BD FB FC 00 BD FB 6A 04 88 1D 00 00 7D 01 AF FB
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1); // ?
        return mplew.getPacket();
    }

    /**
     * Adds info about an item to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to write to.
     * @param item The item to write info about.
     */
    protected static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item) {
        addItemInfo(mplew, item, false, false);
    }

    /**
     * Adds expiration time info to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to write to.
     * @param time The expiration time.
     * @param showexpirationtime Show the expiration time?
     */
    private static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time, boolean showexpirationtime) {
        if (time != 0) {
            mplew.writeInt(KoreanDateUtil.getItemTimestamp(time));
        } else {
            mplew.writeInt(400967355);
        }
        mplew.write(showexpirationtime ? 1 : 2);
    }

    /**
     * Adds item info to existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to write to.
     * @param item The item to add info about.
     * @param zeroPosition Is the position zero?
     * @param leaveOut Leave out the item if position is zero?
     */
    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean ring = false;
        IEquip equip = null;
        if (item.getType() == IItem.EQUIP) {
            equip = (IEquip) item;
            if (equip.getRingId() > -1) {
                ring = true;
            }
        }
        byte pos = item.getPosition();
        boolean masking = false;
        boolean equipped = false;
        if (zeroPosition) {
            if (!leaveOut) {
                mplew.write(0);
            }
        } else if (pos <= (byte) -1) {
            pos *= -1;
            if ((pos > 100 || pos == -128) || ring) {
                masking = true;
                mplew.write(0);
                mplew.write(pos - 100);
            } else {
                mplew.write(pos);
            }
            equipped = true;
        } else {
            mplew.write(item.getPosition());
        }
        if (item.getPetId() > -1) {
            mplew.write(3);
        } else {
            mplew.write(item.getType());
        }
        mplew.writeInt(item.getItemId());
        if (ring) {
            mplew.write(1);
            mplew.writeInt(equip.getRingId());
            mplew.writeInt(0);
        }
        if (item.getPetId() > -1) {
            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getPosition(), item.getPetId());
            String petname = pet.getName();
            mplew.write(1);
            mplew.writeInt(item.getPetId());
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            mplew.write(HexTool.getByteArrayFromHexString("BB 46 E6 17 02"));
            if (petname.length() > 13) {
                petname = petname.substring(0, 13);
            }
            mplew.writeAsciiString(petname);
            for (int i = petname.length(); i < 13; i++) {
                mplew.write(0);
            }
            mplew.write(pet.getLevel());
            mplew.writeShort(pet.getCloseness());
            mplew.write(pet.getFullness());

            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
            mplew.writeInt(0);

            return;
        }
        if (masking && !ring) {
            // 07.03.2008 06:49... o.o
            mplew.write(HexTool.getByteArrayFromHexString("01 41 B4 38 00 00 00 00 00 80 20 6F"));
            addExpirationTime(mplew, 0, false);
        } else if (ring) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        } else {
            mplew.writeShort(0);
            mplew.write(ITEM_MAGIC);
            addExpirationTime(mplew, 0, false);
        }
        if (item.getType() == IItem.EQUIP) {
            mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort(equip.getStr()); // str
            mplew.writeShort(equip.getDex()); // dex
            mplew.writeShort(equip.getInt()); // int
            mplew.writeShort(equip.getLuk()); // luk
            mplew.writeShort(equip.getHp()); // hp
            mplew.writeShort(equip.getMp()); // mp
            mplew.writeShort(equip.getWatk()); // watk
            mplew.writeShort(equip.getMatk()); // matk
            mplew.writeShort(equip.getWdef()); // wdef
            mplew.writeShort(equip.getMdef()); // mdef
            mplew.writeShort(equip.getAcc()); // accuracy
            mplew.writeShort(equip.getAvoid()); // avoid
            mplew.writeShort(equip.getHands()); // hands
            mplew.writeShort(equip.getSpeed()); // speed
            mplew.writeShort(equip.getJump()); // jump
            mplew.writeMapleAsciiString(equip.getOwner());

            // 0 normal; 1 locked
            mplew.write(equip.getLocked());

            if (ring && !equipped) {
                mplew.write(0);
            }

            if (!masking && !ring) {
                mplew.write(0);
                mplew.writeLong(0); // values of these don't seem to matter at
            // all
            }
        } else {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(0); // this seems to end the item entry
            // but only if its not a THROWING STAR :))9 O.O!

            if (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
                mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 54 00 00 34"));
            }
        }
    }

    /**
     * Gets the response to a relog request.
     *
     * @return The relog response packet.
     */
    public static MaplePacket getRelogResponse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
        mplew.write(1);

        return mplew.getPacket();
    }

    /**
     * Gets a server message packet.
     *
     * @param message The message to convey.
     * @return The server message packet.
     */
    public static MaplePacket serverMessage(String message) {
        return serverMessage(4, 0, message, true, false);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static MaplePacket serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false, false);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static MaplePacket serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, false);
    }

    public static MaplePacket serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, false, smegaEar);
    }

    /**
     * Gets a server message packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @param servermessage Is this a scrolling ticker?
     * @return The server notice packet.
     */
    private static MaplePacket serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (servermessage) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        if (type == 3) {
            mplew.write(channel - 1); // channel
            mplew.write(megaEar ? 1 : 0);

        }
        return mplew.getPacket();
    }

    /**
     * Gets an avatar megaphone packet.
     *
     * @param chr The character using the avatar megaphone.
     * @param channel The channel the character is on.
     * @param itemId The ID of the avatar-mega.
     * @param message The message that is sent.
     * @param ear
     * @return The avatar mega packet.
     */
    public static MaplePacket getAvatarMega(MapleCharacter chr, int channel, int itemId, List<String> message, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getName());
        for (String s : message) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(channel - 1); // channel
        mplew.write(ear ? 1 : 0);
        addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }

    /**
     * Gets a NPC spawn packet.
     *
     * @param life The NPC to spawn.
     * @return The NPC spawn packet.
     */
    public static MaplePacket spawnNPC(MapleNPC life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        if (life.getF() == 1) {
            mplew.write(0);
        } else {
            mplew.write(1);
        }
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        if (life.getF() == 1) {
            mplew.write(0);
        } else {
            mplew.write(1);
        }
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(MiniMap ? 1 : 0);

        return mplew.getPacket();
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     * @param newSpawn Is it a new spawn?
     * @return The spawn monster packet.
     */
    public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn) {
        return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     * @param newSpawn Is it a new spawn?
     * @param effect The spawn effect.
     * @return The spawn monster packet.
     */
    public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
        return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
    }

    /**
     * Gets a control monster packet.
     *
     * @param life The monster to give control to.
     * @param newSpawn Is it a new spawn?
     * @param aggro Aggressive monster?
     * @return The monster control packet.
     */
    public static MaplePacket controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
        return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
    }

    public static MaplePacket makeMonsterInvisible(MapleMonster life) {
        return spawnMonsterInternal(life, true, false, false, 0, true);
    }

    /**
     * Internal function to handler monster spawning and controlling.
     *
     * @param life The mob to perform operations with.
     * @param requestController Requesting control of mob?
     * @param newSpawn New spawn (fade in?)
     * @param aggro Aggressive mob?
     * @param effect The spawn effect to use.
     * @return The spawn/control packet.
     */
    private static MaplePacket spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
        // 95 00 DA 33 37 00 01 58 CC 6C 00 00 00 00 00 B7 FF F3 FB 02 1A 00 1A
        // 00 02 0E 06 00 00 FF
        // OP OBJID MOBID NULL PX PY ST 00 00 FH
        // 95 00 7A 00 00 00 01 58 CC 6C 00 00 00 00 00 56 FF 3D FA 05 00 00 00
        // 00 FE FF

        // [AE 00] [1D 43 3C 00] [01] [21 B3 81 00] [00 00 00 08] [00 00 00 00] [63 FE] [7E FE] [02] [14 00] [14 00] [FD] [B3 42 3C 00] [FF] [00 00 00 00]
        // [AE 00] [EA 77 2A 00] [01] [DB 70 8F 00] [00 00 00 08] [00 00 00 00] [04 01] [91 FA] [02] [81 00] [8A 00] [FF FF] [00 00 00 00]

        // [B0 00] [01] [1D 43 3C 00] [01] [21 B3 81 00] [00 00 00 08] [00 00 00 00] [63 FE] [7E FE] [02] [14 00] [14 00] [FF FF] [00 00 00 00]
        // [AE 00] [2F FE 3D 00] [01] [B4 34 03 00] [00 00 00 08] [00 00 00 00] [03 03] [AB FF] [02 0A] [00] [0D 00] [FF FF] [00 00 00 00]
        // AE 00 4C EF 18 00 01 A4 CB 6C 00 00 00 00 08 00 00 00 00 ED FE C6 06 0D 00 00 00 00 FD AB 03 18 00 FF 00 00 00 00
        // B0 00 01 4C EF 18 00 01 A4 CB 6C 00 00 00 00 08 00 00 00 00 ED FE C6 06 0D 00 00 00 00 FF FF 00 00 00 00


        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (makeInvis) {
            mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
            mplew.write(0);
            mplew.writeInt(life.getObjectId());
            return mplew.getPacket();
        }
        if (requestController) {
            mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
            if (aggro) {
                mplew.write(2);
            } else {
                mplew.write(1);
            }
        } else {
            mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        }
        mplew.writeInt(life.getObjectId());
        mplew.write(5); // ????!? either 5 or 1?
        mplew.writeInt(life.getId());
        mplew.write(0);
        mplew.writeShort(0);
        mplew.write(8);
        mplew.writeInt(0);
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
//		mplew.writeShort(life.getStartFh()); // Makes monster looks like being vacced...
        mplew.writeShort(0);
        mplew.writeShort(life.getFh());
        if (effect > 0) {
            mplew.write(effect);
            mplew.write(0);
            mplew.writeShort(0);
        }

        if (newSpawn) {
            mplew.writeShort(-2);
        } else {
            mplew.writeShort(-1);
        }
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * Handles monsters not being targettable, such as Zakum's first body.
     * @param life The mob to spawn as non-targettable.
     * @param effect The effect to show when spawning.
     * @return The packet to spawn the mob as non-targettable.
     */
    public static MaplePacket spawnFakeMonster(MapleMonster life, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.write(5);
        mplew.writeInt(life.getId());
        mplew.writeInt(0);
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        if (effect > 0) {
            mplew.write(effect);
            mplew.write(0);
            mplew.writeShort(0);
        }
        mplew.writeShort(-2);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * Makes a monster previously spawned as non-targettable, targettable.
     * @param life The mob to make targettable.
     * @return The packet to make the mob targettable.
     */
    public static MaplePacket makeMonsterReal(MapleMonster life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.write(5);
        mplew.writeInt(life.getId());
        mplew.writeInt(0);
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        mplew.writeShort(-1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * Gets a stop control monster packet.
     *
     * @param oid The ObjectID of the monster to stop controlling.
     * @return The stop control monster packet.
     */
    public static MaplePacket stopControllingMonster(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(0);
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @return The move response packet.
     */
    public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @param skillId The skill ID for the monster to use.
     * @param skillLevel The level of the skill to use.
     * @return The move response packet.
     */
    public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        // A1 00 18 DC 41 00 01 00 00 1E 00 00 00
        // A1 00 22 22 22 22 01 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(moveid);
        mplew.write(useSkills ? 1 : 0);
        mplew.writeShort(currentMp);
        mplew.write(skillId);
        mplew.write(skillLevel);

        return mplew.getPacket();
    }

    /**
     * Gets a general chat packet.
     *
     * @param cidfrom The character ID who sent the chat.
     * @param text The text of the chat.
     * @param whiteBG
     * @param show
     * @return The general chat packet.
     */
    public static MaplePacket getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        return mplew.getPacket();
    }

    /**
     * For testing only! Gets a packet from a hexadecimal string.
     *
     * @param hex The hexadecimal packet to create.
     * @return The MaplePacket representing the hex string.
     */
    public static MaplePacket getPacketFromHexString(String hex) {
        byte[] b = HexTool.getByteArrayFromHexString(hex);
        return new ByteArrayMaplePacket(b);
    }

    /**
     * Gets a packet telling the client to show an EXP increase.
     *
     * @param gain The amount of EXP gained.
     * @param inChat In the chat box?
     * @param white White text or yellow?
     * @return The exp gained packet.
     */
    public static MaplePacket getShowExpGain(int gain, boolean inChat, boolean white) {
        // 20 00 03 01 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(inChat ? 1 : 0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a fame gain.
     *
     * @param gain How many fame gained.
     * @return The meso gain packet.
     */
    public static MaplePacket getShowFameGain(int gain) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);

        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @return The meso gain packet.
     */
    public static MaplePacket getShowMesoGain(int gain) {
        return getShowMesoGain(gain, false);
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @param inChat Show in the chat window?
     * @return The meso gain packet.
     */
    public static MaplePacket getShowMesoGain(int gain, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.write(1);
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0); // inet cafe meso gain ?.o

        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity How many items gained.
     * @return The item gain packet.
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     * Gets a packet telling the client to show an item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity The number of items gained.
     * @param inChat Show in the chat window?
     * @return The item gain packet.
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (inChat) {
            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static MaplePacket killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     * Gets a packet telling the client that a monster was killed.
     *
     * @param oid The objectID of the killed monster.
     * @param animation 0 = dissapear, 1 = fade out, 2+ = special
     * @return The kill monster packet.
     */
    public static MaplePacket killMonster(int oid, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation); // Not a boolean, really an int type
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show mesos coming out of a map
     * object.
     *
     * @param amount The amount of mesos.
     * @param itemoid The ObjectID of the dropped mesos.
     * @param dropperoid The OID of the dropper.
     * @param ownerid The ID of the drop owner.
     * @param dropfrom Where to drop from.
     * @param dropto Where the drop lands.
     * @param mod ?
     * @return The drop mesos packet.
     */
    public static MaplePacket dropMesoFromMapObject(int amount, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod) {
        return dropItemFromMapObjectInternal(amount, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, true);
    }

    /**
     * Gets a packet telling the client to show an item coming out of a map
     * object.
     *
     * @param itemid The ID of the dropped item.
     * @param itemoid The ObjectID of the dropped item.
     * @param dropperoid The OID of the dropper.
     * @param ownerid The ID of the drop owner.
     * @param dropfrom Where to drop from.
     * @param dropto Where the drop lands.
     * @param mod ?
     * @return The drop mesos packet.
     */
    public static MaplePacket dropItemFromMapObject(int itemid, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod) {
        return dropItemFromMapObjectInternal(itemid, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, false);
    }

    /**
     * Internal function to get a packet to tell the client to drop an item onto
     * the map.
     *
     * @param itemid The ID of the item to drop.
     * @param itemoid The ObjectID of the dropped item.
     * @param dropperoid The OID of the dropper.
     * @param ownerid The ID of the drop owner.
     * @param dropfrom Where to drop from.
     * @param dropto Where the drop lands.
     * @param mod ?
     * @param mesos Is the drop mesos?
     * @return The item drop packet.
     */
    public static MaplePacket dropItemFromMapObjectInternal(int itemid, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod, boolean mesos) {
        // dropping mesos
        // BF 00 01 01 00 00 00 01 0A 00 00 00 24 46 32 00 00 84 FF 70 00 00 00
        // 00 00 84 FF 70 00 00 00 00
        // dropping maple stars
        // BF 00 00 02 00 00 00 00 FB 95 1F 00 24 46 32 00 00 84 FF 70 00 00 00
        // 00 00 84 FF 70 00 00 00 00 80 05 BB 46 E6 17 02 00
        // killing monster (0F 2C 67 00)
        // BF 00 01 2C 03 00 00 00 6D 09 3D 00 24 46 32 00 00 A3 02 6C FF 0F 2C
        // 67 00 A3 02 94 FF 89 01 00 80 05 BB 46 E6 17 02 01

        // 4000109
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        // mplew.write(1); // 1 with animation, 2 without o.o
        mplew.write(mod);
        mplew.writeInt(itemoid);
        mplew.write(mesos ? 1 : 0); // 1 = mesos, 0 =item
        mplew.writeInt(itemid);
        mplew.writeInt(ownerid); // owner charid
        mplew.write(0);
        mplew.writeShort(dropto.x);
        mplew.writeShort(dropto.y);
        if (mod != 2) {
            mplew.writeInt(ownerid);
            mplew.writeShort(dropfrom.x);
            mplew.writeShort(dropfrom.y);
        } else {
            mplew.writeInt(dropperoid);
        }
        mplew.write(0);
        if (mod != 2) {
            mplew.write(0); //fuck knows
            mplew.write(1); //PET Meso pickup
        }
        if (!mesos) {
            mplew.write(ITEM_MAGIC);
            // TODO getTheExpirationTimeFromSomewhere o.o
            addExpirationTime(mplew, System.currentTimeMillis(), false);
            mplew.write(1); //pet EQP pickup
        }

        return mplew.getPacket();
    }

    /* (non-javadoc)
     * TODO: make MapleCharacter a mapobject, remove the need for passing oid
     * here.
     */
    /**
     * Gets a packet spawning a player as a mapobject to other clients.
     *
     * @param chr The character to spawn to other clients.
     * @return The spawn player packet.
     */
    public static MaplePacket spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeMapleAsciiString(chr.getName());
        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("");
            mplew.write(new byte[6]);
        } else {
            MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());

            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeMapleAsciiString("");
                mplew.write(new byte[6]);
            }
        }
        mplew.writeInt(0);
        mplew.writeInt(1);
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            mplew.write(2);
        } else {
            mplew.write(0);
        }
        mplew.writeShort(0);
        mplew.write(0xF8);
        long buffmask = 0;
        Integer buffvalue = null;
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
        }
        mplew.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
        if (buffvalue != null) {
            if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
                mplew.writeShort(buffvalue);
            } else {
                mplew.write(buffvalue.byteValue());
            }
        }
        mplew.writeInt((int) (buffmask & 0xffffffffL));
        int CHAR_MAGIC_SPAWN = new Random().nextInt();
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        MapleMount mount = chr.getMount();
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && mount != null) {
            mplew.writeInt(mount.getId());
            mplew.writeInt(mount.getSkillId());
            mplew.writeInt(0x34F9D6ED);
        } else {
            mplew.writeLong(0);
            mplew.writeInt(CHAR_MAGIC_SPAWN);
        }
        mplew.writeLong(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeInt(0);
        mplew.write(0x40);
        mplew.write(1);
        addCharLook(mplew, chr, false);
        mplew.writeInt(0);
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(chr.getChair());
        mplew.writeShort(chr.getPosition().x);
        mplew.writeShort(chr.getPosition().y);
        mplew.write(chr.getStance());
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeLong(0);
        if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr)) {
            if (chr.getPlayerShop().hasFreeSlot()) {
                addAnnounceBox(mplew, chr.getPlayerShop(), 4);
            } else {
                addAnnounceBox(mplew, chr.getPlayerShop(), 1);
            }
        } else if (chr.getMiniGame() != null && chr.getMiniGame().isOwner(chr)) {
            if (chr.getMiniGame().hasFreeSlot()) {
                addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 1, 0);
            } else {
                addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 2, 1);
            }
        } else {
            mplew.write(0);
        }
        mplew.writeShort(0);
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        List<MapleRing> rings = new ArrayList<MapleRing>();
        for (Item item : equipped) {
            if (((IEquip) item).getRingId() > -1) {
                rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
            }
        }
        Collections.sort(rings);
        // [01] [07 6C 31 00] [00 00 00 00] [08 6C 31 00] [00 00 00 00] [C1 F7 10 00] [01] [3B 13 32 00] [00 00 00 00] [3C 13 32 00] [00 00 00 00] [E0 FA 10 00] 00 00
        if (rings.size() > 0) {
            mplew.write(0);
            for (MapleRing ring : rings) {
                mplew.write(1);
                mplew.writeInt(ring.getRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getPartnerRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getItemId());
            }
            mplew.writeShort(0);
        } else {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    /**
     * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to add an announcement box to.
     * @param shop The shop to announce.
     */
    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
        // 00: no game
        // 01: omok game
        // 02: card game
        // 04: shop
        mplew.write(4);
        mplew.writeInt(shop.getObjectId()); // gameid/shopid

        mplew.writeMapleAsciiString(shop.getDescription()); // desc
        // 00: public
        // 01: private

        mplew.write(0);
        // 00: red 4x3
        // 01: green 5x4
        // 02: blue 6x5
        // omok:
        // 00: normal
        mplew.write(0);
        // first slot: 1/2/3/4
        // second slot: 1/2/3/4
        mplew.write(1);
        //mplew.write(4);
        mplew.write(availability);
        // 0: open
        // 1: in progress
        mplew.write(0);
    }

    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int gametype, int type, int ammount, int joinable) {
        // 00: no game 1 0 0
        // 01: omok game
        // 02: card game
        // 04: shop
        mplew.write(gametype);
        mplew.writeInt(game.getObjectId()); // gameid/shopid

        mplew.writeMapleAsciiString(game.getDescription()); // desc
        // 00: public
        // 01: private

        mplew.write(0);
        // 00: red 4x3
        // 01: green 5x4
        // 02: blue 6x5
        // omok:
        // 00: normal
        mplew.write(type);
        // first slot: 1/2/3/4
        // second slot: 1/2/3/4
        mplew.write(ammount);
        mplew.write(2);
        // 0: open
        // 1: in progress
        mplew.write(joinable);
    }

    public static MaplePacket facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());

        mplew.writeInt(from.getId());
        mplew.writeInt(expression);

        return mplew.getPacket();
    }

    private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static MaplePacket movePlayer(int cid, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static MaplePacket moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static MaplePacket moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int oid, Point startPos, List<LifeMovementFragment> moves) {
        /*
         * A0 00 C8 00 00 00 00 FF 00 00 00 00 48 02 7D FE 02 00 1C 02 7D FE 9C FF 00 00 2A 00 03 BD 01 00 DC 01 7D FE
         * 9C FF 00 00 2B 00 03 7B 02
         */
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
        // mplew.writeShort(0xA2); // 47 a0
        mplew.writeInt(oid);
        mplew.write(useskill);
        mplew.write(skill);
        mplew.write(skill_1);
        mplew.write(skill_2);
        mplew.write(skill_3);
        mplew.write(0);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static MaplePacket summonAttack(int cid, int summonSkillId, int newStance, List<SummonAttackEntry> allDamage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        mplew.write(allDamage.size());
        for (SummonAttackEntry attackEntry : allDamage) {
            mplew.writeInt(attackEntry.getMonsterOid()); // oid
            mplew.write(6); // who knows
            mplew.writeInt(attackEntry.getDamage()); // damage
        }
        return mplew.getPacket();
    }

    public static MaplePacket closeRangeAttack(int cid, int skill, int stance, int numAttackedAndDamage, List<Pair<Integer, List<Integer>>> damage, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        if (skill == 4211006) { // meso explosion
            addMesoExplosion(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed);
        } else {
            addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed);
        }
        return mplew.getPacket();
    }

    public static MaplePacket rangedAttack(int cid, int skill, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed) {
        // 7E 00 30 75 00 00 01 00 97 04 0A CB 72 1F 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        // mplew.writeShort(0x80); // 47 7E
        addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, projectile, damage, speed);

        return mplew.getPacket();
    }

    public static MaplePacket magicAttack(int cid, int skill, int stance, int numAttackedAndDamage, List<Pair<Integer, List<Integer>>> damage, int charge, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        // mplew.writeShort(0x81);
        addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed);
        if (charge != -1) {
            mplew.writeInt(charge);
        }

        return mplew.getPacket();
    }

    private static void addAttackBody(LittleEndianWriter lew, int cid, int skill, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed) {
        lew.writeInt(cid);
        lew.write(numAttackedAndDamage);
        if (skill > 0) {
            lew.write(0xFF); // too low and some skills don't work (?)

            lew.writeInt(skill);
        } else {
            lew.write(0);
        }
        lew.write(0);
        lew.write(stance);
        lew.write(speed);
        lew.write(0x0A);
        //lew.write(0);
        lew.writeInt(projectile);

        for (Pair<Integer, List<Integer>> oned : damage) {
            if (oned.getRight() != null) {
                lew.writeInt(oned.getLeft().intValue());
                lew.write(0xFF);
                for (Integer eachd : oned.getRight()) {
                    // highest bit set = crit
                    lew.writeInt(eachd.intValue());
                }
            }
        }
    }

    private static void addMesoExplosion(LittleEndianWriter lew, int cid, int skill, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed) {
        // 7A 00 6B F4 0C 00 22 1E 3E 41 40 00 38 04 0A 00 00 00 00 44 B0 04 00
        // 06 02 E6 00 00 00 D0 00 00 00 F2 46 0E 00 06 02 D3 00 00 00 3B 01 00
        // 00
        // 7A 00 6B F4 0C 00 00 1E 3E 41 40 00 38 04 0A 00 00 00 00
        lew.writeInt(cid);
        lew.write(numAttackedAndDamage);
        lew.write(0x1E);
        lew.writeInt(skill);
        lew.write(0);
        lew.write(stance);
        lew.write(speed);
        lew.write(0x0A);
        lew.writeInt(projectile);

        for (Pair<Integer, List<Integer>> oned : damage) {
            if (oned.getRight() != null) {
                lew.writeInt(oned.getLeft().intValue());
                lew.write(0xFF);
                lew.write(oned.getRight().size());
                for (Integer eachd : oned.getRight()) {
                    lew.writeInt(eachd.intValue());
                }
            }
        }

    }

    public static MaplePacket getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size()); // item count
        for (MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            if (!ii.isThrowingStar(item.getItemId()) && !ii.isBullet(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o

                mplew.writeShort(item.getBuyable());
            } else {
                mplew.writeShort(0);
                mplew.writeInt(0);
                // o.O getPrice sometimes returns the unitPrice not the price
                mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }

        return mplew.getPacket();
    }

    /**
     * code (8 = sell, 0 = buy, 0x20 = due to an error the trade did not happen
     * o.o)
     *
     * @param code
     * @return
     */
    public static MaplePacket confirmShopTransaction(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        // mplew.writeShort(0xE6); // 47 E4
        mplew.write(code); // recharge == 8?

        return mplew.getPacket();
    }

    /*
     * 19 reference 00 01 00 = new while adding 01 01 00 = add from drop 00 01 01 = update count 00 01 03 = clear slot
     * 01 01 02 = move to empty slot 01 02 03 = move and merge 01 02 01 = move and merge with rest
     */
    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item) {
        return addInventorySlot(type, item, false);
    }

    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(HexTool.getByteArrayFromHexString("01 00")); // add mode
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        addItemInfo(mplew, item, true, false);

        return mplew.getPacket();
    }

    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item) {
        return updateInventorySlot(type, item, false);
    }

    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(HexTool.getByteArrayFromHexString("01 01")); // update
        // mode
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        mplew.write(0); // ?
        mplew.writeShort(item.getQuantity());

        return mplew.getPacket();
    }

    public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
        return moveInventoryItem(type, src, dst, (byte) -1);
    }

    public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst, byte equipIndicator) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 02"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.write(equipIndicator);
        }

        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeInventoryItem(MapleInventoryType type, byte src, byte dst, short total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.write(1); // merge mode?
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(total);

        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeWithRestInventoryItem(MapleInventoryType type, byte src, byte dst, short srcQ, short dstQ) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 01"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(srcQ);
        mplew.write(HexTool.getByteArrayFromHexString("01"));
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(dstQ);

        return mplew.getPacket();
    }

    public static MaplePacket clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(HexTool.getByteArrayFromHexString("01 03"));
        mplew.write(type.getType());
        mplew.writeShort(slot);

        return mplew.getPacket();
    }

    public static MaplePacket scrolledItem(IItem scroll, IItem item, boolean destroyed) {
        // 18 00 01 02 03 02 08 00 03 01 F7 FF 01
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1); // fromdrop always true
        mplew.write(destroyed ? 2 : 3);
        mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
        mplew.write(MapleInventoryType.USE.getType());
        mplew.writeShort(scroll.getPosition());
        if (scroll.getQuantity() > 0) {
            mplew.writeShort(scroll.getQuantity());
        }
        mplew.write(3);
        if (!destroyed) {
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            mplew.write(0);
        }
        mplew.write(MapleInventoryType.EQUIP.getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            addItemInfo(mplew, item, true, true);
        }
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS:
                mplew.writeShort(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case FAIL:
                mplew.writeShort(0);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case CURSE:
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            default:
                throw new IllegalArgumentException("effect in illegal range");
        }

        return mplew.getPacket();
    }

    public static MaplePacket removePlayerFromMap(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        // mplew.writeShort(0x65); // 47 63
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
     * 4 - explode<br/> cid is ignored for 0 and 1
     *
     * @param oid
     * @param animation
     * @param cid
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, false, 0);
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
     * 4 - explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet
     * as true will make a pet pick up the item.
     *
     * @param oid
     * @param animation
     * @param cid
     * @param pet
     * @param slot
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // expire
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (pet) {
                mplew.write(slot);
            }
        }

        return mplew.getPacket();
    }

    public static MaplePacket updateCharLook(MapleCharacter chr) {
        // 88 00 80 74 03 00 01 00 00 19 50 00 00 00 67 75 00 00 02 34 71 0F 00
        // 04 59 BF 0F 00 05 AB 05 10 00 07 8C 5B
        // 10 00 08 F4 82 10 00 09 E7 D0 10 00 0A BE A9 10 00 0B 0C 05 14 00 FF
        // FF 00 00 00 00 00 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        addCharLook(mplew, chr, false);
        //mplew.writeShort(0);
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        List<MapleRing> rings = new ArrayList<MapleRing>();
        for (Item item : equipped) {
            if (((IEquip) item).getRingId() > -1) {
                rings.add(MapleRing.loadFromDb(((IEquip) item).getRingId()));
            }
        }
        Collections.sort(rings);
        /*
        97 00
        77 6B 2E 00
        01 00 00 20 4E 00 00 00 04 76 00 00 05 C8 DE 0F 00 06 DD 2C 10 00 07 B7 5B 10 00 08 92 82 10 00 0B F0 4E 16 00 0C E0 FA 10 00 0D C1 F7 10 00 0E 2E 7F 1B 00 11 D7 1E 11 00 17 20 A6 1B 00 1A 24 A6 1B 00 FF FF 00 00 00 00 57 4B 4C 00 00 00 00 00 00 00 00 00
        01
        07 6C 31 00
        00 00 00 00
        08 6C 31 00
        00 00 00 00
        C1 F7 10 00
        01
        3B 13 32 00
        00 00 00 00
        3C 13 32 00
        00 00 00 00
        E0 FA 10 00
        00*/
        if (rings.size() > 0) {
            for (MapleRing ring : rings) {
                mplew.write(1);
                mplew.writeInt(ring.getRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getPartnerRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getItemId());
            }
        } else {
            mplew.write(0);
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static MaplePacket dropInventoryItem(MapleInventoryType type, short src) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        // mplew.writeShort(0x19);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        if (src < 0) {
            mplew.write(1);
        }

        return mplew.getPacket();
    }

    public static MaplePacket dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01"));
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());

        return mplew.getPacket();
    }

    public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
        // 82 00 30 C0 23 00 FF 00 00 00 00 B4 34 03 00 01 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        // mplew.writeShort(0x84); // 47 82
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(direction);
        if (pgmr) {
            mplew.write(pgmr_1);
            mplew.write(is_pg ? 1 : 0);
            mplew.writeInt(oid);
            mplew.write(6);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
            mplew.write(0);
        } else {
            mplew.writeShort(0);
        }
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }

        return mplew.getPacket();
    }

    public static MaplePacket charNameResponse(String charname, boolean nameUsed) {
        // 0D 00 0C 00 42 6C 61 62 6C 75 62 62 31 32 33 34 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    public static MaplePacket addNewCharEntry(MapleCharacter chr, boolean worked) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(worked ? 0 : 1);
        addCharEntry(mplew, chr);

        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket startQuest(MapleCharacter c, short quest) {
        // [24 00] [01] [69 08] [01 00] [00]
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // mplew.writeShort(0x21);
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.writeShort(1);
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * state 0 = del ok state 12 = invalid bday
     *
     * @param cid
     * @param state
     * @return
     */
    public static MaplePacket deleteCharResponse(int cid, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);
        return mplew.getPacket();
    }

    public static MaplePacket charInfo(MapleCharacter chr, boolean isSelf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        // mplew.writeShort(0x31);
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob().getId());
        mplew.writeShort(chr.getFame());
        mplew.write(chr.isMarried()); // heart red or gray
        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("-"); // guild
        } else {
            MapleGuildSummary gs = null;
            gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
            } else {
                mplew.writeMapleAsciiString("-"); // guild
            }
        }
        mplew.writeMapleAsciiString(""); // Alliance
        mplew.write(isSelf ? 1 : 0);
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.write(pets[i].getUniqueId());
                mplew.writeInt(pets[i].getItemId()); // petid
                mplew.writeMapleAsciiString(pets[i].getName());
                mplew.write(pets[i].getLevel()); // pet level
                mplew.writeShort(pets[i].getCloseness()); // pet closeness
                mplew.write(pets[i].getFullness()); // pet fullness
                mplew.writeShort(0); // ??
                mplew.writeInt(0);
            }
        }
        mplew.write(0); //end of pets
        if (chr.getMount() != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            mplew.write(chr.getMount().getId()); //mount
            mplew.writeInt(chr.getMount().getLevel()); //level
            mplew.writeInt(chr.getMount().getExp()); //exp
            mplew.writeInt(chr.getMount().getTiredness()); //tiredness
        }
        mplew.write(0); //end of mounts
        mplew.writeShort(0);
        try { // wishlists
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wishlist WHERE charid = ?");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next()) {
                i++;
            }
            mplew.write(i);//size (number of items in the wishlist)
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.info("Error getting wishlist data:", e);
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wishlist WHERE charid = ? ORDER BY sn DESC");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mplew.writeInt(rs.getInt("sn"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.info("Error getting wishlist data:", e);
        }
        mplew.writeLong(1);
        mplew.writeLong(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket giveEnergyCharge(int barammount, int test2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString("1D 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00"));
        mplew.writeShort(barammount); // 0=no bar, 10000=full bar
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeInt(test2);
        mplew.writeShort(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean morph, boolean ismount, MapleMount mount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        if (!ismount) {
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                mplew.writeShort(statup.getRight().shortValue());
                mplew.writeInt(buffid);
                mplew.writeInt(bufflength);
            }
            mplew.writeShort(0); // ??? wk charges have 600 here o.o
            mplew.write(0); // combo 600, too
            mplew.write(0); // new in v0.56
            mplew.write(0);
        } else {
            if (ismount) {
                mplew.writeShort(0);
                mplew.writeInt(mount.getItemId());
                mplew.writeInt(mount.getSkillId());
                mplew.writeInt(0);
                mplew.writeShort(0);
                mplew.write(0);
            } else {
                log.error("Something went wrong with making the mount packet.");
                return null;
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, MapleMount mount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.writeShort(0);
        mplew.writeInt(mount.getItemId());
        mplew.writeInt(mount.getSkillId());
        mplew.writeInt(0x2D4DFC2A);
        mplew.writeShort(0); // same as give_buff
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket forfeitQuest(MapleCharacter c, short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket completeQuest(MapleCharacter c, short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(2);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @param npc
     * @param progress
     * @return
     */
    // frz note, 0.52 transition: this is only used when starting a quest and
    // seems to have no effect, is it needed?
    public static MaplePacket updateQuestInfo(MapleCharacter c, short quest, int npc, byte progress) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    private static <E extends LongValueHolder> long getLongMask(List<Pair<E, Integer>> statups) {
        long mask = 0;
        for (Pair<E, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskFromList(List<E> statups) {
        long mask = 0;
        for (E statup : statups) {
            mask |= statup.getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskD(List<Pair<MapleDisease, Integer>> statups) {
        long mask = 0;
        for (Pair<MapleDisease, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskFromListD(List<MapleDisease> statups) {
        long mask = 0;
        for (MapleDisease statup : statups) {
            mask |= statup.getValue();
        }
        return mask;
    }

    public static MaplePacket giveBuffTest(int buffid, long mask) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.writeShort(1);
        mplew.writeInt(buffid);
        mplew.writeInt(60);
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.write(0); // combo 600, too
        mplew.write(0); // new in v0.56
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * It is important that statups is in the correct order (see decleration
     * order in MapleBuffStat) since this method doesn't do automagical
     * reordering.
     *
     * @param buffid
     * @param bufflength
     * @param statups
     * @param morph
     * @return
     */
    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean morph) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        if (bufflength == 1004 || bufflength == 5221006) {
            mplew.writeInt(0x2F258DB9);
        } else {
            mplew.writeShort(0); // ??? wk charges have 600 here o.o
        }
        mplew.write(0); // combo 600, too
        mplew.write(0); // new in v0.56
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket giveDebuff(List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleDisease, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillLevel());
            mplew.writeInt((int) skill.getDuration());
        }
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.writeShort(900);//Delay
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket giveForeignDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (@SuppressWarnings("unused") Pair<MapleDisease, Integer> statup : statups) {
            //mplew.writeShort(statup.getRight().byteValue());
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillLevel());
        }
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(900);//Delay

        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignDebuff(int cid, List<MapleDisease> statups) {
        // 8A 00 24 46 32 00 80 04 00 00 00 00 00 00 F4 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskFromListD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        return mplew.getPacket();
    }

    public static MaplePacket showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, int itemId, int skillId) {
        // 8A 00 24 46 32 00 80 04 00 00 00 00 00 00 F4 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.writeShort(0);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeInt(0x2D4DFC2A);
        mplew.writeShort(0); // same as give_buff
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups, boolean morph) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        //mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (morph) {
                mplew.write(statup.getRight().byteValue());
            } else {
                mplew.writeShort(statup.getRight().shortValue());
            }
        }
        mplew.writeShort(0);
        if (morph) {
            mplew.writeShort(0);
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        // 8A 00 24 46 32 00 80 04 00 00 00 00 00 00 F4 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskFromList(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        return mplew.getPacket();
    }

    public static MaplePacket cancelBuff(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        long mask = getLongMaskFromList(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.write(3); // wtf?
        return mplew.getPacket();
    }

    public static MaplePacket cancelDebuff(List<MapleDisease> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        long mask = getLongMaskFromListD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("06 08"));
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);

        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        log.info("player shop send packet: \n" + mplew.toString());
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopRemoveVisitor(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 0" + slot));
        log.info("player shop send packet: \n" + mplew.toString());
        return mplew.getPacket();
    }

    public static MaplePacket getTradePartnerAdd(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 01"));// 00 04 88 4E 00"));
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());

        return mplew.getPacket();
    }

    public static MaplePacket getTradeInvite(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("02 03"));
        mplew.writeMapleAsciiString(c.getName());
        mplew.write(HexTool.getByteArrayFromHexString("B7 50 00 00"));

        return mplew.getPacket();
    }

    public static MaplePacket getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xF);
        mplew.write(number);
        mplew.writeInt(meso);

        return mplew.getPacket();
    }

    public static MaplePacket getTradeItemAdd(byte number, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xE);
        mplew.write(number);
        // mplew.write(1);
        addItemInfo(mplew, item);

        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopItemUpdate(MaplePlayerShop shop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x16);
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true, true);
        }

        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param shop
     * @param owner
     * @return
     */
    public static MaplePacket getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 04 04"));
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(1);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(shop.getDescription());
        List<MaplePlayerShopItem> items = shop.getItems();
        mplew.write(0x10);
        mplew.write(items.size());
        for (MaplePlayerShopItem item : items) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 03 02"));
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
        }
        mplew.write(number);
        /*if (number == 1) {
        mplew.write(0);
        mplew.writeInt(c.getPlayer().getId());
        }*/
        addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(0xFF);

        return mplew.getPacket();
    }

    public static MaplePacket getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x10);

        return mplew.getPacket();
    }

    public static MaplePacket getTradeCompletion(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(number);
        mplew.write(6);

        return mplew.getPacket();
    }

    public static MaplePacket getTradeCancel(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(number);
        mplew.write(2);

        return mplew.getPacket();
    }

    public static MaplePacket addCharBox(MapleCharacter c, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getPlayerShop(), type);
        return mplew.getPacket();
    }

    public static MaplePacket removeCharBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalk(int npc, byte msgType, String talk, String endBytes) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));

        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkStyle(int npc, String talk, int styles[]) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(7);
        mplew.writeMapleAsciiString(talk);
        mplew.write(styles.length);
        for (int i = 0; i < styles.length; i++) {
            mplew.writeInt(styles[i]);
        }

        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(3);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkText(int npc, String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(2);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static MaplePacket showLevelup(int cid) {
        return showForeignEffect(cid, 0);
    }

    public static MaplePacket showJobChange(int cid) {
        return showForeignEffect(cid, 8);
    }

    public static MaplePacket showForeignEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid) {
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid); // ?
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1); // probably buff level but we don't know it and it doesn't really matter
        if (direction != (byte) 3) {
            mplew.write(direction);
        }

        return mplew.getPacket();
    }

    public static MaplePacket showOwnBuffEffect(int skillid, int effectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1); // probably buff level but we don't know it and it doesn't really matter

        return mplew.getPacket();
    }

    public static MaplePacket showOwnBerserk(int skilllevel, boolean Berserk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);

        return mplew.getPacket();
    }

    public static MaplePacket showBerserk(int cid, int skilllevel, boolean Berserk) {
        // [99 00] [5D 94 27 00] [01] [46 24 14 00] [14] [01]
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);

        return mplew.getPacket();
    }

    public static MaplePacket updateSkill(int skillid, int level, int masterlevel) {
        // 1E 00 01 01 00 E9 03 00 00 01 00 00 00 00 00 00 00 01
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket updateQuestMobKills(MapleQuestStatus status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(status.getQuest().getId());
        mplew.write(1);
        String killStr = "";
        for (int kills : status.getMobKills().values()) {
            killStr += StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3);
        }
        mplew.writeMapleAsciiString(killStr);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getShowQuestCompletion(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);
        return mplew.getPacket();
    }

    public static MaplePacket getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
        mplew.write(0);
        for (int x = 0; x < 90; x++) {
            MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
            if (binding != null) {
                mplew.write(binding.getType());
                mplew.writeInt(binding.getAction());
            } else {
                mplew.write(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getWhisper(String sender, int channel, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1); // I guess this is the channel
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    /**
     *
     * @param target name of the target character
     * @param reply error code: 0x0 = cannot find char, 0x1 = success
     * @return the MaplePacket
     */
    public static MaplePacket getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        return mplew.getPacket();
    }

    public static MaplePacket getFindReplyWithMap(String target, int mapid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.write(new byte[8]);
        return mplew.getPacket();
    }

    public static MaplePacket getFindReply(String target, int channel) {
        // Received UNKNOWN (1205941596.79689): (25)
        // 54 00 09 07 00 64 61 76 74 73 61 69 01 86 7F 3D 36 D5 02 00 00 22 00
        // 00 00
        // T....davtsai..=6...."...
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);

        return mplew.getPacket();
    }

    public static MaplePacket getInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static MaplePacket showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public static MaplePacket getShowInventoryStatus(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static MaplePacket getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x15);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(0x7E);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.writeShort(0);
        mplew.write((byte) items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true, true);
        }
        mplew.writeShort(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket getStorageFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x10);

        return mplew.getPacket();
    }

    public static MaplePacket mesoStorage(byte slots, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x12);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);

        return mplew.getPacket();
    }

    public static MaplePacket storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0xC);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true, true);
        // mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static MaplePacket takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true, true);
        // mplew.write(0);
        }

        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param remhp in %
     * @return
     */
    public static MaplePacket showMonsterHP(int oid, int remhppercentage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(oid);
        mplew.write(remhppercentage);

        return mplew.getPacket();
    }

    public static MaplePacket showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
        //53 00 05 21 B3 81 00 46 F2 5E 01 C0 F3 5E 01 04 01
        //00 81 B3 21 = 8500001 = Pap monster ID
        //01 5E F3 C0 = 23,000,000 = Pap max HP
        //04, 01 - boss bar color/background color as provided in WZ
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(oid);
        mplew.writeInt(currHP);
        mplew.writeInt(maxHP);
        mplew.write(tagColor);
        mplew.write(tagBgColor);

        return mplew.getPacket();
    }

    public static MaplePacket giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    /**
     * status can be: <br>
     * 0: ok, use giveFameResponse<br>
     * 1: the username is incorrectly entered<br>
     * 2: users under level 15 are unable to toggle with fame.<br>
     * 3: can't raise or drop fame anymore today.<br>
     * 4: can't raise or drop fame for this character for this month anymore.<br>
     * 5: received fame, use receiveFame()<br>
     * 6: level of fame neither has been raised nor dropped due to an unexpected
     * error
     *
     * @param status
     * @return
     */
    public static MaplePacket giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);

        return mplew.getPacket();
    }

    public static MaplePacket receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static MaplePacket partyCreated() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeShort(0x8b);
        mplew.writeShort(2);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static MaplePacket partyInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.write(0);

        return mplew.getPacket();
    }

    /**
     * 10: A beginner can't create a party.
     * 1/11/14/19: Your request for a party didn't work due to an unexpected error.
     * 13: You have yet to join a party.
     * 16: Already have joined a party.
     * 17: The party you're trying to join is already in full capacity.
     * 19: Unable to find the requested character in this channel.
     *
     * @param message
     * @return
     */
    public static MaplePacket partyStatusMessage(int message) {
        // 32 00 08 DA 14 00 00 FF C9 9A 3B FF C9 9A 3B 22 03 6E 67
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);

        return mplew.getPacket();
    }

    /**
     * 23: 'Char' have denied request to the party.
     *
     * @param message
     * @param charname
     * @return
     */
    public static MaplePacket partyStatusMessage(int message, String charname) {
        // 32 00 08 DA 14 00 00 FF C9 9A 3B FF C9 9A 3B 22 03 6E 67
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charname);

        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
        List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(StringUtil.getRightPaddedStr(partychar.getName(), '\0', 13));
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-2);
            }
        }
        lew.writeInt(party.getLeader().getId());
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapid());
            } else {
                lew.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                lew.writeInt(partychar.getDoorPosition().x);
                lew.writeInt(partychar.getDoorPosition().y);
            } else {
                lew.writeInt(0);
                lew.writeInt(0);
                lew.writeInt(0);
                lew.writeInt(0);
            }
        }
    }

    public static MaplePacket updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                mplew.write(0xC);
                mplew.writeInt(40546);
                mplew.writeInt(target.getId());

                if (op == PartyOperation.DISBAND) {
                    mplew.write(0);
                    mplew.writeInt(party.getId());
                } else {
                    mplew.write(1);
                    if (op == PartyOperation.EXPEL) {
                        mplew.write(1);
                    } else {
                        mplew.write(0);
                    }
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, false);
                // addLeavePartyTail(mplew);
                }

                break;
            case JOIN:
                mplew.write(0xF);
                mplew.writeInt(40546);
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                // addJoinPartyTail(mplew);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                mplew.write(0x7);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, false);
                break;

        }

        return mplew.getPacket();
    }

    public static MaplePacket partyPortal(int townId, int targetId, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x22);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writeShort(position.x);
        mplew.writeShort(position.y);

        return mplew.getPacket();
    }

    public static MaplePacket updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);

        return mplew.getPacket();
    }

    /**
     * mode: 0 buddychat; 1 partychat; 2 guildchat
     *
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static MaplePacket multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);

        return mplew.getPacket();
    }

    public static MaplePacket applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stats, int skill, boolean monsterSkill, int delay) {
        return applyMonsterStatus(oid, stats, skill, monsterSkill, delay, null);
    }

    public static MaplePacket applyMonsterStatusTest(int oid, int mask, int delay, MobSkill mobskill, int value) {
        // 9B 00 67 40 6F 00 80 00 00 00 01 00 FD FE 30 00 08 00 64 00 01
        // 1D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 01 00 79 00 01 00 B4 78 00 00 00 00 84 03
        // B4 00 A8 90 03 00 00 00 04 00 01 00 8C 00 03 00 14 00 4C 04 02
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mask);
        mplew.writeShort(1);
        mplew.writeShort(mobskill.getSkillId());
        mplew.writeShort(mobskill.getSkillLevel());
        mplew.writeShort(0); // as this looks similar to giveBuff this might actually be the buffTime but it's not displayed anywhere
        mplew.writeShort(delay); // delay in ms
        mplew.write(1); // ?

        return mplew.getPacket();
    }

    public static MaplePacket applyMonsterStatusTest2(int oid, int mask, int skill, int value) {
        // 9B 00 67 40 6F 00 80 00 00 00 01 00 FD FE 30 00 08 00 64 00 01
        // 1D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 01 00 79 00 01 00 B4 78 00 00 00 00 84 03
        // B4 00 A8 90 03 00 00 00 04 00 01 00 8C 00 03 00 14 00 4C 04 02
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mask);
        mplew.writeShort(value);
        mplew.writeInt(skill);
        mplew.writeShort(0); // as this looks similar to giveBuff this might actually be the buffTime but it's not displayed anywhere
        mplew.writeShort(0); // delay in ms
        mplew.write(1); // ?

        return mplew.getPacket();
    }

    public static MaplePacket applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stats, int skill, boolean monsterSkill, int delay, MobSkill mobskill) {
        // 9B 00 67 40 6F 00 80 00 00 00 01 00 FD FE 30 00 08 00 64 00 01
        // 1D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 01 00 79 00 01 00 B4 78 00 00 00 00 84 03
        // B4 00 A8 90 03 00 00 00 04 00 01 00 8C 00 03 00 14 00 4C 04 02
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        int mask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            mask |= stat.getValue();
        }
        mplew.writeInt(mask);
        for (Integer val : stats.values()) {
            mplew.writeShort(val);
            if (monsterSkill) {
                mplew.writeShort(mobskill.getSkillId());
                mplew.writeShort(mobskill.getSkillLevel());
            } else {
                mplew.writeInt(skill);
            }
            mplew.writeShort(0); // as this looks similar to giveBuff this
        // might actually be the buffTime but it's not displayed anywhere

        }
        mplew.writeShort(delay); // delay in ms
        mplew.write(1); // ?

        return mplew.getPacket();
    }

    public static MaplePacket cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        int mask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            mask |= stat.getValue();
        }
        mplew.writeInt(mask);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket getClock(int time) { // time in seconds
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static MaplePacket getClockTime(int hour, int min, int sec) { // Current Time
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type

        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        return mplew.getPacket();
    }

    public static MaplePacket giveDash(List<Pair<MapleBuffStat, Integer>> statups, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeLong(MapleBuffStat.DASH.getValue());
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            if (stat.getLeft().getValue() != MapleBuffStat.DASH.getValue()) {
                mplew.writeInt(stat.getRight().shortValue());
                mplew.writeInt(5001005);
                mplew.write(HexTool.getByteArrayFromHexString("1A 7C 8D 35"));
                mplew.writeShort(duration);
            }
        }
        mplew.writeShort(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket spawnMist(int oid, int ownerCid, int skillId, Rectangle mistPosition, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(oid); // maybe this should actually be the "mistid" -
        // seems to always be 1 with only one mist in the map...
        mplew.writeInt(ownerCid); // probably only intresting for smokescreen
        mplew.writeInt(skillId);
        mplew.write(level); // who knows
        mplew.writeShort(8); // ???
        mplew.writeInt(mistPosition.x); // left position
        mplew.writeInt(mistPosition.y); // bottom position
        mplew.writeInt(mistPosition.x + mistPosition.width); // left position
        mplew.writeInt(mistPosition.y + mistPosition.height); // upper position
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static MaplePacket removeMist(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static MaplePacket damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        // 77 00 29 1D 02 00 FA FE 30 00 00 10 00 00 00 BF 70 8F 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket damageMonster(int oid, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(damage);

        return mplew.getPacket();
    }

    public static MaplePacket healMonster(int oid, int heal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(-heal);

        return mplew.getPacket();
    }

    public static MaplePacket updateBuddylist(Collection<BuddylistEntry> buddylist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());
        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId()); // cid
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13));
                mplew.write(0);
                mplew.writeInt(buddy.getChannel() - 1);
            }
        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveDash(List<Pair<MapleBuffStat, Integer>> statups, int x, int y, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        long mask = getLongMask(statups);
        mplew.writeLong(mask);
        mplew.writeShort(0);
        mplew.writeInt(x);
        mplew.writeInt(5001005);
        mplew.write(HexTool.getByteArrayFromHexString("1A 7C 8D 35"));
        mplew.writeShort(duration);
        mplew.writeInt(y);
        mplew.writeInt(5001005);
        mplew.write(HexTool.getByteArrayFromHexString("1A 7C 8D 35"));
        mplew.writeShort(duration);
        mplew.writeShort(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket showDashEffecttoOthers(int cid, int x, int y, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0);
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 30 00 00 00 00"));
        mplew.writeShort(0);
        mplew.writeInt(x);
        mplew.writeInt(5001005);
        mplew.write(HexTool.getByteArrayFromHexString("1A 7C 8D 35"));
        mplew.writeShort(duration);
        mplew.writeInt(y);
        mplew.writeInt(5001005);
        mplew.write(HexTool.getByteArrayFromHexString("1A 7C 8D 35"));
        mplew.writeShort(duration);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket buddylistMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    public static MaplePacket requestBuddylistAdd(int cidFrom, String nameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13));
        mplew.write(1);
        mplew.write(31);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyChannel(int characterid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }

    public static MaplePacket itemEffect(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);

        return mplew.getPacket();
    }

    public static MaplePacket showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());

        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static MaplePacket cancelChair() {
        return cancelChair(-1);
    }

    public static MaplePacket cancelChair(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());

        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }

        return mplew.getPacket();
    }

    // is there a way to spawn reactors non-animated?
    public static MaplePacket spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();

        mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getId());
        mplew.write(reactor.getState());
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static MaplePacket triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();

        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.write(5); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it

        return mplew.getPacket();
    }

    public static MaplePacket destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();

        mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);

        return mplew.getPacket();
    }

    public static MaplePacket musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static MaplePacket showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static MaplePacket playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static MaplePacket environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);

        return mplew.getPacket();
    }

    public static MaplePacket startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);

        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }

        return mplew.getPacket();
    }

    public static MaplePacket removeMapEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static MaplePacket showGuildInfo(MapleCharacter c) {
        //whatever functions calling this better make sure
        //that the character actually HAS a guild
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x1A); //signature for showing guild info
        if (c == null) { //show empty guild (used for leaving, expelled)
            mplew.write(0);
            return mplew.getPacket();
        }
        MapleGuildCharacter initiator = c.getMGC();
        MapleGuild g = c.getClient().getChannelServer().getGuild(initiator);
        if (g == null) { //failed to read from DB - don't show a guild
            mplew.write(0);
            log.warn(MapleClient.getLogMessage(c, "Couldn't load a guild"));
            return mplew.getPacket();
        } else {
            //MapleGuild holds the absolute correct value of guild rank
            //after it is initiated
            MapleGuildCharacter mgc = g.getMGC(c.getId());
            c.setGuildRank(mgc.getGuildRank());
        }
        mplew.write(1); //bInGuild
        mplew.writeInt(c.getGuildId()); //not entirely sure about this one
        mplew.writeMapleAsciiString(g.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(g.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = g.getMembers();
        mplew.write(members.size());
        //then it is the size of all the members
        for (MapleGuildCharacter mgc : members) //and each of their character ids o_O
        {
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(g.getSignature());
            mplew.writeInt(3);
        }
        mplew.writeInt(g.getCapacity());
        mplew.writeShort(g.getLogoBG());
        mplew.write(g.getLogoBGColor());
        mplew.writeShort(g.getLogo());
        mplew.write(g.getLogoColor());
        mplew.writeMapleAsciiString(g.getNotice());
        mplew.writeInt(g.getGP());
        mplew.writeInt(0);

        // System.out.println("DEBUG: showGuildInfo packet:\n" + mplew.toString());
        return mplew.getPacket();
    }

    public static MaplePacket guildMemberOnline(int gid, int cid, boolean bOnline) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3d);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);

        return mplew.getPacket();
    }

    public static MaplePacket guildInvite(int gid, String charName) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);

        return mplew.getPacket();
    }

    /**
     * 'Char' has denied your guild invitation.
     *
     * @param charname
     * @return
     */
    public static MaplePacket denyGuildInvitation(String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x37);
        mplew.writeMapleAsciiString(charname);

        return mplew.getPacket();
    }

    public static MaplePacket genericGuildMessage(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);

        return mplew.getPacket();
    }

    public static MaplePacket newGuildMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
        mplew.writeInt(3);

        return mplew.getPacket();
    }

    //someone leaving, mode == 0x2c for leaving, 0x2f for expelled
    public static MaplePacket memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 0x2f : 0x2c);

        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());

        return mplew.getPacket();
    }

    //rank change
    public static MaplePacket changeRank(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x40);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());

        return mplew.getPacket();
    }

    public static MaplePacket guildNotice(int gid, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x44);

        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);

        return mplew.getPacket();
    }

    public static MaplePacket guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3C);

        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        return mplew.getPacket();
    }

    public static MaplePacket rankTitleChange(int gid, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3e);
        mplew.writeInt(gid);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildDisband(int gid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x32);
        mplew.writeInt(gid);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x42);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);
        return mplew.getPacket();
    }

    public static MaplePacket guildCapacityChange(int gid, int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3a);
        mplew.writeInt(gid);
        mplew.write(capacity);

        return mplew.getPacket();
    }

    public static void addThread(MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
        mplew.writeInt(rs.getInt("localthreadid"));
        mplew.writeInt(rs.getInt("postercid"));
        mplew.writeMapleAsciiString(rs.getString("name"));
        mplew.writeLong(MaplePacketCreator.getKoreanTimestamp(rs.getLong("timestamp")));
        mplew.writeInt(rs.getInt("icon"));
        mplew.writeInt(rs.getInt("replycount"));
    }

    public static MaplePacket BBSThreadList(ResultSet rs, int start) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(0x06);
        if (!rs.last()) {
            //no result at all
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        int threadCount = rs.getRow();
        if (rs.getInt("localthreadid") == 0) { //has a notice
            mplew.write(1);
            addThread(mplew, rs);
            threadCount--; //one thread didn't count (because it's a notice)
        } else {
            mplew.write(0);
        }
        if (!rs.absolute(start + 1)) { //seek to the thread before where we start
            rs.first(); //uh, we're trying to start at a place past possible
            start = 0;
        // System.out.println("Attempting to start past threadCount");
        }
        mplew.writeInt(threadCount);
        mplew.writeInt(Math.min(10, threadCount - start));
        for (int i = 0; i < Math.min(10, threadCount - start); i++) {
            addThread(mplew, rs);
            rs.next();
        }

        return mplew.getPacket();
    }

    public static MaplePacket showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(0x07);
        mplew.writeInt(localthreadid);
        mplew.writeInt(threadRS.getInt("postercid"));
        mplew.writeLong(getKoreanTimestamp(threadRS.getLong("timestamp")));
        mplew.writeMapleAsciiString(threadRS.getString("name"));
        mplew.writeMapleAsciiString(threadRS.getString("startpost"));
        mplew.writeInt(threadRS.getInt("icon"));
        if (repliesRS != null) {
            int replyCount = threadRS.getInt("replycount");
            mplew.writeInt(replyCount);
            int i;
            for (i = 0; i < replyCount && repliesRS.next(); i++) {
                mplew.writeInt(repliesRS.getInt("replyid"));
                mplew.writeInt(repliesRS.getInt("postercid"));
                mplew.writeLong(getKoreanTimestamp(repliesRS.getLong("timestamp")));
                mplew.writeMapleAsciiString(repliesRS.getString("content"));
            }
            if (i != replyCount || repliesRS.next()) {
                //in the unlikely event that we lost count of replyid
                throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
            //we need to fix the database and stop the packet sending
            //or else it'll probably error 38 whoever tries to read it

            //there is ONE case not checked, and that's when the thread
            //has a replycount of 0 and there is one or more replies to the
            //thread in bbs_replies
            }
        } else {
            mplew.writeInt(0); //0 replies
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGuildRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("GP"));
            mplew.writeInt(rs.getInt("logo"));
            mplew.writeInt(rs.getInt("logoColor"));
            mplew.writeInt(rs.getInt("logoBG"));
            mplew.writeInt(rs.getInt("logoBGColor"));
        }

        return mplew.getPacket();
    }

    public static MaplePacket updateGP(int gid, int GP) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(GP);

        return mplew.getPacket();
    }

    public static MaplePacket skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);

        return mplew.getPacket();
    }

    public static MaplePacket skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static MaplePacket showMagnet(int mobid, byte success) { // Monster Magnet
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);

        return mplew.getPacket();
    }

    /**
     * Sends a player hint.
     *
     * @param hint The hint it's going to send.
     * @param width How tall the box is going to be.
     * @param height How long the box is going to be.
     * @return The player hint packet.
     */
    public static MaplePacket sendHint(String hint, int width, int height) {
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket messengerInvite(String from, int messengerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0x00);
        mplew.writeInt(messengerid);
        mplew.write(0x00);

        return mplew.getPacket();
    }

    public static MaplePacket sendSpouseChat(MapleCharacter wife, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPOUSECHAT.getValue());
        mplew.writeMapleAsciiString(wife.getName());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static MaplePacket addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static MaplePacket removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static MaplePacket updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x07);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static MaplePacket joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static MaplePacket messengerChat(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static MaplePacket messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        return mplew.getPacket();
    }

//    public static MaplePacket warpCS(MapleClient c) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//        MapleCharacter chr = c.getPlayer();
//        mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());
//        mplew.writeLong(-1);
//        addCharStats(mplew, chr);
//        mplew.write(20); // ???
//        mplew.writeInt(chr.getMeso()); // mesos
//        mplew.write(100); // equip slots
//        mplew.write(100); // use slots
//        mplew.write(100); // set-up slots
//        mplew.write(100); // etc slots
//        mplew.write(48); // storage slots
//        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
//        Collection<IItem> equippedC = iv.list();
//        List<Item> equipped = new ArrayList<Item>(equippedC.size());
//        for (IItem item : equippedC) {
//            equipped.add((Item) item);
//        }
//        Collections.sort(equipped);
//        for (Item item : equipped) {
//            addItemInfo(mplew, item);
//        }
//        mplew.writeShort(0); // start of equip inventory
//        iv = chr.getInventory(MapleInventoryType.EQUIP);
//        for (IItem item : iv.list()) {
//            addItemInfo(mplew, item);
//        }
//        mplew.write(0); // start of use inventory
//        iv = chr.getInventory(MapleInventoryType.USE);
//        for (IItem item : iv.list()) {
//            addItemInfo(mplew, item);
//        }
//        mplew.write(0); // start of set-up inventory
//        iv = chr.getInventory(MapleInventoryType.SETUP);
//        for (IItem item : iv.list()) {
//            addItemInfo(mplew, item);
//        }
//        mplew.write(0); // start of etc inventory
//        iv = chr.getInventory(MapleInventoryType.ETC);
//        for (IItem item : iv.list()) {
//            addItemInfo(mplew, item);
//        }
//        mplew.write(0); // start of cash inventory
//        iv = chr.getInventory(MapleInventoryType.CASH);
//        for (IItem item : iv.list()) {
//            addItemInfo(mplew, item);
//        }
//        mplew.write(0);
//        Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
//        mplew.writeShort(skills.size());
//        for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
//            mplew.writeInt(skill.getKey().getId());
//            mplew.writeInt(skill.getValue().skillevel);
//            if (skill.getKey().isFourthJob()) {
//                mplew.writeInt(skill.getValue().masterlevel);
//            }
//        }
//        mplew.writeShort(0);
//        mplew.writeShort(1);
//        mplew.writeInt(662990);
//        mplew.write(HexTool.getByteArrayFromHexString("5A 42 43 44 45 46 47 48 49 4A 00 00"));
//        mplew.writeLong(0);
//        for (int i = 0; i < 15; i++) {
//            mplew.write(CHAR_INFO_MAGIC);
//        }
//        mplew.writeInt(0);
//        mplew.write(1);
//        mplew.writeMapleAsciiString(chr.getClient().getAccountName());
//        mplew.write(HexTool.getByteArrayFromHexString("96 00 00 00 38 21 9A 00 39 21 9A 00 3A 21 9A 00 3B 21 9A 00 3C 21 9A 00 3D 21 9A 00 3E 21 9A 00 3F 21 9A 00 40 21 9A 00 41 21 9A 00 42 21 9A 00 43 21 9A 00 44 21 9A 00 45 21 9A 00 46 21 9A 00 47 21 9A 00 48 21 9A 00 49 21 9A 00 4A 21 9A 00 4B 21 9A 00 4C 21 9A 00 4D 21 9A 00 4E 21 9A 00 4F 21 9A 00 50 21 9A 00 51 21 9A 00 52 21 9A 00 53 21 9A 00 54 21 9A 00 55 21 9A 00 56 21 9A 00 57 21 9A 00 58 21 9A 00 59 21 9A 00 5A 21 9A 00 5B 21 9A 00 5C 21 9A 00 5D 21 9A 00 5E 21 9A 00 5F 21 9A 00 60 21 9A 00 61 21 9A 00 62 21 9A 00 63 21 9A 00 64 21 9A 00 65 21 9A 00 66 21 9A 00 67 21 9A 00 68 21 9A 00 69 21 9A 00 6A 21 9A 00 6B 21 9A 00 6C 21 9A 00 6D 21 9A 00 6E 21 9A 00 6F 21 9A 00 70 21 9A 00 71 21 9A 00 72 21 9A 00 73 21 9A 00 74 21 9A 00 75 21 9A 00 76 21 9A 00 77 21 9A 00 78 21 9A 00 79 21 9A 00 7A 21 9A 00 7B 21 9A 00 7C 21 9A 00 7D 21 9A 00 7E 21 9A 00 7F 21 9A 00 80 21 9A 00 81 21 9A 00 82 21 9A 00 83 21 9A 00 84 21 9A 00 85 21 9A 00 86 21 9A 00 87 21 9A 00 88 21 9A 00 89 21 9A 00 8A 21 9A 00 8B 21 9A 00 8C 21 9A 00 8D 21 9A 00 8E 21 9A 00 8F 21 9A 00 90 21 9A 00 91 21 9A 00 92 21 9A 00 93 21 9A 00 94 21 9A 00 95 21 9A 00 96 21 9A 00 97 21 9A 00 98 21 9A 00 99 21 9A 00 9A 21 9A 00 9B 21 9A 00 9C 21 9A 00 9D 21 9A 00 9E 21 9A 00 9F 21 9A 00 A0 21 9A 00 A1 21 9A 00 A2 21 9A 00 A3 21 9A 00 A4 21 9A 00 A5 21 9A 00 A6 21 9A 00 A7 21 9A 00 A8 21 9A 00 A9 21 9A 00 AA 21 9A 00 AB 21 9A 00 AC 21 9A 00 AD 21 9A 00 AE 21 9A 00 AF 21 9A 00 B0 21 9A 00 B1 21 9A 00 B2 21 9A 00 B3 21 9A 00 B4 21 9A 00 B5 21 9A 00 B6 21 9A 00 B7 21 9A 00 B8 21 9A 00 B9 21 9A 00 BA 21 9A 00 BB 21 9A 00 BC 21 9A 00 BD 21 9A 00 BE 21 9A 00 BF 21 9A 00 C0 21 9A 00 C1 21 9A 00 C2 21 9A 00 C3 21 9A 00 C4 21 9A 00 C5 21 9A 00 C6 21 9A 00 C7 21 9A 00 C8 21 9A 00 C9 21 9A 00 CA 21 9A 00 CB 21 9A 00 CC 21 9A 00 CD 21 9A 00 59 01 C8 9D 98 00 00 02 00 C9 9D 98 00 00 02 00 EB 9D 98 00 00 02 00 EC 9D 98 00 00 02 00 EF 9D 98 00 00 02 00 F0 9D 98 00 00 02 00 F1 9D 98 00 00 02 00 F2 9D 98 00 00 02 00 F5 9D 98 00 00 06 00 00 F6 9D 98 00 00 06 00 00 F7 9D 98 00 00 02 00 F8 9D 98 00 00 02 00 F9 9D 98 00 00 02 00 FA 9D 98 00 00 02 00 FB 9D 98 00 00 02 00 FC 9D 98 00 00 02 00 FD 9D 98 00 00 02 00 FE 9D 98 00 00 02 00 FF 9D 98 00 00 02 00 00 9E 98 00 00 02 00 01 9E 98 00 00 02 00 02 9E 98 00 00 02 00 03 9E 98 00 00 02 00 04 9E 98 00 00 02 00 38 21 9A 00 00 02 01 39 21 9A 00 00 02 01 3A 21 9A 00 00 02 01 3B 21 9A 00 00 02 01 3C 21 9A 00 00 02 01 3D 21 9A 00 00 02 01 3E 21 9A 00 00 02 01 3F 21 9A 00 00 02 01 40 21 9A 00 00 02 01 41 21 9A 00 00 02 01 42 21 9A 00 00 02 01 43 21 9A 00 00 02 01 44 21 9A 00 00 02 01 45 21 9A 00 00 02 01 46 21 9A 00 00 02 01 47 21 9A 00 00 02 01 48 21 9A 00 00 02 01 49 21 9A 00 00 02 01 4A 21 9A 00 00 02 01 4B 21 9A 00 00 02 01 4C 21 9A 00 00 02 01 4D 21 9A 00 00 02 01 4E 21 9A 00 00 02 01 4F 21 9A 00 00 02 01 50 21 9A 00 00 02 01 51 21 9A 00 00 02 01 52 21 9A 00 00 02 01 53 21 9A 00 00 02 01 54 21 9A 00 00 02 01 55 21 9A 00 00 02 01 56 21 9A 00 00 02 01 57 21 9A 00 00 02 01 58 21 9A 00 00 02 01 59 21 9A 00 00 02 01 5A 21 9A 00 00 02 01 5B 21 9A 00 00 02 01 5C 21 9A 00 00 02 01 5D 21 9A 00 00 02 01 5E 21 9A 00 00 02 01 5F 21 9A 00 00 02 01 60 21 9A 00 00 02 01 61 21 9A 00 00 02 01 62 21 9A 00 00 02 01 63 21 9A 00 00 02 01 64 21 9A 00 00 02 01 65 21 9A 00 00 02 01 66 21 9A 00 00 02 01 67 21 9A 00 00 02 01 68 21 9A 00 00 02 01 69 21 9A 00 00 02 01 6A 21 9A 00 00 02 01 6B 21 9A 00 00 02 01 6C 21 9A 00 00 02 01 6D 21 9A 00 00 02 01 6E 21 9A 00 00 02 01 6F 21 9A 00 00 02 01 70 21 9A 00 00 02 01 71 21 9A 00 00 02 01 72 21 9A 00 00 02 01 73 21 9A 00 00 02 01 74 21 9A 00 00 02 01 75 21 9A 00 00 02 01 76 21 9A 00 00 02 01 77 21 9A 00 00 02 01 78 21 9A 00 00 02 01 79 21 9A 00 00 02 01 7A 21 9A 00 00 02 01 7B 21 9A 00 00 02 01 7C 21 9A 00 00 02 01 7D 21 9A 00 00 02 01 7E 21 9A 00 00 02 01 7F 21 9A 00 00 02 01 80 21 9A 00 00 02 01 81 21 9A 00 00 02 01 82 21 9A 00 00 02 01 83 21 9A 00 00 02 01 84 21 9A 00 00 02 01 85 21 9A 00 00 02 01 86 21 9A 00 00 02 01 87 21 9A 00 00 02 01 88 21 9A 00 00 02 01 89 21 9A 00 00 02 01 8A 21 9A 00 00 02 01 8B 21 9A 00 00 02 01 8C 21 9A 00 00 02 01 8D 21 9A 00 00 02 01 8E 21 9A 00 00 02 01 8F 21 9A 00 00 02 01 90 21 9A 00 00 02 01 91 21 9A 00 00 02 01 92 21 9A 00 00 02 01 93 21 9A 00 00 02 01 94 21 9A 00 00 02 01 95 21 9A 00 00 02 01 96 21 9A 00 00 02 01 97 21 9A 00 00 02 01 98 21 9A 00 00 02 01 99 21 9A 00 00 02 01 9A 21 9A 00 00 02 01 9B 21 9A 00 00 02 01 9C 21 9A 00 00 02 01 9D 21 9A 00 00 02 01 9E 21 9A 00 00 02 01 9F 21 9A 00 00 02 01 A0 21 9A 00 00 02 01 A1 21 9A 00 00 02 01 A2 21 9A 00 00 02 01 A3 21 9A 00 00 02 01 A4 21 9A 00 00 02 01 A5 21 9A 00 00 02 01 A6 21 9A 00 00 02 01 A7 21 9A 00 00 02 01 A8 21 9A 00 00 02 01 A9 21 9A 00 00 02 01 AA 21 9A 00 00 02 01 AB 21 9A 00 00 02 01 AC 21 9A 00 00 02 01 AD 21 9A 00 00 02 01 AE 21 9A 00 00 02 01 AF 21 9A 00 00 02 01 B0 21 9A 00 00 02 01 B1 21 9A 00 00 02 01 B2 21 9A 00 00 02 01 B3 21 9A 00 00 02 01 B4 21 9A 00 00 02 01 B5 21 9A 00 00 02 01 B6 21 9A 00 00 02 01 B7 21 9A 00 00 02 01 B8 21 9A 00 00 02 01 B9 21 9A 00 00 02 01 BA 21 9A 00 00 02 01 BB 21 9A 00 00 02 01 BC 21 9A 00 00 02 01 BD 21 9A 00 00 02 01 BE 21 9A 00 00 02 01 BF 21 9A 00 00 02 01 C0 21 9A 00 00 02 01 C1 21 9A 00 00 02 01 C2 21 9A 00 00 02 01 C3 21 9A 00 00 02 01 C4 21 9A 00 00 02 01 C5 21 9A 00 00 02 01 C6 21 9A 00 00 02 01 C7 21 9A 00 00 02 01 C8 21 9A 00 00 02 01 C9 21 9A 00 00 02 01 CA 21 9A 00 00 02 01 CB 21 9A 00 00 02 01 CC 21 9A 00 00 02 01 CD 21 9A 00 00 02 01 16 2D 31 01 08 04 07 FF 2B 2D 31 01 08 00 0B 4C 2D 31 01 08 00 0A 84 2D 31 01 08 04 08 02 FC 2D 31 01 08 04 08 02 E7 2E 31 01 08 04 07 FF E8 2E 31 01 08 00 08 E9 2E 31 01 08 04 07 FF FA 2E 31 01 08 04 07 FF FB 2E 31 01 08 04 07 FF FC 2E 31 01 08 04 07 FF FD 2E 31 01 08 04 07 FF FE 2E 31 01 08 04 07 FF FF 2E 31 01 08 04 07 FF 00 2F 31 01 08 04 07 FF 01 2F 31 01 08 00 0C 02 2F 31 01 08 00 0C 03 2F 31 01 08 00 0C A4 B3 32 01 08 00 0A C3 B3 32 01 08 00 09 CE B3 32 01 08 04 07 FF D1 B3 32 01 08 00 0B EB B3 32 01 08 04 08 02 02 B4 32 01 08 00 08 06 B4 32 01 08 04 07 FF 08 B4 32 01 08 04 07 FF 09 B4 32 01 08 04 07 FF 64 3A 34 01 08 00 08 70 3A 34 01 08 00 0B 87 3A 34 01 08 04 07 FF 88 3A 34 01 08 04 0A 02 89 3A 34 01 08 04 07 FF E9 C0 35 01 08 00 0B 50 C1 35 01 08 00 08 65 C1 35 01 08 04 07 02 85 C1 35 01 08 00 0A 86 C1 35 01 08 00 09 C8 C1 35 01 08 04 08 02 EE C1 35 01 08 04 07 FF 03 C2 35 01 08 04 07 FF 9C 47 37 01 08 04 08 02 1A 48 37 01 08 00 09 2B 48 37 01 08 00 0A 35 48 37 01 08 00 09 5E 48 37 01 08 00 0B 6D 48 37 01 08 00 08 6F 48 37 01 08 04 07 FF 70 48 37 01 08 04 07 FF 33 CE 38 01 08 04 08 02 3B CE 38 01 08 00 09 67 CE 38 01 08 00 09 9A CE 38 01 08 04 08 02 9F CE 38 01 08 00 0B D0 CE 38 01 08 00 0A DA CE 38 01 08 04 07 FF DB CE 38 01 08 04 07 FF DC CE 38 01 08 04 07 FF DD CE 38 01 08 04 07 FF E0 CE 38 01 08 04 07 FF C4 54 3A 01 08 04 08 02 C7 54 3A 01 08 00 0B CD 54 3A 01 08 04 07 FF 05 55 3A 01 08 00 0A 9F 55 3A 01 08 04 07 FF 67 DB 3B 01 08 00 0A 77 DB 3B 01 08 04 07 FF 8E DB 3B 01 08 00 08 92 DB 3B 01 08 04 0B 02 93 DB 3B 01 08 04 07 FF 8A 62 3D 01 08 00 0B C8 62 3D 01 08 00 0A CB 62 3D 01 08 04 07 FF D2 62 3D 01 08 00 09 EC 62 3D 01 08 00 08 EE 62 3D 01 08 04 07 FF F3 62 3D 01 08 04 08 02 F4 62 3D 01 08 04 08 02 F5 62 3D 01 08 00 0C CF E8 3E 01 08 04 08 02 DA E8 3E 01 08 00 0A DC E8 3E 01 08 00 0B DD E8 3E 01 08 00 08 E0 E8 3E 01 08 04 07 FF EF E8 3E 01 08 04 07 FF E2 F5 41 01 08 00 0B F8 F5 41 01 08 00 0A FA F5 41 01 08 04 07 FF 2C F6 41 01 08 04 07 FF 2D F6 41 01 08 04 08 02 80 C3 C9 01 08 00 0B 88 C3 C9 01 08 00 0A 20 4A CB 01 08 04 08 02 22 4A CB 01 08 00 0B 3A 4A CB 01 08 00 0A 3D 4A CB 01 08 04 07 FF C5 D0 CC 01 08 04 08 02 CD D0 CC 01 08 04 08 02 D4 D0 CC 01 08 04 07 FF DD D0 CC 01 08 04 07 FF DE D0 CC 01 08 00 09 E2 D0 CC 01 08 00 0B E3 D0 CC 01 08 00 0A A4 F0 FA 02 08 00 0B A7 F0 FA 02 08 00 0A 20 77 FC 02 08 00 0B 27 77 FC 02 08 00 09 29 77 FC 02 08 00 08 34 77 FC 02 08 00 0A 42 77 FC 02 08 04 08 02 45 77 FC 02 08 04 07 FF C1 FD FD 02 08 00 09 C4 FD FD 02 08 00 0A 05 FE FD 02 08 00 0B 06 FE FD 02 08 00 0A 08 FE FD 02 08 04 0C 02 09 FE FD 02 08 04 0C 02 0A FE FD 02 08 04 0C FF 0B FE FD 02 08 04 0B FF 11 FE FD 02 08 04 07 FF 12 FE FD 02 08 04 07 FF 13 FE FD 02 08 04 07 FF 63 84 FF 02 08 00 0B 6B 84 FF 02 08 00 0A B8 91 02 03 08 00 09 BB 91 02 03 08 00 0B BD 91 02 03 08 00 07 D0 91 02 03 08 00 08 D2 91 02 03 08 00 0A D6 91 02 03 08 00 0C 0E 87 93 03 08 00 09 14 87 93 03 08 00 08 1A 87 93 03 08 00 09 25 87 93 03 08 00 0B 26 87 93 03 08 00 08 A0 0D 95 03 08 00 0B A3 0D 95 03 08 04 08 02 A4 0D 95 03 08 00 0A D3 0D 95 03 08 04 07 FF E1 0D 95 03 08 04 07 FF 41 94 96 03 08 00 0B 5A 94 96 03 08 00 0A 73 1E 2C 04 08 06 07 00 FF 74 1E 2C 04 00 02 00 75 1E 2C 04 08 06 08 00 02 76 1E 2C 04 00 02 00 77 1E 2C 04 00 02 00 78 1E 2C 04 00 02 00 79 1E 2C 04 08 06 08 00 02 7A 1E 2C 04 00 02 00 7B 1E 2C 04 08 06 0A 00 02 7C 1E 2C 04 00 02 00 7D 1E 2C 04 00 02 00 7E 1E 2C 04 08 02 0B 00 7F 1E 2C 04 00 02 00 80 1E 2C 04 00 02 00 81 1E 2C 04 08 02 09 00 82 1E 2C 04 08 02 09 00 83 1E 2C 04 08 02 08 00 84 1E 2C 04 00 02 00 85 1E 2C 04 00 02 00 86 1E 2C 04 00 02 00 87 1E 2C 04 08 06 07 00 FF 88 1E 2C 04 08 06 07 00 FF 3A B4 C4 04 FF FF 6F D1 10 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3B B4 C4 04 FF FF 71 D1 10 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3C B4 C4 04 FF FF 68 4D 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3D B4 C4 04 FF FF 5A DE 13 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3E B4 C4 04 FF FF 86 86 3D 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3F B4 C4 04 FF FF 27 DC 1E 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 40 B4 C4 04 FF FF 28 DC 1E 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 41 B4 C4 04 FF FF 29 DC 1E 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 00 08 00 00 00 37 00 31 00 38 00 31 00 00 00 00 00 18 00 0E 00 0F 00 0C 06 38 02 14 00 08 80 B6 03 67 00 69 00 6E 00 49 00 70 00 00 00 00 00 00 00 06 00 04 00 13 00 0E 06 A8 01 14 00 D8 9F CD 03 33 00 2E 00 33 00 31 00 2E 00 32 00 33 00 35 00 2E 00 32 00 32 00 34 00 00 00 00 00 00 00 00 00 04 00 0A 00 15 01 0C 06 0E 00 00 00 62 00 65 00 67 00 69 00 6E 00 49 00 01 00 00 00 00 00 00 00 C5 FD FD 02 01 00 00 00 00 00 00 00 05 FE FD 02 01 00 00 00 00 00 00 00 13 FE FD 02 01 00 00 00 00 00 00 00 22 4A CB 01 01 00 00 00 00 00 00 00 C2 FD FD 02 01 00 00 00 01 00 00 00 C5 FD FD 02 01 00 00 00 01 00 00 00 05 FE FD 02 01 00 00 00 01 00 00 00 13 FE FD 02 01 00 00 00 01 00 00 00 22 4A CB 01 01 00 00 00 01 00 00 00 C2 FD FD 02 02 00 00 00 00 00 00 00 C5 FD FD 02 02 00 00 00 00 00 00 00 05 FE FD 02 02 00 00 00 00 00 00 00 13 FE FD 02 02 00 00 00 00 00 00 00 22 4A CB 01 02 00 00 00 00 00 00 00 C2 FD FD 02 02 00 00 00 01 00 00 00 C5 FD FD 02 02 00 00 00 01 00 00 00 05 FE FD 02 02 00 00 00 01 00 00 00 13 FE FD 02 02 00 00 00 01 00 00 00 22 4A CB 01 02 00 00 00 01 00 00 00 C2 FD FD 02 03 00 00 00 00 00 00 00 C5 FD FD 02 03 00 00 00 00 00 00 00 05 FE FD 02 03 00 00 00 00 00 00 00 13 FE FD 02 03 00 00 00 00 00 00 00 22 4A CB 01 03 00 00 00 00 00 00 00 C2 FD FD 02 03 00 00 00 01 00 00 00 C5 FD FD 02 03 00 00 00 01 00 00 00 05 FE FD 02 03 00 00 00 01 00 00 00 13 FE FD 02 03 00 00 00 01 00 00 00 22 4A CB 01 03 00 00 00 01 00 00 00 C2 FD FD 02 04 00 00 00 00 00 00 00 C5 FD FD 02 04 00 00 00 00 00 00 00 05 FE FD 02 04 00 00 00 00 00 00 00 13 FE FD 02 04 00 00 00 00 00 00 00 22 4A CB 01 04 00 00 00 00 00 00 00 C2 FD FD 02 04 00 00 00 01 00 00 00 C5 FD FD 02 04 00 00 00 01 00 00 00 05 FE FD 02 04 00 00 00 01 00 00 00 13 FE FD 02 04 00 00 00 01 00 00 00 22 4A CB 01 04 00 00 00 01 00 00 00 C2 FD FD 02 05 00 00 00 00 00 00 00 C5 FD FD 02 05 00 00 00 00 00 00 00 05 FE FD 02 05 00 00 00 00 00 00 00 13 FE FD 02 05 00 00 00 00 00 00 00 22 4A CB 01 05 00 00 00 00 00 00 00 C2 FD FD 02 05 00 00 00 01 00 00 00 C5 FD FD 02 05 00 00 00 01 00 00 00 05 FE FD 02 05 00 00 00 01 00 00 00 13 FE FD 02 05 00 00 00 01 00 00 00 22 4A CB 01 05 00 00 00 01 00 00 00 C2 FD FD 02 06 00 00 00 00 00 00 00 C5 FD FD 02 06 00 00 00 00 00 00 00 05 FE FD 02 06 00 00 00 00 00 00 00 13 FE FD 02 06 00 00 00 00 00 00 00 22 4A CB 01 06 00 00 00 00 00 00 00 C2 FD FD 02 06 00 00 00 01 00 00 00 C5 FD FD 02 06 00 00 00 01 00 00 00 05 FE FD 02 06 00 00 00 01 00 00 00 13 FE FD 02 06 00 00 00 01 00 00 00 22 4A CB 01 06 00 00 00 01 00 00 00 C2 FD FD 02 07 00 00 00 00 00 00 00 C5 FD FD 02 07 00 00 00 00 00 00 00 05 FE FD 02 07 00 00 00 00 00 00 00 13 FE FD 02 07 00 00 00 00 00 00 00 22 4A CB 01 07 00 00 00 00 00 00 00 C2 FD FD 02 07 00 00 00 01 00 00 00 C5 FD FD 02 07 00 00 00 01 00 00 00 05 FE FD 02 07 00 00 00 01 00 00 00 13 FE FD 02 07 00 00 00 01 00 00 00 22 4A CB 01 07 00 00 00 01 00 00 00 C2 FD FD 02 08 00 00 00 00 00 00 00 C5 FD FD 02 08 00 00 00 00 00 00 00 05 FE FD 02 08 00 00 00 00 00 00 00 13 FE FD 02 08 00 00 00 00 00 00 00 22 4A CB 01 08 00 00 00 00 00 00 00 C2 FD FD 02 08 00 00 00 01 00 00 00 C5 FD FD 02 08 00 00 00 01 00 00 00 05 FE FD 02 08 00 00 00 01 00 00 00 13 FE FD 02 08 00 00 00 01 00 00 00 22 4A CB 01 08 00 00 00 01 00 00 00 C2 FD FD 02 00 00 A3 00 26 71 0F 00 F3 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 23 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 54 71 0F 00 F4 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 58 71 0F 00 F5 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 32 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 B5 E6 0F 00 F8 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 FD 4A 0F 00 FC 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 14 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 29 71 0F 00 FD 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 23 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E5 DE 0F 00 FF 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0B D1 10 00 00 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 23 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1C F9 19 00 01 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 49 4B 4C 00 02 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 63 72 4C 00 03 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 76 72 4C 00 04 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 32 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 77 72 4C 00 05 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 FC 4A 0F 00 06 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BF 68 32 01 BF 68 32 01 0B 00 00 00 0C 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F6 4B 0F 00 07 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BF 68 32 01 BF 68 32 01 0B 00 00 00 0C 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 20 4E 00 3A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 00 00 00 00 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 79 72 4C 00 3B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 42 BC 4E 00 3C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0A 31 10 00 3D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 54 72 4C 00 3E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 69 0F 00 3F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 78 4B 0F 00 40 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A8 F8 19 00 41 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 21 A6 1B 00 42 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B4 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 84 5C 10 00 43 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 98 34 10 00 44 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 98 0F 00 45 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 75 83 10 00 46 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 18 0A 10 00 47 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0B 31 10 00 48 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 61 72 4C 00 49 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4B BC 4E 00 4A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 39 70 4D 00 4B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 10 47 4E 00 4C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C8 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B3 E6 0F 00 4D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 73 34 10 00 4E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5C 98 0F 00 4F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 DF 82 10 00 50 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 57 4B 4C 00 51 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B8 34 10 00 52 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 48 94 0F 00 53 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A9 7E 10 00 54 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 96 0D 10 00 55 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3C 95 4E 00 56 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E9 F8 19 00 57 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1C D1 10 00 58 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3F 71 0F 00 59 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F5 4B 0F 00 5A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B9 82 10 00 5B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B7 D0 10 00 5C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 98 0F 00 5D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 51 4B 4C 00 5E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0F 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C0 DE 0F 00 5F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 E3 4E 00 60 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B4 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D0 27 4E 00 61 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E8 94 50 00 62 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 26 71 0F 00 63 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B1 3E 52 00 64 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D6 D0 10 00 65 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 6B 71 0F 00 66 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 71 4B 0F 00 67 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8B F8 10 00 68 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A9 F8 19 00 69 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 89 83 4F 00 6A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 BE 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C5 F7 10 00 6B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5F 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 02 20 4E 00 6C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F0 F8 4D 00 6D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 43 BC 4E 00 6E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 10 31 10 00 6F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 71 72 4C 00 70 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 71 0F 00 71 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A9 57 10 00 72 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 08 20 4E 00 73 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 4B 4C 00 74 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 6A 5C 10 00 75 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 AB 34 10 00 76 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E9 94 50 00 77 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 47 BC 4E 00 78 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 CA 2C 10 00 79 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 60 72 4C 00 7A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4A BC 4E 00 7B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 38 70 4D 00 7C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 7A C0 4C 00 7D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B0 CD 4F 00 7E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 AA 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D1 E6 0F 00 7F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 CB 34 10 00 80 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 40 98 0F 00 81 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 32 83 10 00 82 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 90 0D 10 00 83 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C3 2C 10 00 84 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 42 98 0F 00 85 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 FD 09 10 00 86 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B5 DE 0F 00 87 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 24 A6 1B 00 88 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B4 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 62 E6 0F 00 89 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 EF D0 10 00 8A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5B 98 0F 00 8B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 CA 4A 0F 00 8C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 DC D0 10 00 8D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4E 98 0F 00 8E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3C 46 0F 00 8F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4A 4B 4C 00 90 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 9D E6 0F 00 91 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4B 4B 4C 00 92 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D3 F8 19 00 93 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F2 D0 10 00 94 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3C 71 0F 00 95 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 67 4B 0F 00 96 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 EE D0 10 00 97 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 58 71 0F 00 98 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 45 4C 0F 00 99 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8E F8 10 00 9A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 95 F8 19 00 9B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3A 95 4E 00 9C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 20 4E 00 9D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 46 BC 4E 00 9E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 0A 10 00 9F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0D 31 10 00 A0 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 72 4C 00 A1 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 71 0F 00 A2 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3D 70 4D 00 A3 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 77 5C 10 00 A4 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F9 23 4E 00 A5 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 47 4E 00 A6 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 BE 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 5C 10 00 A7 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F 34 10 00 A8 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 34 98 0F 00 A9 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 31 10 00 AA 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 57 98 0F 00 AB 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 45 BC 4E 00 AC 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 09 10 00 AD 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AD DE 0F 00 AE 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 70 4B 0F 00 AF 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 64 4D 00 B0 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F0 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AF E6 0F 00 B1 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 98 34 10 00 B2 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 31 98 0F 00 B3 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 77 83 10 00 B4 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B D1 10 00 B5 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 58 98 0F 00 B6 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 DE 82 10 00 B7 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE 09 10 00 B8 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8A E6 0F 00 B9 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 A6 1B 00 BA 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 BE 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 24 F9 19 00 BB 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D7 D0 10 00 BC 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 57 71 0F 00 BD 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 EC 4A 0F 00 BE 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E3 D0 10 00 BF 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 64 98 0F 00 C0 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F3 4A 0F 00 C1 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A9 F8 10 00 C2 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8D E6 0F 00 C3 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 89 83 4F 00 C4 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8C 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 28 F9 19 00 C5 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 D1 10 00 C6 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 58 BF 0F 00 C7 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 B0 3E 52 00 C8 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E2 D0 10 00 C9 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 4F 71 0F 00 CA 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 77 4B 0F 00 CB 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2A F8 10 00 CC 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 F9 19 00 CD 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 00 00"));
//        return mplew.getPacket();
//    }
    public static void toCashItem(MaplePacketLittleEndianWriter mplew, int sn, int type1, int type2) {
        mplew.writeInt(sn);
        mplew.write(0);
        mplew.write(type1);
        mplew.writeShort(0);
        mplew.write(type2);
    }

    public static void toCashItem(MaplePacketLittleEndianWriter mplew, int sn, int type0, int type1, int type2) {
        mplew.writeInt(sn);
        mplew.write(type0);
        mplew.write(type1);
        mplew.writeShort(0);
        mplew.write(type2);
    }

    public static MaplePacket showNXMapleTokens(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
        mplew.writeInt(chr.getCSPoints(1)); // Paypal/PayByCash NX
        mplew.writeInt(chr.getCSPoints(2)); // Maple Points
        mplew.writeInt(chr.getCSPoints(4)); // Game Card NX
        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCSItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeInt(15741499);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(4305024);
        mplew.writeInt(0);
        mplew.writeInt(itemid);
        mplew.write(HexTool.getByteArrayFromHexString("CD D0 CC 01 01 00 00 0A F1 4B 40 00 80 00 00 00 01 00 00 10 CB E9 4A BD 53 C9 01"));
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCSQuestItem(byte position, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeInt(365);
        mplew.write(0);
        mplew.writeShort(1);
        mplew.write(position);
        mplew.write(0);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket showCouponRedeemedItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeShort(0x3A);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(0x1A);
        mplew.writeInt(itemid);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket enableCSorMTS() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString("12 00 00 00 00 00 00"));
        return mplew.getPacket();
    }

    public static MaplePacket enableCSUse1() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeShort(0x2F);
        mplew.write(0);
        mplew.writeShort(4);
        mplew.writeShort(3);
        return mplew.getPacket();
    }

    public static MaplePacket enableCSUse2() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x31);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static MaplePacket enableCSUse3() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x33);
        mplew.write(new byte[40]);

        return mplew.getPacket();
    }

    public static MaplePacket sendWishList(int characterid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x33);
        Connection con = DatabaseConnection.getConnection();
        int i = 10;

        try {
            PreparedStatement ps = con.prepareStatement("SELECT sn FROM wishlist WHERE charid = ? LIMIT 10");
            ps.setInt(1, characterid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mplew.writeInt(rs.getInt("sn"));
                i--;
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            log.info("Error getting wishlist data:", se);
        }
        while (i > 0) {
            mplew.writeInt(0);
            i--;
        }

        return mplew.getPacket();
    }

    public static MaplePacket updateWishList(int characterid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x39);

        Connection con = DatabaseConnection.getConnection();
        int i = 10;

        try {
            PreparedStatement ps = con.prepareStatement("SELECT sn FROM wishlist WHERE charid = ? LIMIT 10");
            ps.setInt(1, characterid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mplew.writeInt(rs.getInt("sn"));
                i--;
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            log.info("Error getting wishlist data:", se);
        }

        while (i > 0) {
            mplew.writeInt(0);
            i--;
        }

        return mplew.getPacket();
    }

    public static MaplePacket wrongCouponCode() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x40);
        mplew.write(0x87);
        return mplew.getPacket();
    }

    public static MaplePacket getFindReplyWithCS(String target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);
        return mplew.getPacket();
    }

    public static MaplePacket updatePet(MaplePet pet, boolean alive) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(2);
        mplew.write(3);
        mplew.write(5);
        mplew.write(pet.getPosition());
        mplew.writeShort(0);
        mplew.write(5);
        mplew.write(pet.getPosition());
        mplew.write(0);
        mplew.write(3);
        mplew.writeInt(pet.getItemId());
        mplew.write(1);
        mplew.writeInt(pet.getUniqueId());
        mplew.writeInt(0);
        mplew.write(HexTool.getByteArrayFromHexString("00 40 6f e5 0f e7 17 02"));
        String petname = pet.getName();
        if (petname.length() > 13) {
            petname = petname.substring(0, 13);
        }
        mplew.writeAsciiString(petname);
        for (int i = petname.length(); i < 13; i++) {
            mplew.write(0);
        }
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        if (alive) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
            mplew.writeInt(0);
        } else {
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            mplew.write(HexTool.getByteArrayFromHexString("bb 46 e6 17 02 00 00 00 00"));
        }

        return mplew.getPacket();
    }

    public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove) {
        return showPet(chr, pet, remove, false);
    }

    public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getPetIndex(pet));
        if (remove) {
            mplew.write(0);
            mplew.write(hunger ? 1 : 0);
        } else {
            mplew.write(1);
            mplew.write(0);
            mplew.writeInt(pet.getItemId());
            mplew.writeMapleAsciiString(pet.getName());
            mplew.writeInt(pet.getUniqueId());
            mplew.writeInt(0);
            mplew.writeShort(pet.getPos().x);
            mplew.writeShort(pet.getPos().y);
            mplew.write(pet.getStance());
            mplew.writeInt(pet.getFh());
        }
        return mplew.getPacket();
    }

    public static MaplePacket movePet(int cid, int pid, int slot, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_PET.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeInt(pid);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket petChat(int cid, int un, String text, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_CHAT.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeShort(un);
        mplew.writeMapleAsciiString(text);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket commandResponse(int cid, byte command, int slot, boolean success, boolean food) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        if (!food) {
            mplew.write(0);
        }
        mplew.write(command);
        if (success) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket showOwnPetLevelUp(int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index);
        return mplew.getPacket();
    }

    public static MaplePacket showPetLevelUp(MapleCharacter chr, int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index);

        return mplew.getPacket();
    }

    public static MaplePacket changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket petStatUpdate(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        int mask = 0;
        mask |= MapleStat.PET.getValue();
        mplew.write(0);
        mplew.writeInt(mask);
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.writeInt(pets[i].getUniqueId());
                mplew.writeInt(0);
            } else {
                mplew.writeLong(0);
            }
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket weirdStatUpdate() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0);
        mplew.write(8);
        mplew.write(0);
        mplew.write(0x18);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.write(0);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket showEquipEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        return mplew.getPacket();
    }

    public static MaplePacket summonSkill(int cid, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        return mplew.getPacket();
    }

    public static MaplePacket skillCooldown(int sid, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(time);
        return mplew.getPacket();
    }

    public static MaplePacket skillBookSuccess(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static MaplePacket getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerNPC(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mplew.write(rs.getByte("dir"));
                mplew.writeShort(rs.getInt("x"));
                mplew.writeShort(rs.getInt("y"));
                mplew.writeMapleAsciiString(rs.getString("name"));
                mplew.write(0);
                mplew.write(rs.getByte("skin"));
                mplew.writeInt(rs.getInt("face"));
                mplew.write(0);
                mplew.writeInt(rs.getInt("hair"));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE npcid = ? AND type = 0");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mplew.write(rs.getByte("equippos"));
                mplew.writeInt(rs.getInt("equipid"));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
        }
        mplew.writeShort(-1);
        int count = 0;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE npcid = ? AND type = 1");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mplew.writeInt(rs.getInt("equipid"));
                count += 1;
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
        }
        while (count < 4) {
            mplew.writeInt(0);
            count += 1;
        }
        return mplew.getPacket();
    }

    public static MaplePacket showNotes(ResultSet notes, int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(2);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from"));
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(0);
            notes.next();
        }

        return mplew.getPacket();
    }

    public static void sendUnkwnNote(String to, String msg, String from) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setString(1, to);
        ps.setString(2, from);
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public static MaplePacket updateAriantPQRanking(String name, int score, boolean empty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ARIANT_PQ_START.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        return mplew.getPacket();
    }

    public static MaplePacket catchMonster(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);

        return mplew.getPacket();
    }

    public static MaplePacket showAriantScoreBoard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

        return mplew.getPacket();
    }

    public static MaplePacket showAllCharacter(int chars, int unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(1);
        mplew.writeInt(chars);
        mplew.writeInt(unk);
        return mplew.getPacket();
    }

    public static MaplePacket showAllCharacterInfo(int worldid, List<MapleCharacter> chars) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(0);
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateMount(int charid, MapleMount mount, boolean levelup) {
        return updateMount(charid, mount.getLevel(), mount.getExp(), mount.getTiredness(), levelup);
    }

    public static MaplePacket updateMount(int charid, int newlevel, int newexp, int tiredness, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(charid);
        mplew.writeInt(newlevel);
        mplew.writeInt(newexp);
        mplew.writeInt(tiredness);
        mplew.write(levelup ? (byte) 1 : (byte) 0);
        return mplew.getPacket();
    }

    public static MaplePacket useChalkboard(MapleCharacter chr, boolean close) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());
        mplew.writeInt(chr.getId());
        if (close) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }

        return mplew.getPacket();
    }

    public static MaplePacket showZakumShrineTimeLeft(int timeleft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ZAKUM_SHRINE.getValue());
        mplew.write(0);
        mplew.writeInt(timeleft);

        return mplew.getPacket();
    }

    public static MaplePacket boatPacket(boolean type) {

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(type ? 1 : 2);

        return mplew.getPacket();
    }

    public static MaplePacket sendDueyAction(byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(type);
        if (type == MapleDueyActions.S_OPEN_DUEY.getCode()) {
            mplew.write(0);
            mplew.write(1);
            mplew.writeInt(1209219);
            mplew.writeNullTerminatedAsciiString("Purpley");
            mplew.write(HexTool.getByteArrayFromHexString("8F 81 7C 02 02 01 FF FF FF 00 16 E5 10 D1 57 C9 01 00 00 00 00 00 1E 02 04 00 00 44 FC 74 0A 8E B5 82 7C 00 00 14 00 F8 EF B8 1E F8 EF B8 1E 00 00 14 00 00 00 B9 1E 00 00 03 18 00 00 00 09 00 00 14 00 18 05 CB 1E 02 04 00 00 74 FC 74 0A 8E B5 82 7C 00 00 14 00 18 05 CB 1E 18 05 CB 1E 00 00 14 00 00 00 03 18 00 00 03 18 00 00 F8 09 BC FC 74 0A 81 BA 82 7C 02 04 00 00 18 05 CB 01 00 00 00 00 00 00 14 00 18 05 CB 1E 00 00 00 00 28 05 00 00 00 20 CB 1E 59 9F 82 7C 00 00 03 18 00 00 00 00 00 00 00 00 00 00 00 00 C0 FC 74 0A 01 00 00 00 B5 9F 82 7C 88 A8 1F 10 A0 FD 74 0A 3D 9F 82 7C F3 E6 13 00 00 00 00 00 00 00 00 00 84 FB 74 0A 00 00 00 00 00 00 00 00 CC FC 74 01 02 65 8C 1E 00 00 00 80 05 BB 46 E6 17 02 01 00 00 00 00 00 00"));
        }

        return mplew.getPacket();
    }

    public static MaplePacket startMonsterCarnival(int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_START.getValue());
        mplew.write(team);
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
        return mplew.getPacket();
    }

    public static MaplePacket playerDiedMessage(String name, int lostCP, int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_DIED.getValue());
        mplew.write(team); //team
        mplew.writeMapleAsciiString(name);
        mplew.write(lostCP);
        return mplew.getPacket();
    }

    public static MaplePacket CPUpdate(boolean party, int curCP, int totalCP, int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (!party) {
            mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        } else {
            mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
            mplew.write(team);
        }
        mplew.writeShort(curCP);
        mplew.writeShort(totalCP);
        return mplew.getPacket();
    }

    public static MaplePacket playerSummoned(String name, int tab, int number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab);
        mplew.write(number);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 01 02"));
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeInt(minigame.getOwner().getOmokPoints("wins"));
        mplew.writeInt(minigame.getOwner().getOmokPoints("ties"));
        mplew.writeInt(minigame.getOwner().getOmokPoints("losses"));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(1);
            mplew.writeInt(visitor.getOmokPoints("wins"));
            mplew.writeInt(visitor.getOmokPoints("ties"));
            mplew.writeInt(visitor.getOmokPoints("losses"));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameReady(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x34);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameUnReady(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x35);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("37 0" + loser));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipOwner(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("39 01"));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRequestTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("2C"));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameDenyTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("2D"));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 00"));
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipVisitor(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("39 00"));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("3A"));
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getOmokPoints("wins"));
        mplew.writeInt(c.getOmokPoints("ties"));
        mplew.writeInt(c.getOmokPoints("losses"));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRemoveVisitor() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 01"));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameOwnerWin(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 00"));
        mplew.write(0); // owner
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getOmokPoints("wins") + 1); // wins
        mplew.writeInt(game.getOwner().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getOwner().getOmokPoints("losses")); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getOmokPoints("wins")); // wins
        mplew.writeInt(game.getVisitor().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getVisitor().getOmokPoints("losses") + 1); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setOmokPoints(game.getVisitor(), 1);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameVisitorWin(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 00"));
        mplew.write(1);
        mplew.writeInt(1); // start of owner; unknown
        mplew.writeInt(game.getOwner().getOmokPoints("wins")); // wins
        mplew.writeInt(game.getOwner().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getOwner().getOmokPoints("losses") + 1); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getOmokPoints("wins") + 1); // wins
        mplew.writeInt(game.getVisitor().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getVisitor().getOmokPoints("losses")); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setOmokPoints(game.getVisitor(), 2);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 01"));
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getOmokPoints("wins")); // wins
        mplew.writeInt(game.getOwner().getOmokPoints("ties") + 1); // ties
        mplew.writeInt(game.getOwner().getOmokPoints("losses")); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getOmokPoints("wins")); // wins
        mplew.writeInt(game.getVisitor().getOmokPoints("ties") + 1); // ties
        mplew.writeInt(game.getVisitor().getOmokPoints("losses")); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setOmokPoints(game.getVisitor(), 3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameClose(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(1);
        mplew.write(3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameVisitorForfeit(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 02"));
        mplew.write(0); // owner
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getOmokPoints("wins") + 1); // wins
        mplew.writeInt(game.getOwner().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getOwner().getOmokPoints("losses")); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getOmokPoints("wins")); // wins
        mplew.writeInt(game.getVisitor().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getVisitor().getOmokPoints("losses") + 1); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setOmokPoints(game.getVisitor(), 1);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameOwnerForfeit(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 02"));
        mplew.write(1);
        mplew.writeInt(1); // start of owner; unknown
        mplew.writeInt(game.getOwner().getOmokPoints("wins")); // wins
        mplew.writeInt(game.getOwner().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getOwner().getOmokPoints("losses") + 1); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getOmokPoints("wins") + 1); // wins
        mplew.writeInt(game.getVisitor().getOmokPoints("ties")); // ties
        mplew.writeInt(game.getVisitor().getOmokPoints("losses")); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setOmokPoints(game.getVisitor(), 2);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 02 02"));
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeInt(minigame.getOwner().getMatchCardPoints("wins"));
        mplew.writeInt(minigame.getOwner().getMatchCardPoints("ties"));
        mplew.writeInt(minigame.getOwner().getMatchCardPoints("losses"));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(2);
            mplew.writeInt(visitor.getMatchCardPoints("wins"));
            mplew.writeInt(visitor.getMatchCardPoints("ties"));
            mplew.writeInt(visitor.getMatchCardPoints("losses"));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("37 0" + loser));
        mplew.write(HexTool.getByteArrayFromHexString("0C"));
        mplew.writeInt(game.getCardId(1));
        mplew.writeInt(game.getCardId(2));
        mplew.writeInt(game.getCardId(3));
        mplew.writeInt(game.getCardId(4));
        mplew.writeInt(game.getCardId(5));
        mplew.writeInt(game.getCardId(6));
        mplew.writeInt(game.getCardId(7));
        mplew.writeInt(game.getCardId(8));
        mplew.writeInt(game.getCardId(9));
        mplew.writeInt(game.getCardId(10));
        mplew.writeInt(game.getCardId(11));
        mplew.writeInt(game.getCardId(12));
        if (game.getMatchesToWin() > 6) {
            mplew.writeInt(game.getCardId(13));
            mplew.writeInt(game.getCardId(14));
            mplew.writeInt(game.getCardId(15));
            mplew.writeInt(game.getCardId(16));
            mplew.writeInt(game.getCardId(17));
            mplew.writeInt(game.getCardId(18));
            mplew.writeInt(game.getCardId(19));
            mplew.writeInt(game.getCardId(20));
        }
        if (game.getMatchesToWin() > 10) {
            mplew.writeInt(game.getCardId(21));
            mplew.writeInt(game.getCardId(22));
            mplew.writeInt(game.getCardId(23));
            mplew.writeInt(game.getCardId(24));
            mplew.writeInt(game.getCardId(25));
            mplew.writeInt(game.getCardId(26));
            mplew.writeInt(game.getCardId(27));
            mplew.writeInt(game.getCardId(28));
            mplew.writeInt(game.getCardId(29));
            mplew.writeInt(game.getCardId(30));
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMatchCardPoints("wins"));
        mplew.writeInt(c.getMatchCardPoints("ties"));
        mplew.writeInt(c.getMatchCardPoints("losses"));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("3E 0" + turn));
        if (turn == 1) {
            mplew.write(slot);
        }
        if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        log.info(mplew.toString());
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardOwnerWin(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 00"));
        mplew.write(0); // owner
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getMatchCardPoints("wins") + 1); // wins
        mplew.writeInt(game.getOwner().getMatchCardPoints("ties")); // ties
        mplew.writeInt(game.getOwner().getMatchCardPoints("losses")); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getMatchCardPoints("wins")); // wins
        mplew.writeInt(game.getVisitor().getMatchCardPoints("ties")); // ties
        mplew.writeInt(game.getVisitor().getMatchCardPoints("losses") + 1); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setMatchCardPoints(game.getVisitor(), 1);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardVisitorWin(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 00"));
        mplew.write(1);
        mplew.writeInt(1); // start of owner; unknown
        mplew.writeInt(game.getOwner().getMatchCardPoints("wins")); // wins
        mplew.writeInt(game.getOwner().getMatchCardPoints("ties")); // ties
        mplew.writeInt(game.getOwner().getMatchCardPoints("losses") + 1); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getMatchCardPoints("wins") + 1); // wins
        mplew.writeInt(game.getVisitor().getMatchCardPoints("ties")); // ties
        mplew.writeInt(game.getVisitor().getMatchCardPoints("losses")); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setMatchCardPoints(game.getVisitor(), 2);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 01"));
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getMatchCardPoints("wins")); // wins
        mplew.writeInt(game.getOwner().getMatchCardPoints("ties") + 1); // ties
        mplew.writeInt(game.getOwner().getMatchCardPoints("losses")); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getMatchCardPoints("wins")); // wins
        mplew.writeInt(game.getVisitor().getMatchCardPoints("ties") + 1); // ties
        mplew.writeInt(game.getVisitor().getMatchCardPoints("losses")); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setMatchCardPoints(game.getVisitor(), 3);
        return mplew.getPacket();
    }

    public static MaplePacket getHiredMerchant(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 05 04 00 00 71 C0 4C 00"));
        mplew.writeMapleAsciiString("Hired Merchant");
        mplew.write(0xFF);
        mplew.write(0);
        mplew.write(0);
        mplew.writeMapleAsciiString("MyDexIsBroke");
        mplew.write(HexTool.getByteArrayFromHexString("1F 7E 00 00 00 00 00 00 00 00 03 00 31 32 33 10 00 00 00 00 01 01 00 01 00 7B 00 00 00 02 52 8C 1E 00 00 00 80 05 BB 46 E6 17 02 01 00 00 00 00 00"));
        return mplew.getPacket();
    }

    public static MaplePacket addOmokBox(MapleCharacter c, int ammount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 1, 0, ammount, type);
        return mplew.getPacket();
    }

    public static MaplePacket removeOmokBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket addMatchCardBox(MapleCharacter c, int ammount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 2, 0, ammount, type);
        return mplew.getPacket();
    }

    public static MaplePacket removeMatchcardBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket warpCS(MapleClient c, boolean mts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        MapleCharacter chr = c.getPlayer();
        if (!mts) {
            mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());
        } else {
            mplew.writeShort(SendPacketOpcode.MTS_OPEN.getValue());
        }
        mplew.writeLong(-1);
        addCharStats(mplew, chr);
        mplew.write(20); // ???
        mplew.writeInt(chr.getMeso()); // mesos
        mplew.write(100); // equip slots
        mplew.write(100); // use slots
        mplew.write(100); // set-up slots
        mplew.write(100); // etc slots
        mplew.write(48); // storage slots
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        for (Item item : equipped) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0); // start of equip inventory
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of use inventory
        iv = chr.getInventory(MapleInventoryType.USE);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of set-up inventory
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of etc inventory
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of cash inventory
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            if (skill.getKey().isFourthJob()) {
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }
        mplew.writeShort(0);
        mplew.writeShort(1);
        mplew.writeInt(662990);
        mplew.write(HexTool.getByteArrayFromHexString("5A 42 43 44 45 46 47 48 49 4A 00 00"));
        mplew.writeLong(0);
        for (int i = 0; i < 15; i++) {
            mplew.write(CHAR_INFO_MAGIC);
        }
        mplew.writeInt(0);
        if (!mts) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(chr.getClient().getAccountName());
        if (mts) {
            mplew.writeInt(5000);
            mplew.write(HexTool.getByteArrayFromHexString("0A 00 00 00 64 00 00 00 18 00 00 00 A8 00 00 00 B0 ED 4E 3C FD 68 C9 01"));
        } else {
            mplew.write(HexTool.getByteArrayFromHexString("96 00 00 00 38 21 9A 00 39 21 9A 00 3A 21 9A 00 3B 21 9A 00 3C 21 9A 00 3D 21 9A 00 3E 21 9A 00 3F 21 9A 00 40 21 9A 00 41 21 9A 00 42 21 9A 00 43 21 9A 00 44 21 9A 00 45 21 9A 00 46 21 9A 00 47 21 9A 00 48 21 9A 00 49 21 9A 00 4A 21 9A 00 4B 21 9A 00 4C 21 9A 00 4D 21 9A 00 4E 21 9A 00 4F 21 9A 00 50 21 9A 00 51 21 9A 00 52 21 9A 00 53 21 9A 00 54 21 9A 00 55 21 9A 00 56 21 9A 00 57 21 9A 00 58 21 9A 00 59 21 9A 00 5A 21 9A 00 5B 21 9A 00 5C 21 9A 00 5D 21 9A 00 5E 21 9A 00 5F 21 9A 00 60 21 9A 00 61 21 9A 00 62 21 9A 00 63 21 9A 00 64 21 9A 00 65 21 9A 00 66 21 9A 00 67 21 9A 00 68 21 9A 00 69 21 9A 00 6A 21 9A 00 6B 21 9A 00 6C 21 9A 00 6D 21 9A 00 6E 21 9A 00 6F 21 9A 00 70 21 9A 00 71 21 9A 00 72 21 9A 00 73 21 9A 00 74 21 9A 00 75 21 9A 00 76 21 9A 00 77 21 9A 00 78 21 9A 00 79 21 9A 00 7A 21 9A 00 7B 21 9A 00 7C 21 9A 00 7D 21 9A 00 7E 21 9A 00 7F 21 9A 00 80 21 9A 00 81 21 9A 00 82 21 9A 00 83 21 9A 00 84 21 9A 00 85 21 9A 00 86 21 9A 00 87 21 9A 00 88 21 9A 00 89 21 9A 00 8A 21 9A 00 8B 21 9A 00 8C 21 9A 00 8D 21 9A 00 8E 21 9A 00 8F 21 9A 00 90 21 9A 00 91 21 9A 00 92 21 9A 00 93 21 9A 00 94 21 9A 00 95 21 9A 00 96 21 9A 00 97 21 9A 00 98 21 9A 00 99 21 9A 00 9A 21 9A 00 9B 21 9A 00 9C 21 9A 00 9D 21 9A 00 9E 21 9A 00 9F 21 9A 00 A0 21 9A 00 A1 21 9A 00 A2 21 9A 00 A3 21 9A 00 A4 21 9A 00 A5 21 9A 00 A6 21 9A 00 A7 21 9A 00 A8 21 9A 00 A9 21 9A 00 AA 21 9A 00 AB 21 9A 00 AC 21 9A 00 AD 21 9A 00 AE 21 9A 00 AF 21 9A 00 B0 21 9A 00 B1 21 9A 00 B2 21 9A 00 B3 21 9A 00 B4 21 9A 00 B5 21 9A 00 B6 21 9A 00 B7 21 9A 00 B8 21 9A 00 B9 21 9A 00 BA 21 9A 00 BB 21 9A 00 BC 21 9A 00 BD 21 9A 00 BE 21 9A 00 BF 21 9A 00 C0 21 9A 00 C1 21 9A 00 C2 21 9A 00 C3 21 9A 00 C4 21 9A 00 C5 21 9A 00 C6 21 9A 00 C7 21 9A 00 C8 21 9A 00 C9 21 9A 00 CA 21 9A 00 CB 21 9A 00 CC 21 9A 00 CD 21 9A 00 59 01 C8 9D 98 00 00 02 00 C9 9D 98 00 00 02 00 EB 9D 98 00 00 02 00 EC 9D 98 00 00 02 00 EF 9D 98 00 00 02 00 F0 9D 98 00 00 02 00 F1 9D 98 00 00 02 00 F2 9D 98 00 00 02 00 F5 9D 98 00 00 06 00 00 F6 9D 98 00 00 06 00 00 F7 9D 98 00 00 02 00 F8 9D 98 00 00 02 00 F9 9D 98 00 00 02 00 FA 9D 98 00 00 02 00 FB 9D 98 00 00 02 00 FC 9D 98 00 00 02 00 FD 9D 98 00 00 02 00 FE 9D 98 00 00 02 00 FF 9D 98 00 00 02 00 00 9E 98 00 00 02 00 01 9E 98 00 00 02 00 02 9E 98 00 00 02 00 03 9E 98 00 00 02 00 04 9E 98 00 00 02 00 38 21 9A 00 00 02 01 39 21 9A 00 00 02 01 3A 21 9A 00 00 02 01 3B 21 9A 00 00 02 01 3C 21 9A 00 00 02 01 3D 21 9A 00 00 02 01 3E 21 9A 00 00 02 01 3F 21 9A 00 00 02 01 40 21 9A 00 00 02 01 41 21 9A 00 00 02 01 42 21 9A 00 00 02 01 43 21 9A 00 00 02 01 44 21 9A 00 00 02 01 45 21 9A 00 00 02 01 46 21 9A 00 00 02 01 47 21 9A 00 00 02 01 48 21 9A 00 00 02 01 49 21 9A 00 00 02 01 4A 21 9A 00 00 02 01 4B 21 9A 00 00 02 01 4C 21 9A 00 00 02 01 4D 21 9A 00 00 02 01 4E 21 9A 00 00 02 01 4F 21 9A 00 00 02 01 50 21 9A 00 00 02 01 51 21 9A 00 00 02 01 52 21 9A 00 00 02 01 53 21 9A 00 00 02 01 54 21 9A 00 00 02 01 55 21 9A 00 00 02 01 56 21 9A 00 00 02 01 57 21 9A 00 00 02 01 58 21 9A 00 00 02 01 59 21 9A 00 00 02 01 5A 21 9A 00 00 02 01 5B 21 9A 00 00 02 01 5C 21 9A 00 00 02 01 5D 21 9A 00 00 02 01 5E 21 9A 00 00 02 01 5F 21 9A 00 00 02 01 60 21 9A 00 00 02 01 61 21 9A 00 00 02 01 62 21 9A 00 00 02 01 63 21 9A 00 00 02 01 64 21 9A 00 00 02 01 65 21 9A 00 00 02 01 66 21 9A 00 00 02 01 67 21 9A 00 00 02 01 68 21 9A 00 00 02 01 69 21 9A 00 00 02 01 6A 21 9A 00 00 02 01 6B 21 9A 00 00 02 01 6C 21 9A 00 00 02 01 6D 21 9A 00 00 02 01 6E 21 9A 00 00 02 01 6F 21 9A 00 00 02 01 70 21 9A 00 00 02 01 71 21 9A 00 00 02 01 72 21 9A 00 00 02 01 73 21 9A 00 00 02 01 74 21 9A 00 00 02 01 75 21 9A 00 00 02 01 76 21 9A 00 00 02 01 77 21 9A 00 00 02 01 78 21 9A 00 00 02 01 79 21 9A 00 00 02 01 7A 21 9A 00 00 02 01 7B 21 9A 00 00 02 01 7C 21 9A 00 00 02 01 7D 21 9A 00 00 02 01 7E 21 9A 00 00 02 01 7F 21 9A 00 00 02 01 80 21 9A 00 00 02 01 81 21 9A 00 00 02 01 82 21 9A 00 00 02 01 83 21 9A 00 00 02 01 84 21 9A 00 00 02 01 85 21 9A 00 00 02 01 86 21 9A 00 00 02 01 87 21 9A 00 00 02 01 88 21 9A 00 00 02 01 89 21 9A 00 00 02 01 8A 21 9A 00 00 02 01 8B 21 9A 00 00 02 01 8C 21 9A 00 00 02 01 8D 21 9A 00 00 02 01 8E 21 9A 00 00 02 01 8F 21 9A 00 00 02 01 90 21 9A 00 00 02 01 91 21 9A 00 00 02 01 92 21 9A 00 00 02 01 93 21 9A 00 00 02 01 94 21 9A 00 00 02 01 95 21 9A 00 00 02 01 96 21 9A 00 00 02 01 97 21 9A 00 00 02 01 98 21 9A 00 00 02 01 99 21 9A 00 00 02 01 9A 21 9A 00 00 02 01 9B 21 9A 00 00 02 01 9C 21 9A 00 00 02 01 9D 21 9A 00 00 02 01 9E 21 9A 00 00 02 01 9F 21 9A 00 00 02 01 A0 21 9A 00 00 02 01 A1 21 9A 00 00 02 01 A2 21 9A 00 00 02 01 A3 21 9A 00 00 02 01 A4 21 9A 00 00 02 01 A5 21 9A 00 00 02 01 A6 21 9A 00 00 02 01 A7 21 9A 00 00 02 01 A8 21 9A 00 00 02 01 A9 21 9A 00 00 02 01 AA 21 9A 00 00 02 01 AB 21 9A 00 00 02 01 AC 21 9A 00 00 02 01 AD 21 9A 00 00 02 01 AE 21 9A 00 00 02 01 AF 21 9A 00 00 02 01 B0 21 9A 00 00 02 01 B1 21 9A 00 00 02 01 B2 21 9A 00 00 02 01 B3 21 9A 00 00 02 01 B4 21 9A 00 00 02 01 B5 21 9A 00 00 02 01 B6 21 9A 00 00 02 01 B7 21 9A 00 00 02 01 B8 21 9A 00 00 02 01 B9 21 9A 00 00 02 01 BA 21 9A 00 00 02 01 BB 21 9A 00 00 02 01 BC 21 9A 00 00 02 01 BD 21 9A 00 00 02 01 BE 21 9A 00 00 02 01 BF 21 9A 00 00 02 01 C0 21 9A 00 00 02 01 C1 21 9A 00 00 02 01 C2 21 9A 00 00 02 01 C3 21 9A 00 00 02 01 C4 21 9A 00 00 02 01 C5 21 9A 00 00 02 01 C6 21 9A 00 00 02 01 C7 21 9A 00 00 02 01 C8 21 9A 00 00 02 01 C9 21 9A 00 00 02 01 CA 21 9A 00 00 02 01 CB 21 9A 00 00 02 01 CC 21 9A 00 00 02 01 CD 21 9A 00 00 02 01 16 2D 31 01 08 04 07 FF 2B 2D 31 01 08 00 0B 4C 2D 31 01 08 00 0A 84 2D 31 01 08 04 08 02 FC 2D 31 01 08 04 08 02 E7 2E 31 01 08 04 07 FF E8 2E 31 01 08 00 08 E9 2E 31 01 08 04 07 FF FA 2E 31 01 08 04 07 FF FB 2E 31 01 08 04 07 FF FC 2E 31 01 08 04 07 FF FD 2E 31 01 08 04 07 FF FE 2E 31 01 08 04 07 FF FF 2E 31 01 08 04 07 FF 00 2F 31 01 08 04 07 FF 01 2F 31 01 08 00 0C 02 2F 31 01 08 00 0C 03 2F 31 01 08 00 0C A4 B3 32 01 08 00 0A C3 B3 32 01 08 00 09 CE B3 32 01 08 04 07 FF D1 B3 32 01 08 00 0B EB B3 32 01 08 04 08 02 02 B4 32 01 08 00 08 06 B4 32 01 08 04 07 FF 08 B4 32 01 08 04 07 FF 09 B4 32 01 08 04 07 FF 64 3A 34 01 08 00 08 70 3A 34 01 08 00 0B 87 3A 34 01 08 04 07 FF 88 3A 34 01 08 04 0A 02 89 3A 34 01 08 04 07 FF E9 C0 35 01 08 00 0B 50 C1 35 01 08 00 08 65 C1 35 01 08 04 07 02 85 C1 35 01 08 00 0A 86 C1 35 01 08 00 09 C8 C1 35 01 08 04 08 02 EE C1 35 01 08 04 07 FF 03 C2 35 01 08 04 07 FF 9C 47 37 01 08 04 08 02 1A 48 37 01 08 00 09 2B 48 37 01 08 00 0A 35 48 37 01 08 00 09 5E 48 37 01 08 00 0B 6D 48 37 01 08 00 08 6F 48 37 01 08 04 07 FF 70 48 37 01 08 04 07 FF 33 CE 38 01 08 04 08 02 3B CE 38 01 08 00 09 67 CE 38 01 08 00 09 9A CE 38 01 08 04 08 02 9F CE 38 01 08 00 0B D0 CE 38 01 08 00 0A DA CE 38 01 08 04 07 FF DB CE 38 01 08 04 07 FF DC CE 38 01 08 04 07 FF DD CE 38 01 08 04 07 FF E0 CE 38 01 08 04 07 FF C4 54 3A 01 08 04 08 02 C7 54 3A 01 08 00 0B CD 54 3A 01 08 04 07 FF 05 55 3A 01 08 00 0A 9F 55 3A 01 08 04 07 FF 67 DB 3B 01 08 00 0A 77 DB 3B 01 08 04 07 FF 8E DB 3B 01 08 00 08 92 DB 3B 01 08 04 0B 02 93 DB 3B 01 08 04 07 FF 8A 62 3D 01 08 00 0B C8 62 3D 01 08 00 0A CB 62 3D 01 08 04 07 FF D2 62 3D 01 08 00 09 EC 62 3D 01 08 00 08 EE 62 3D 01 08 04 07 FF F3 62 3D 01 08 04 08 02 F4 62 3D 01 08 04 08 02 F5 62 3D 01 08 00 0C CF E8 3E 01 08 04 08 02 DA E8 3E 01 08 00 0A DC E8 3E 01 08 00 0B DD E8 3E 01 08 00 08 E0 E8 3E 01 08 04 07 FF EF E8 3E 01 08 04 07 FF E2 F5 41 01 08 00 0B F8 F5 41 01 08 00 0A FA F5 41 01 08 04 07 FF 2C F6 41 01 08 04 07 FF 2D F6 41 01 08 04 08 02 80 C3 C9 01 08 00 0B 88 C3 C9 01 08 00 0A 20 4A CB 01 08 04 08 02 22 4A CB 01 08 00 0B 3A 4A CB 01 08 00 0A 3D 4A CB 01 08 04 07 FF C5 D0 CC 01 08 04 08 02 CD D0 CC 01 08 04 08 02 D4 D0 CC 01 08 04 07 FF DD D0 CC 01 08 04 07 FF DE D0 CC 01 08 00 09 E2 D0 CC 01 08 00 0B E3 D0 CC 01 08 00 0A A4 F0 FA 02 08 00 0B A7 F0 FA 02 08 00 0A 20 77 FC 02 08 00 0B 27 77 FC 02 08 00 09 29 77 FC 02 08 00 08 34 77 FC 02 08 00 0A 42 77 FC 02 08 04 08 02 45 77 FC 02 08 04 07 FF C1 FD FD 02 08 00 09 C4 FD FD 02 08 00 0A 05 FE FD 02 08 00 0B 06 FE FD 02 08 00 0A 08 FE FD 02 08 04 0C 02 09 FE FD 02 08 04 0C 02 0A FE FD 02 08 04 0C FF 0B FE FD 02 08 04 0B FF 11 FE FD 02 08 04 07 FF 12 FE FD 02 08 04 07 FF 13 FE FD 02 08 04 07 FF 63 84 FF 02 08 00 0B 6B 84 FF 02 08 00 0A B8 91 02 03 08 00 09 BB 91 02 03 08 00 0B BD 91 02 03 08 00 07 D0 91 02 03 08 00 08 D2 91 02 03 08 00 0A D6 91 02 03 08 00 0C 0E 87 93 03 08 00 09 14 87 93 03 08 00 08 1A 87 93 03 08 00 09 25 87 93 03 08 00 0B 26 87 93 03 08 00 08 A0 0D 95 03 08 00 0B A3 0D 95 03 08 04 08 02 A4 0D 95 03 08 00 0A D3 0D 95 03 08 04 07 FF E1 0D 95 03 08 04 07 FF 41 94 96 03 08 00 0B 5A 94 96 03 08 00 0A 73 1E 2C 04 08 06 07 00 FF 74 1E 2C 04 00 02 00 75 1E 2C 04 08 06 08 00 02 76 1E 2C 04 00 02 00 77 1E 2C 04 00 02 00 78 1E 2C 04 00 02 00 79 1E 2C 04 08 06 08 00 02 7A 1E 2C 04 00 02 00 7B 1E 2C 04 08 06 0A 00 02 7C 1E 2C 04 00 02 00 7D 1E 2C 04 00 02 00 7E 1E 2C 04 08 02 0B 00 7F 1E 2C 04 00 02 00 80 1E 2C 04 00 02 00 81 1E 2C 04 08 02 09 00 82 1E 2C 04 08 02 09 00 83 1E 2C 04 08 02 08 00 84 1E 2C 04 00 02 00 85 1E 2C 04 00 02 00 86 1E 2C 04 00 02 00 87 1E 2C 04 08 06 07 00 FF 88 1E 2C 04 08 06 07 00 FF 3A B4 C4 04 FF FF 6F D1 10 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3B B4 C4 04 FF FF 71 D1 10 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3C B4 C4 04 FF FF 68 4D 0F 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3D B4 C4 04 FF FF 5A DE 13 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3E B4 C4 04 FF FF 86 86 3D 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 3F B4 C4 04 FF FF 27 DC 1E 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 40 B4 C4 04 FF FF 28 DC 1E 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 41 B4 C4 04 FF FF 29 DC 1E 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 FF 00 00 00 00 00 00 00 00 00 08 00 00 00 37 00 31 00 38 00 31 00 00 00 00 00 18 00 0E 00 0F 00 0C 06 38 02 14 00 08 80 B6 03 67 00 69 00 6E 00 49 00 70 00 00 00 00 00 00 00 06 00 04 00 13 00 0E 06 A8 01 14 00 D8 9F CD 03 33 00 2E 00 33 00 31 00 2E 00 32 00 33 00 35 00 2E 00 32 00 32 00 34 00 00 00 00 00 00 00 00 00 04 00 0A 00 15 01 0C 06 0E 00 00 00 62 00 65 00 67 00 69 00 6E 00 49 00 01 00 00 00 00 00 00 00 C5 FD FD 02 01 00 00 00 00 00 00 00 05 FE FD 02 01 00 00 00 00 00 00 00 13 FE FD 02 01 00 00 00 00 00 00 00 22 4A CB 01 01 00 00 00 00 00 00 00 C2 FD FD 02 01 00 00 00 01 00 00 00 C5 FD FD 02 01 00 00 00 01 00 00 00 05 FE FD 02 01 00 00 00 01 00 00 00 13 FE FD 02 01 00 00 00 01 00 00 00 22 4A CB 01 01 00 00 00 01 00 00 00 C2 FD FD 02 02 00 00 00 00 00 00 00 C5 FD FD 02 02 00 00 00 00 00 00 00 05 FE FD 02 02 00 00 00 00 00 00 00 13 FE FD 02 02 00 00 00 00 00 00 00 22 4A CB 01 02 00 00 00 00 00 00 00 C2 FD FD 02 02 00 00 00 01 00 00 00 C5 FD FD 02 02 00 00 00 01 00 00 00 05 FE FD 02 02 00 00 00 01 00 00 00 13 FE FD 02 02 00 00 00 01 00 00 00 22 4A CB 01 02 00 00 00 01 00 00 00 C2 FD FD 02 03 00 00 00 00 00 00 00 C5 FD FD 02 03 00 00 00 00 00 00 00 05 FE FD 02 03 00 00 00 00 00 00 00 13 FE FD 02 03 00 00 00 00 00 00 00 22 4A CB 01 03 00 00 00 00 00 00 00 C2 FD FD 02 03 00 00 00 01 00 00 00 C5 FD FD 02 03 00 00 00 01 00 00 00 05 FE FD 02 03 00 00 00 01 00 00 00 13 FE FD 02 03 00 00 00 01 00 00 00 22 4A CB 01 03 00 00 00 01 00 00 00 C2 FD FD 02 04 00 00 00 00 00 00 00 C5 FD FD 02 04 00 00 00 00 00 00 00 05 FE FD 02 04 00 00 00 00 00 00 00 13 FE FD 02 04 00 00 00 00 00 00 00 22 4A CB 01 04 00 00 00 00 00 00 00 C2 FD FD 02 04 00 00 00 01 00 00 00 C5 FD FD 02 04 00 00 00 01 00 00 00 05 FE FD 02 04 00 00 00 01 00 00 00 13 FE FD 02 04 00 00 00 01 00 00 00 22 4A CB 01 04 00 00 00 01 00 00 00 C2 FD FD 02 05 00 00 00 00 00 00 00 C5 FD FD 02 05 00 00 00 00 00 00 00 05 FE FD 02 05 00 00 00 00 00 00 00 13 FE FD 02 05 00 00 00 00 00 00 00 22 4A CB 01 05 00 00 00 00 00 00 00 C2 FD FD 02 05 00 00 00 01 00 00 00 C5 FD FD 02 05 00 00 00 01 00 00 00 05 FE FD 02 05 00 00 00 01 00 00 00 13 FE FD 02 05 00 00 00 01 00 00 00 22 4A CB 01 05 00 00 00 01 00 00 00 C2 FD FD 02 06 00 00 00 00 00 00 00 C5 FD FD 02 06 00 00 00 00 00 00 00 05 FE FD 02 06 00 00 00 00 00 00 00 13 FE FD 02 06 00 00 00 00 00 00 00 22 4A CB 01 06 00 00 00 00 00 00 00 C2 FD FD 02 06 00 00 00 01 00 00 00 C5 FD FD 02 06 00 00 00 01 00 00 00 05 FE FD 02 06 00 00 00 01 00 00 00 13 FE FD 02 06 00 00 00 01 00 00 00 22 4A CB 01 06 00 00 00 01 00 00 00 C2 FD FD 02 07 00 00 00 00 00 00 00 C5 FD FD 02 07 00 00 00 00 00 00 00 05 FE FD 02 07 00 00 00 00 00 00 00 13 FE FD 02 07 00 00 00 00 00 00 00 22 4A CB 01 07 00 00 00 00 00 00 00 C2 FD FD 02 07 00 00 00 01 00 00 00 C5 FD FD 02 07 00 00 00 01 00 00 00 05 FE FD 02 07 00 00 00 01 00 00 00 13 FE FD 02 07 00 00 00 01 00 00 00 22 4A CB 01 07 00 00 00 01 00 00 00 C2 FD FD 02 08 00 00 00 00 00 00 00 C5 FD FD 02 08 00 00 00 00 00 00 00 05 FE FD 02 08 00 00 00 00 00 00 00 13 FE FD 02 08 00 00 00 00 00 00 00 22 4A CB 01 08 00 00 00 00 00 00 00 C2 FD FD 02 08 00 00 00 01 00 00 00 C5 FD FD 02 08 00 00 00 01 00 00 00 05 FE FD 02 08 00 00 00 01 00 00 00 13 FE FD 02 08 00 00 00 01 00 00 00 22 4A CB 01 08 00 00 00 01 00 00 00 C2 FD FD 02 00 00 A3 00 26 71 0F 00 F3 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 23 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 54 71 0F 00 F4 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 58 71 0F 00 F5 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 32 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 B5 E6 0F 00 F8 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BD 68 32 01 BD 68 32 01 10 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 FD 4A 0F 00 FC 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 14 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 29 71 0F 00 FD 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 23 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E5 DE 0F 00 FF 20 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0B D1 10 00 00 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 23 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1C F9 19 00 01 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 49 4B 4C 00 02 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 63 72 4C 00 03 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 76 72 4C 00 04 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 32 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 77 72 4C 00 05 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BE 68 32 01 BE 68 32 01 0B 00 00 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 FC 4A 0F 00 06 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 BF 68 32 01 BF 68 32 01 0B 00 00 00 0C 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F6 4B 0F 00 07 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 FF FF FF FF 0F 00 00 00 BF 68 32 01 BF 68 32 01 0B 00 00 00 0C 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 20 4E 00 3A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 00 00 00 00 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 79 72 4C 00 3B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 42 BC 4E 00 3C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0A 31 10 00 3D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 54 72 4C 00 3E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 69 0F 00 3F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 78 4B 0F 00 40 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A8 F8 19 00 41 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 09 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 21 A6 1B 00 42 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B4 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 84 5C 10 00 43 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 98 34 10 00 44 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 98 0F 00 45 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 75 83 10 00 46 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 18 0A 10 00 47 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0B 31 10 00 48 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 61 72 4C 00 49 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4B BC 4E 00 4A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 39 70 4D 00 4B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 10 47 4E 00 4C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C8 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B3 E6 0F 00 4D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 73 34 10 00 4E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5C 98 0F 00 4F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 DF 82 10 00 50 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 57 4B 4C 00 51 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B8 34 10 00 52 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 48 94 0F 00 53 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A9 7E 10 00 54 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 96 0D 10 00 55 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3C 95 4E 00 56 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E9 F8 19 00 57 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 1C D1 10 00 58 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3F 71 0F 00 59 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F5 4B 0F 00 5A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B9 82 10 00 5B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B7 D0 10 00 5C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 98 0F 00 5D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 51 4B 4C 00 5E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0F 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C0 DE 0F 00 5F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 10 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 E3 4E 00 60 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B4 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D0 27 4E 00 61 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E8 94 50 00 62 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 26 71 0F 00 63 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B1 3E 52 00 64 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D6 D0 10 00 65 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 6B 71 0F 00 66 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 71 4B 0F 00 67 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8B F8 10 00 68 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A9 F8 19 00 69 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 3D 6A 32 01 3D 6A 32 01 12 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 89 83 4F 00 6A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 BE 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C5 F7 10 00 6B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5F 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 02 20 4E 00 6C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F0 F8 4D 00 6D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 43 BC 4E 00 6E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 10 31 10 00 6F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 71 72 4C 00 70 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 71 0F 00 71 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 A9 57 10 00 72 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 08 20 4E 00 73 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 08 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 4B 4C 00 74 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 6A 5C 10 00 75 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 AB 34 10 00 76 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 E9 94 50 00 77 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 47 BC 4E 00 78 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 CA 2C 10 00 79 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 60 72 4C 00 7A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4A BC 4E 00 7B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 38 70 4D 00 7C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 7A C0 4C 00 7D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0B 00 00 00 0D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B0 CD 4F 00 7E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 AA 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D1 E6 0F 00 7F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 CB 34 10 00 80 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 40 98 0F 00 81 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 32 83 10 00 82 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 90 0D 10 00 83 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 C3 2C 10 00 84 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 42 98 0F 00 85 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 FD 09 10 00 86 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B5 DE 0F 00 87 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0D 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 24 A6 1B 00 88 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B4 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 62 E6 0F 00 89 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 EF D0 10 00 8A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5B 98 0F 00 8B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 CA 4A 0F 00 8C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 DC D0 10 00 8D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4E 98 0F 00 8E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3C 46 0F 00 8F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4A 4B 4C 00 90 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 9D E6 0F 00 91 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 0F 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 4B 4B 4C 00 92 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 D3 F8 19 00 93 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F2 D0 10 00 94 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3C 71 0F 00 95 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 67 4B 0F 00 96 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 EE D0 10 00 97 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 58 71 0F 00 98 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 45 4C 0F 00 99 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8E F8 10 00 9A 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 2D 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 95 F8 19 00 9B 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 E9 69 32 01 E9 69 32 01 11 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 3A 95 4E 00 9C 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 20 4E 00 9D 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 46 BC 4E 00 9E 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 50 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 0A 10 00 9F 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0D 31 10 00 A0 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 72 4C 00 A1 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7A 71 0F 00 A2 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3D 70 4D 00 A3 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 77 5C 10 00 A4 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F9 23 4E 00 A5 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 08 00 00 00 0A 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 47 4E 00 A6 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 BE 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 5C 10 00 A7 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F 34 10 00 A8 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 34 98 0F 00 A9 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 31 10 00 AA 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 57 98 0F 00 AB 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 45 BC 4E 00 AC 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF 09 10 00 AD 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AD DE 0F 00 AE 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 70 4B 0F 00 AF 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0B 00 00 00 0D 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 64 4D 00 B0 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 F0 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AF E6 0F 00 B1 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 98 34 10 00 B2 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 31 98 0F 00 B3 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 77 83 10 00 B4 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B D1 10 00 B5 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 58 98 0F 00 B6 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 DE 82 10 00 B7 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE 09 10 00 B8 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8A E6 0F 00 B9 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0D 00 00 00 0F 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 A6 1B 00 BA 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 BE 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 24 F9 19 00 BB 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D7 D0 10 00 BC 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 57 71 0F 00 BD 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 EC 4A 0F 00 BE 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E3 D0 10 00 BF 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 64 98 0F 00 C0 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F3 4A 0F 00 C1 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A9 F8 10 00 C2 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8D E6 0F 00 C3 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 0F 00 00 00 11 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 89 83 4F 00 C4 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 8C 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 28 F9 19 00 C5 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 D1 10 00 C6 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 58 BF 0F 00 C7 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 55 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 B0 3E 52 00 C8 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 46 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E2 D0 10 00 C9 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 4F 71 0F 00 CA 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 28 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 77 4B 0F 00 CB 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2A F8 10 00 CC 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 37 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 F9 19 00 CD 21 9A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 19 00 00 00 FF FF FF FF 0F 00 00 00 EA 69 32 01 EA 69 32 01 11 00 00 00 13 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 00 00"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket warpCS(MapleClient c) {
        return warpCS(c, false);
    }

    public static MaplePacket warpMTS(MapleClient c) {
        return warpCS(c, true);
    }

    public static MaplePacket sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x15); //operation
        mplew.writeInt(pages * 10);
        mplew.writeInt(items.size()); //number of items
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);

        for (int i = 0; i < items.size(); i++) {
            MTSItemInfo item = items.get(i);
            addItemInfo(mplew, item.getItem(), true, true);
            mplew.writeInt(item.getID()); //id
            mplew.writeInt(item.getTaxes()); //this + below = price
            mplew.writeInt(item.getPrice()); //price
            mplew.writeLong(0);
            mplew.writeInt(KoreanDateUtil.getQuestTimestamp(item.getEndingDate()));
            mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
            mplew.writeMapleAsciiString(item.getSeller()); //char name
            for (int ii = 0; ii < 28; ii++) {
                mplew.write(0);
            }
        }
        mplew.write(1);

        return mplew.getPacket();
    }

    public static MaplePacket showMTSCash(MapleCharacter p) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION2.getValue());
        mplew.writeInt(p.getCSPoints(1)); // NexonCash = PaypalNx
        mplew.writeInt(p.getCSPoints(2)); // maplepoint
        return mplew.getPacket();
    }

    public static MaplePacket MTSWantedListingOver(int nx, int items) {
        //Listing period on [WANTED] items
        //(just a message stating you have gotten your NX/items back, only displays if nx or items != 0)
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(nx);
        mplew.writeInt(items);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x1D);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x33);
        return mplew.getPacket();
    }

    public static MaplePacket MTSFailBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x34);
        mplew.write(0x42);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmTransfer(int quantity, int pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);
        return mplew.getPacket();
    }

    public static MaplePacket NotYetSoldInv(List<MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x23);

        mplew.writeInt(items.size());

        if (items.size() != 0) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true, true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //this + below = price
                mplew.writeInt(item.getPrice()); //price
                mplew.writeLong(0);
                mplew.writeInt(KoreanDateUtil.getQuestTimestamp(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int ii = 0; ii < 28; ii++) {
                    mplew.write(0);
                }
            }
        } else {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static MaplePacket TransferInventory(List<MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x21);

        mplew.writeInt(items.size());

        if (items.size() != 0) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true, true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //taxes
                mplew.writeInt(item.getPrice()); //price
                mplew.writeLong(0);
                mplew.writeInt(KoreanDateUtil.getQuestTimestamp(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int ii = 0; ii < 28; ii++) {
                    mplew.write(0);
                }
            }
        }
        mplew.write(0xD0 + items.size());
        mplew.write(HexTool.getByteArrayFromHexString("FF FF FF 00"));

        return mplew.getPacket();
    }

    public static MaplePacket enableMTS() {
        return enableCSorMTS();
    }

    public static MaplePacket TrockRefreshMapList(int characterid, boolean vip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Connection con = DatabaseConnection.getConnection();
        mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(3); // unknown at this point
        mplew.write(vip ? 1 : 0);
        try {
            PreparedStatement ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ? LIMIT 10");
            ps.setInt(1, characterid);
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next()) {
                mplew.writeInt(rs.getInt("mapid"));
                i++;
            }
            for (; i < 10; i++) {
                mplew.write(CHAR_INFO_MAGIC);
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
        }
        return mplew.getPacket();
    }
}