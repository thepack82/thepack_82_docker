package net.sf.odinms.client;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javax.script.ScriptEngine;
import net.sf.odinms.server.maps.FakeCharacter;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.database.DatabaseException;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.net.world.MapleMessengerCharacter;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.guild.MapleGuildCharacter;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.scripting.npc.NPCConversationManager;
import net.sf.odinms.scripting.quest.QuestScriptManager;
import net.sf.odinms.scripting.quest.QuestActionManager;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.IPAddressTool;
import net.sf.odinms.tools.MapleAESOFB;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleClient {

    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;
    public static final int LOGIN_WAITING = 3;
    public static final String CLIENT_KEY = "CLIENT";
    private static final Logger log = LoggerFactory.getLogger(MapleClient.class);
    private MapleAESOFB send;
    private MapleAESOFB receive;
    private IoSession session;
    private MapleCharacter player;
    private int channel = 1;
    private int accId = 1;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private Calendar birthday = null;
    private Calendar tempban = null;
    private String accountName;
    private int world;
    private long lastPong;
    private boolean gm;
    private int gmlevel;
    private byte greason = 1;
    private Map<Pair<MapleCharacter, Integer>, Integer> timesTalked = new HashMap<Pair<MapleCharacter, Integer>, Integer>(); //npcid, times
    private Set<String> macs = new HashSet<String>();
    private Map<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();
    private ScheduledFuture<?> idleTask = null;

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public MapleAESOFB getSendCrypto() {
        return send;
    }

    public synchronized IoSession getSession() {
        return session;
    }

    public MapleCharacter getPlayer() {
        return player;
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    public void sendCharList(int server) {
        this.session.write(MaplePacketCreator.getCharList(this, server));
    }

    public List<MapleCharacter> loadCharacters(int serverId) {
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            try {
                chars.add(MapleCharacter.loadCharFromDB(cni.id, this, false));
            } catch (Exception e) {
                log.error("MapleClient.java -> loadCharacters");
            }
        }
        return chars;
    }

    public List<String> loadCharacterNames(int serverId) {
        List<String> chars = new LinkedList<String>();
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    private List<CharNameAndId> loadCharactersInternal(int serverId) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        List<CharNameAndId> chars = new LinkedList<CharNameAndId>();
        try {
            ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ?");
            ps.setInt(1, this.accId);
            ps.setInt(2, serverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            log.error("MapleClient -> LoadCharactersInternal");
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
        Calendar lTempban = Calendar.getInstance();
        long blubb = rs.getLong("tempban");
        if (blubb == 0) {
            lTempban.setTimeInMillis(0);
            return lTempban;
        }
        Calendar today = Calendar.getInstance();
        lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
        if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
        }
        lTempban.setTimeInMillis(0);
        return lTempban;
    }

    public Calendar getTempBanCalendar() {
        return tempban;
    }

    public byte getBanReason() {
        return greason;
    }

    public boolean hasBannedIP() {
        boolean ret = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')");
            ps.setString(1, session.getRemoteAddress().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("hasBannedIP MapleClient.java");
        }
        return ret;
    }

    public boolean hasBannedMac() {
        if (macs.isEmpty()) {
            return false;
        }
        boolean ret = false;
        int i = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
            for (i = 0; i < macs.size(); i++) {
                sql.append("?");
                if (i != macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            PreparedStatement ps = con.prepareStatement(sql.toString());
            i = 0;
            for (String mac : macs) {
                i++;
                ps.setString(i, mac);
            }
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("hasBannedMac error -> MapleClient.java");
        }
        return ret;
    }

    private void loadMacsIfNescessary() throws SQLException {
        if (macs.isEmpty()) {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?");
            ps.setInt(1, accId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String[] macData = rs.getString("macs").split(", ");
                for (String mac : macData) {
                    if (!mac.equals("")) {
                        macs.add(mac);
                    }
                }
            } else {
                throw new RuntimeException("loadMacsIfNescessary error");
            }
            rs.close();
            ps.close();
        }
    }

    public void banMacs() {
        Connection con = DatabaseConnection.getConnection();
        try {
            loadMacsIfNescessary();
            List<String> filtered = new LinkedList<String>();
            PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filtered.add(rs.getString("filter"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
            for (String mac : macs) {
                boolean matched = false;
                for (String filter : filtered) {
                    if (mac.matches(filter)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    ps.setString(1, mac);
                    try {
                        ps.executeUpdate();
                    } catch (SQLException e) {
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            log.error("banMacs Error");
        }
    }

    public int finishLogin(boolean success) {
        if (success) {
            synchronized (MapleClient.class) {
                if (getLoginState() > MapleClient.LOGIN_NOTLOGGEDIN && getLoginState() != MapleClient.LOGIN_WAITING) {
                    loggedIn = false;
                    return 7;
                }
                updateLoginState(MapleClient.LOGIN_LOGGEDIN);
            }
            return 0;
        } else {
            return 10;
        }
    }

    public int login(String login, String pwd, boolean ipMacBanned) {
        int loginok = 5;
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id,password,salt,tempban,banned,gm,macs,lastknownip,greason FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int banned = rs.getInt("banned");
                accId = rs.getInt("id");
                gmlevel = rs.getInt("gm");
                String passhash = rs.getString("password");
                String salt = rs.getString("salt");
                gm = gmlevel > 2;
                greason = rs.getByte("greason");
                tempban = getTempBanCalendar(rs);
                if ((banned == 0 && !ipMacBanned) || banned == -1) {
                    PreparedStatement ips = con.prepareStatement("INSERT INTO iplog (accountid, ip) VALUES (?, ?)");
                    ips.setInt(1, accId);
                    ips.setString(2, session.getRemoteAddress().toString());
                    ips.executeUpdate();
                    ips.close();
                }
                if (!rs.getString("lastknownip").equals(session.getRemoteAddress().toString())) {
                    PreparedStatement lkip = con.prepareStatement("UPDATE accounts SET lastknownip = ? where id = ?");
                    String sockAddr = session.getRemoteAddress().toString();
                    lkip.setString(1, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
                    lkip.setInt(2, accId);
                    lkip.executeUpdate();
                    lkip.close();
                }
                ps.close();
                if (banned == 1) {
                    loginok = 3;
                } else {
                    if (banned == -1) {
                        unban();
                    }
                    if (getLoginState() > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
                        loggedIn = false;
                        loginok = 7;
                    } else {
                        boolean updatePasswordHash = false;
                        if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
                            loginok = 0;
                            updatePasswordHash = true;
                        } else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
                            loginok = 0;
                            updatePasswordHash = true;
                        } else if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
                            loginok = 0;
                        } else {
                            loggedIn = false;
                            loginok = 4;
                        }
                        if (updatePasswordHash) {
                            PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?");
                            try {
                                String newSalt = LoginCrypto.makeSalt();
                                pss.setString(1, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
                                pss.setString(2, newSalt);
                                pss.setInt(3, accId);
                                pss.executeUpdate();
                            } finally {
                                pss.close();
                            }
                        }
                    }
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        return loginok;
    }

    public static String getChannelServerIPFromSubnet(String clientIPAddress, int channel) {
        long ipAddress = IPAddressTool.dottedQuadToLong(clientIPAddress);
        Properties subnetInfo = LoginServer.getInstance().getSubnetInfo();
        if (subnetInfo.contains("net.sf.odinms.net.login.subnetcount")) {
            int subnetCount = Integer.parseInt(subnetInfo.getProperty("net.sf.odinms.net.login.subnetcount"));
            for (int i = 0; i < subnetCount; i++) {
                String[] connectionInfo = subnetInfo.getProperty("net.sf.odinms.net.login.subnet." + i).split(":");
                long subnet = IPAddressTool.dottedQuadToLong(connectionInfo[0]);
                long channelIP = IPAddressTool.dottedQuadToLong(connectionInfo[1]);
                int channelNumber = Integer.parseInt(connectionInfo[2]);

                if (((ipAddress & subnet) == (channelIP & subnet)) && (channel == channelNumber)) {
                    return connectionInfo[1];
                }
            }
        }
        return "0.0.0.0";
    }

    private void unban() {
        int i;
        try {
            Connection con = DatabaseConnection.getConnection();
            loadMacsIfNescessary();
            StringBuilder sql = new StringBuilder("DELETE FROM macbans WHERE mac IN (");
            for (i = 0; i < macs.size(); i++) {
                sql.append("?");
                if (i != macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            PreparedStatement ps = con.prepareStatement(sql.toString());
            i = 0;
            for (String mac : macs) {
                i++;
                ps.setString(i, mac);
            }
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM ipbans WHERE ip LIKE CONCAT(?, '%')");
            ps.setString(1, getSession().getRemoteAddress().toString().split(":")[0]);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET banned = 0 WHERE id = ?");
            ps.setInt(1, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
        }
    }

    public void updateMacs(String macData) {
        for (String mac : macData.split(", ")) {
            macs.add(mac);
        }
        StringBuilder newMacData = new StringBuilder();
        Iterator<String> iter = macs.iterator();
        while (iter.hasNext()) {
            String cur = iter.next();
            newMacData.append(cur);
            if (iter.hasNext()) {
                newMacData.append(", ");
            }
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
            ps.setString(1, newMacData.toString());
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("Error saving MACs", e);
        }
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return this.accId;
    }

    public int getGMLevel() {
        return this.gmlevel;
    }

    public void updateLoginState(int newstate) { // TODO hide?
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
            ps.setInt(1, newstate);
            ps.setInt(2, getAccID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        if (newstate == MapleClient.LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
        } else if (newstate == MapleClient.LOGIN_WAITING) {
            loggedIn = false;
            serverTransition = false;
        } else {
            serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION);
            loggedIn = !serverTransition;
        }
    }

    public int getLoginState() { // TODO hide?
        try {
            PreparedStatement ps;
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT loggedin, lastlogin, UNIX_TIMESTAMP(birthday) as birthday FROM accounts WHERE id = ?");
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                throw new DatabaseException("Everything sucks");
            }
            birthday = Calendar.getInstance();
            long blubb = rs.getLong("birthday");
            if (blubb > 0) {
                birthday.setTimeInMillis(blubb * 1000);
            }
            int state = rs.getInt("loggedin");
            if (state == MapleClient.LOGIN_SERVER_TRANSITION) {
                Timestamp ts = rs.getTimestamp("lastlogin");
                long t = ts.getTime();
                long now = System.currentTimeMillis();
                if (t + 30000 < now) { // connecting to chanserver timeout
                    state = MapleClient.LOGIN_NOTLOGGEDIN;
                    updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
                }
            }
            rs.close();
            ps.close();
            if (state == MapleClient.LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else {
                loggedIn = false;
            }
            return state;
        } catch (SQLException e) {
            loggedIn = false;
            log.error("ERROR", e);
            throw new DatabaseException("getLoginState() error");
        }
    }

    public boolean checkBirthDate(Calendar date) {
        if (date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        return false;
    }

    public void disconnect() {
        MapleCharacter chr = this.getPlayer();
        if (chr != null && isLoggedIn()) {
            if (chr.hasFakeChar()) {
                for (FakeCharacter ch : chr.getFakeChars()) {
                    ch.getFakeChar().getMap().removePlayer(ch.getFakeChar());
                }
            }
            if (chr.getTrade() != null) {
                MapleTrade.cancelTrade(chr);
            }
            chr.cancelAllBuffs();
            if (chr.getEventInstance() != null) {
                chr.getEventInstance().playerDisconnected(chr);
            }
            getPlayer().savePet();
            getPlayer().unequipAllPets();
            try {
                WorldChannelInterface wci = getChannelServer().getWorldInterface();
                if (chr.getMessenger() != null) {
                    MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
                    wci.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
                    chr.setMessenger(null);
                }
            } catch (RemoteException e) {
                getChannelServer().reconnectWorld();
            }
            getPlayer().unequipAllPets();
            if (!chr.isAlive()) {
                chr.setHp(50, true);
            }
            chr.setMessenger(null);
            chr.getCheatTracker().dispose();
            chr.saveToDB(true);
            chr.getMap().removePlayer(chr);
            try {
                WorldChannelInterface wci = getChannelServer().getWorldInterface();
                if (chr.getParty() != null) {
                    MaplePartyCharacter chrp = new MaplePartyCharacter(chr);
                    chrp.setOnline(false);
                    wci.updateParty(chr.getParty().getId(), PartyOperation.LOG_ONOFF, chrp);
                }
                if (!this.serverTransition && isLoggedIn()) {
                    wci.loggedOff(chr.getName(), chr.getId(), channel, chr.getBuddylist().getBuddyIds());
                } else {
                    wci.loggedOn(chr.getName(), chr.getId(), channel, chr.getBuddylist().getBuddyIds());
                }
                if (chr.getGuildId() > 0) {
                    wci.setGuildMemberOnline(chr.getMGC(), false, -1);
                }
            } catch (RemoteException e) {
                getChannelServer().reconnectWorld();
            } catch (Exception e) {
                log.error(getLogMessage(this, "ERROR"), e);
            } finally {
                if (getChannelServer() != null) {
                    getChannelServer().removePlayer(chr);
                } else {
                    log.error(getLogMessage(this, "No channelserver associated to char {}", chr.getName()));
                }
            }
        }
        if (!this.serverTransition && isLoggedIn()) {
            this.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
        }
        NPCScriptManager npcsm = NPCScriptManager.getInstance();
        if (npcsm != null) {
            npcsm.dispose(this);
        }
    }

    public void dropDebugMessage(MessageCallback mc) {
        StringBuilder builder = new StringBuilder();
        builder.append("Connected: " + getSession().isConnected() + " Closing: " + getSession().isClosing() + " ClientKeySet: " + getSession().getAttribute(MapleClient.CLIENT_KEY) != null);
        builder.append(" loggedin: " + isLoggedIn() + " has char: " + getPlayer() != null);
        mc.dropMessage(builder.toString());
    }

    public int getChannel() {
        return channel;
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(getChannel());
    }

    public boolean deleteCharacter(int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name FROM characters WHERE id = ? AND accountid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, accId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return false;
            }
            if (rs.getInt("guildid") > 0) {
                MapleGuildCharacter mgc = new MapleGuildCharacter(cid, 0, rs.getString("name"), -1, 0, rs.getInt("guildrank"), rs.getInt("guildid"), false);
                try {
                    LoginServer.getInstance().getWorldInterface().deleteGuildCharacter(mgc);
                } catch (RemoteException re) {
                    return false;
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM characters WHERE id = ?");
            ps.setInt(1, cid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM cheatlog, wishlist, eventstats, famelog, inventoryitems, keymap, queststatus, savedlocation, skillmacros, skills WHERE characterid = ?");
            ps.setInt(1, cid);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            log.error("DeleteChar error", e);
        }
        return false;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void pongReceived() {
        lastPong = System.currentTimeMillis();
    }

    public void sendPing() {
        final long then = System.currentTimeMillis();
        getSession().write(MaplePacketCreator.getPing());
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    if (lastPong - then < 0) {
                        if (getSession().isConnected()) {
                            log.info(getLogMessage(MapleClient.this, "\r\nAuto DC : Ping Timeout"));
                            getSession().close();
                        }
                    }
                } catch (NullPointerException e) {
                }
            }
        }, 15000);
    }

    public static String getLogMessage(MapleClient cfor, String message) {
        return getLogMessage(cfor, message, new Object[0]);
    }

    public static String getLogMessage(MapleCharacter cfor, String message) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message);
    }

    public static String getLogMessage(MapleCharacter cfor, String message, Object... parms) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
    }

    public static String getLogMessage(MapleClient cfor, String message, Object... parms) {
        StringBuilder builder = new StringBuilder();
        if (cfor != null) {
            if (cfor.getPlayer() != null) {
                builder.append("<"+MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName())+" (cid: "+cfor.getPlayer().getId()+")> ");
            }
            if (cfor.getAccountName() != null) {
                builder.append("(Account: "+MapleCharacterUtil.makeMapleReadable(cfor.getAccountName())+") ");
            }
        }
        builder.append(message);
        for (Object parm : parms) {
            int start = builder.indexOf("{}");
            builder.replace(start, start + 2, parm.toString());
        }
        return builder.toString();
    }

    public static int findAccIdForCharacterName(String charName) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, charName);
            ResultSet rs = ps.executeQuery();
            int ret = -1;
            if (rs.next()) {
                ret = rs.getInt("accountid");
            }
            return ret;
        } catch (SQLException e) {
            log.error("SQL THROW");
        }
        return -1;
    }

    public Set<String> getMacs() {
        return Collections.unmodifiableSet(macs);
    }

    public boolean isGm() {
        return gm;
    }

    public void setScriptEngine(String name, ScriptEngine e) {
        engines.put(name, e);
    }

    public ScriptEngine getScriptEngine(String name) {
        return engines.get(name);
    }

    public void removeScriptEngine(String name) {
        engines.remove(name);
    }

    public ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public void setIdleTask(ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    public NPCConversationManager getCM() {
        return NPCScriptManager.getInstance().getCM(this);
    }

    public QuestActionManager getQM() {
        return QuestScriptManager.getInstance().getQM(this);
    }

    public void setTimesTalked(int n, int t) {
        timesTalked.remove(new Pair<MapleCharacter, Integer>(getPlayer(), n));
        timesTalked.put(new Pair<MapleCharacter, Integer>(getPlayer(), n), t);
    }

    public int getTimesTalked(int n) {
        if (timesTalked.get(new Pair<MapleCharacter, Integer>(getPlayer(), n)) == null) {
            setTimesTalked(n, 0);
        }
        return timesTalked.get(new Pair<MapleCharacter, Integer>(getPlayer(), n));
    }

    private static class CharNameAndId {

        public String name;
        public int id;

        public CharNameAndId(String name, int id) {
            super();
            this.name = name;
            this.id = id;
        }
    }
}
