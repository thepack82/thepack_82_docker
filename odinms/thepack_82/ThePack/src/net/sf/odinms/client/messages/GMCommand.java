package net.sf.odinms.client.messages;

import java.util.*;
import net.sf.odinms.server.*;
import net.sf.odinms.server.life.*;
import net.sf.odinms.server.maps.*;
import net.sf.odinms.tools.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import net.sf.odinms.client.*;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.PlayerCoolDownValueHolder;
import net.sf.odinms.net.world.remote.WorldLocation;

public class GMCommand {

    public static boolean executeGMCommand(MapleClient c, MessageCallback mc, String line) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        String[] splitted = line.split(" ");
        if (splitted[0].equals("!ban")) {
            try {
                String originalReason = StringUtil.joinStringFrom(splitted, 2);
                String reason = player.getName() + " banned " + splitted[1] + ": " + originalReason;
                MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (target != null) {
                    if (target.gmLevel() < 3 || player.gmLevel() > 4) {
                        String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
                        String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        reason += " (IP: " + ip + ")";
                        target.ban(reason);
                        mc.dropMessage("Banned " + readableTargetName + " ipban for " + ip + " reason: " + originalReason);
                        try {
                            ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(0, readableTargetName + " has been banned for " + originalReason + ".").getBytes());
                        } catch (RemoteException e) {
                            cserv.reconnectWorld();
                        }
                    } else {
                        mc.dropMessage("You may not ban GMs.");
                    }
                } else {
                    if (MapleCharacter.ban(splitted[1], reason, false)) {
                        mc.dropMessage("Offline Banned " + splitted[1]);
                    } else {
                        mc.dropMessage("Failed to ban " + splitted[1]);
                    }
                }
            } catch (NullPointerException e) {
                mc.dropMessage(splitted[1] + " could not be banned.");
            }
        } else if (splitted[0].equals("!cancelbuffs")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).cancelAllBuffs();
        } else if (splitted[0].equals("!charinfo")) {
            StringBuilder builder = new StringBuilder();
            MapleCharacter other = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            builder.append(MapleClient.getLogMessage(other, "") + " at " + other.getPosition().x + "/" + other.getPosition().y + "/" + other.getMap().getFootholds().findBelow(other.getPosition()).getId() + " " + other.getHp() + "/" + other.getCurrentMaxHp() + "hp " + other.getMp() + "/" + other.getCurrentMaxMp() + "mp " + other.getExp() + "exp" + " remoteAddress: " + other.getClient().getSession().getRemoteAddress());
            mc.dropMessage(builder.toString());
            other.getClient().dropDebugMessage(mc);
        } else if (splitted[0].equals("!chattype")) {
            player.setGMChat(!player.getGMChat());
            mc.dropMessage("Done.");
        } else if (splitted[0].equals("!cleardrops")) {
            MapleMap map = player.getMap();
            List<MapleMapObject> items = map.getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
            for (MapleMapObject i : items) {
                map.removeMapObject(i);
                map.broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
            }
            mc.dropMessage("You have destroyed " + items.size() + " items on the ground.");
        } else if (splitted[0].equals("!clock")) {
            player.getMap().broadcastMessage(MaplePacketCreator.getClock(Integer.parseInt(splitted[1])));
        } else if (splitted[0].equals("!connected")) {
            try {
                Map<Integer, Integer> connected = cserv.getWorldInterface().getConnected();
                StringBuilder conStr = new StringBuilder("Connected Clients: ");
                boolean first = true;
                for (int i : connected.keySet()) {
                    if (!first) {
                        conStr.append(", ");
                    } else {
                        first = false;
                    }
                    if (i == 0) {
                        conStr.append("Total: " + connected.get(i));
                    } else {
                        conStr.append("Ch" + i + ": " + connected.get(i));
                    }
                }
                mc.dropMessage(conStr.toString());
            } catch (RemoteException e) {
                cserv.reconnectWorld();
            }
        } else if (splitted[0].equals("!dc")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.getClient().getSession().close();
            victim.getClient().disconnect();
            victim.saveToDB(true);
            cserv.removePlayer(victim);
        } else if (splitted[0].equals("!drop") || splitted[0].equals("!droprandomstatitem") || splitted[0].equals("!item")) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int itemId = Integer.parseInt(splitted[1]);
            short quantity = (short) getOptionalIntArg(splitted, 2, 1);
            IItem toDrop;
            if (splitted[0].equals("!drop")) {
                if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    toDrop = ii.getEquipById(itemId);
                } else {
                    toDrop = new Item(itemId, (byte) 0, quantity);
                }
                toDrop.setOwner(player.getName());
                player.getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
            } else if (splitted[0].equals("!item")) {
                if (itemId >= 5000000 && itemId <= 5000100) {
                    MaplePet.createPet(itemId);
                } else {
                    MapleInventoryManipulator.addById(c, itemId, quantity, "", player.getName());
                }
            } else {
                if (!MapleItemInformationProvider.getInstance().getInventoryType(itemId).equals(MapleInventoryType.EQUIP)) {
                    mc.dropMessage("Command can only be used for equips.");
                } else {
                    toDrop = MapleItemInformationProvider.getInstance().randomizeStats((Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId));
                    player.getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
                }
            }
        } else if (splitted[0].equals("!event")) {
            if (player.getClient().getChannelServer().eventOn == false) {
                int mapid = getOptionalIntArg(splitted, 1, c.getPlayer().getMapId());
                player.getClient().getChannelServer().eventOn = true;
                player.getClient().getChannelServer().eventMap = mapid;
                try {
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Event] The event has started in Channel " + c.getChannel() + " in " + player.getMapId() + "!").getBytes());
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            } else {
                player.getClient().getChannelServer().eventOn = false;
                try {
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Event] The event has ended. Thanks to all of those who participated.").getBytes());
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            }
        } else if (splitted[0].equals("!fakechar")) {
            for (int i = 0; i < getOptionalIntArg(splitted, 1, 1); i++) {
                FakeCharacter fc = new FakeCharacter(player, player.getId() + player.getFakeChars().size() + 1);
                player.addFakeChar(fc);
            }
            mc.dropMessage("Please move around for it to take effect.");
        } else if (splitted[0].equals("!fakerelog")) {
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            player.getMap().removePlayer(player);
            player.getMap().addPlayer(player);
        } else if (splitted[0].equals("!fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int fame = Integer.parseInt(splitted[2]);
            victim.setFame(fame);
            victim.updateSingleStat(MapleStat.FAME, fame);
        } else if (splitted[0].equals("!giftnx")) {
            for (int i = 1; i < 5; i *= 2) {
                cserv.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(i, Integer.parseInt(splitted[2]));
            }
            mc.dropMessage("Done");
        } else if (splitted[0].equals("!god")) {
            player.setGodMode(!player.isGodMode());
            mc.dropMessage("Done.");
        } else if (splitted[0].equals("!heal")) {
            player.setHp(player.getMaxHp());
            player.updateSingleStat(MapleStat.HP, player.getMaxHp());
            player.setMp(player.getMaxMp());
            player.updateSingleStat(MapleStat.MP, player.getMaxMp());
        } else if (splitted[0].equals("!healmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    mch.setHp(mch.getMaxHp());
                    mch.updateSingleStat(MapleStat.HP, mch.getMaxHp());
                    mch.setMp(mch.getMaxMp());
                    mch.updateSingleStat(MapleStat.MP, mch.getMaxMp());
                }
            }
        } else if (splitted[0].equals("!healperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setHp(victim.getMaxHp());
            victim.updateSingleStat(MapleStat.HP, victim.getMaxHp());
            victim.setMp(victim.getMaxMp());
            victim.updateSingleStat(MapleStat.MP, victim.getMaxMp());
        } else if (splitted[0].equals("!hurt")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setHp(1);
            victim.updateSingleStat(MapleStat.HP, 1);
        } else if (splitted[0].equals("!id") || splitted[0].equals("!search")) {
            try {
                URL url = new URL("http://www.mapletip.com/search_java.php?search_value=" + splitted[1] + "&check=true");
                URLConnection urlConn = url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setUseCaches(false);
                BufferedReader dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String s;
                while ((s = dis.readLine()) != null) {
                    mc.dropMessage(s);
                }
                dis.close();
            } catch (MalformedURLException mue) {
            } catch (IOException ioe) {
            }
        } else if (splitted[0].equals("!jail")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int mapid = 200090300;
            if (splitted.length > 2 && splitted[1].equals("2")) {
                mapid = 980000404;
                victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
            }
            if (victim != null) {
                MapleMap target = cserv.getMapFactory().getMap(mapid);
                victim.changeMap(target, target.getPortal(0));
                mc.dropMessage(victim.getName() + " was jailed!");
            } else {
                mc.dropMessage(splitted[1] + " not found!");
            }
        } else if (splitted[0].equals("!job")) {
            player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
        } else if (splitted[0].equals("!jobperson")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeJob(MapleJob.getById(Integer.parseInt(splitted[2])));
        } else if (splitted[0].equals("!karma")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
            if (splitted[1].equals("up")) {
                if (victim.getKarma() < 25 || player.getGMLevel() > 3) {
                    victim.upKarma();
                    mc.dropMessage("You have raised " + victim + "'s karma. It is currently at " + victim.getKarma() + ".");
                } else {
                    mc.dropMessage("You are unable raise " + victim + "'s karma level.");
                }
            } else if (splitted[1].equals("down")) {
                if (victim.getKarma() > -25 || player.getGMLevel() > 3) {
                    victim.downKarma();
                    mc.dropMessage("You have dropped " + victim + "'s karma. It is currently at " + victim.getKarma() + ".");
                } else {
                    mc.dropMessage("You cannot drop " + victim + "'s karma level anymore.");
                }
            } else {
                mc.dropMessage("Syntax: !karma [up/down] [user]");
            }
        } else if (splitted[0].equals("!kill")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setHp(0);
            victim.updateSingleStat(MapleStat.HP, 0);
        } else if (splitted[0].equals("!killall") || splitted[0].equals("!monsterdebug")) {
            MapleMap map = player.getMap();
            List<MapleMapObject> monsters = map.getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            boolean kill = splitted[0].equals("!killall");
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                if (kill) {
                    map.killMonster(monster, player, true);
                    monster.giveExpToCharacter(player, monster.getExp(), true, 1);
                } else {
                    mc.dropMessage("Monster " + monster.toString());
                }
            }
            if (kill) {
                mc.dropMessage("Killed " + monsters.size() + " monsters.");
            }
        } else if (splitted[0].equals("!killallmany")) {
            int size = 0;
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                MapleMap map = mch.getMap();
                List<MapleMapObject> monsters = map.getMapObjectsInRange(mch.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    map.killMonster(monster, player, true);
                    monster.giveExpToCharacter(player, monster.getExp(), true, 1);
                }
                size += monsters.size();
            }
            mc.dropMessage("Killed " + size + " monsters.");
        } else if (splitted[0].equals("!killeveryone")) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                mch.setHp(0);
                mch.updateSingleStat(MapleStat.HP, 0);
            }
        } else if (splitted[0].equals("!killmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                mch.setHp(0);
                mch.updateSingleStat(MapleStat.HP, 0);
            }
        } else if (splitted[0].equals("!levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(Integer.parseInt(splitted[2]));
            victim.gainExp(-victim.getExp(), false, false);
            victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());
        } else if (splitted[0].equals("!levelup")) {
            player.gainExp(ExpTable.getExpNeededForLevel(player.getLevel() + 1) - player.getExp(), false, false);
        } else if (splitted[0].equals("!lolhaha")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            mc.dropMessage("You have switched the gender of " + victim + ".");
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setGender(1 - victim.getGender());
        } else if (splitted[0].equals("!mesoperson")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).gainMeso(Integer.parseInt(splitted[2]), true);
        } else if (splitted[0].equals("!mesos")) {
            player.gainMeso(Integer.parseInt(splitted[1]), true);
        } else if (splitted[0].equals("!mute")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setCanTalk(victim.getCanTalk() ? 2 : 0);
            mc.dropMessage(victim.getName() + " has been muted or unmuted!");
        } else if (splitted[0].equals("!notice") || (splitted[0].equals("!say"))) {
            String type = "[Notice] ";
            if (splitted[0].equals("!say")) {
                type = "[" + player.getName() + "] ";
            }
            try {
                ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, type + StringUtil.joinStringFrom(splitted, 1)).getBytes());
            } catch (RemoteException e) {
                cserv.reconnectWorld();
            }
        } else if (splitted[0].equals("!openshop")) {
            MapleShopFactory.getInstance().getShop(Integer.parseInt(splitted[1])).sendShop(c);
        } else if (splitted[0].equals("!pos")) {
            mc.dropMessage("Your Pos: x = " + player.getPosition().x + ", y = " + player.getPosition().y + ", fh = " + player.getMap().getFootholds().findBelow(player.getPosition()).getId());
        } else if (splitted[0].equals("!resetcooldowns")) {
            for (PlayerCoolDownValueHolder i : player.getAllCooldowns()) {
                player.removeCooldown(i.skillId);
            }
            mc.dropMessage("Success.");
        } else if (splitted[0].equals("!saveall")) {
            for (ChannelServer chan : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                    chr.saveToDB(true);
                }
            }
            mc.dropMessage("Done.");
        } else if (splitted[0].equals("!servermessage")) {
            for (int i = 1; i <= ChannelServer.getAllInstances().size(); i++) {
                ChannelServer.getInstance(i).setServerMessage(StringUtil.joinStringFrom(splitted, 1));
            }
        } else if (splitted[0].equalsIgnoreCase("!showMonsterID")) {
            MapleMap map = player.getMap();
            double range = Double.POSITIVE_INFINITY;
            List<MapleMapObject> monsters = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                mc.dropMessage("name=" + monster.getName() + " ID=" + monster.getId() + " isAlive=" + monster.isAlive());
            }
        } else if (splitted[0].equals("!skill")) {
            player.maxSkillLevel(Integer.parseInt(splitted[1]));
        } else if (splitted[0].equals("!slap")) {
            int loss = Integer.parseInt(splitted[2]);
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setHp(victim.getHp() - loss);
            victim.updateSingleStat(MapleStat.HP, victim.getHp() - loss);
            mc.dropMessage("You slapped " + victim.getName() + ".");
        } else if (splitted[0].equals("!spawn")) {
            for (int i = 0; i < Math.min(getOptionalIntArg(splitted, 2, 1), 500); i++) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(splitted[1])), player.getPosition());
            }
        } else if (splitted[0].equals("!unjail")) {
            MapleMap target = cserv.getMapFactory().getMap(100000000);
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeMap(target, target.getPortal(0));
        } else if (splitted[0].equals("!warpallhere")) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != player.getMapId()) {
                    mch.changeMap(player.getMap(), player.getPosition());
                }
            }
        } else if (splitted[0].equals("!warp")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted.length == 2) {
                    MapleMap target = victim.getMap();
                    player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    victim.changeMap(target, target.getPortal(0));
                }
            } else {
                try {
                    victim = player;
                    WorldLocation loc = cserv.getWorldInterface().getLocation(splitted[1]);
                    if (loc != null) {
                        mc.dropMessage("You will be cross-channel warped. This may take a few seconds.");
                        MapleMap target = cserv.getMapFactory().getMap(loc.map);
                        victim.cancelAllBuffs();
                        String ip = cserv.getIP(loc.channel);
                        victim.getMap().removePlayer(victim);
                        victim.setMap(target);
                        String[] socket = ip.split(":");
                        if (victim.getTrade() != null) {
                            MapleTrade.cancelTrade(player);
                        }
                        victim.saveToDB(true);
                        if (victim.getCheatTracker() != null) {
                            victim.getCheatTracker().dispose();
                        }
                        ChannelServer.getInstance(c.getChannel()).removePlayer(player);
                        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                            c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        MapleMap target = cserv.getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        player.changeMap(target, target.getPortal(0));
                    }
                } catch (Exception e) {
                }
            }
        } else if (splitted[0].equals("!warpmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[1]));
                    mch.changeMap(target, target.getPortal(0));
                }
            }
        } else if (splitted[0].equals("!warphere")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeMap(player.getMap(), player.getMap().findClosestSpawnpoint(player.getPosition()));
        } else if (splitted[0].equals("!whereami")) {
            mc.dropMessage("You are on map " + player.getMap().getId());
        } else if (splitted[0].equals("!whosthere")) {
            StringBuilder builder = new StringBuilder("Players on Map: ");
            for (MapleCharacter chr : player.getMap().getCharacters()) {
                if (builder.length() > 150) {
                    builder.setLength(builder.length() - 2);
                    mc.dropMessage(builder.toString());
                }
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()) + ", ");
            }
            builder.setLength(builder.length() - 2);
            c.getSession().write(MaplePacketCreator.serverNotice(6, builder.toString()));
        } else if (splitted[0].equals("!exprate")) {//RATE COMMANDS
            int exp = Integer.parseInt(splitted[1]);
            cserv.setExpRate(exp);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Exp Rate has been changed to " + exp + "x."));
        } else if (splitted[0].equals("!petexprate")) {
            int exp = Integer.parseInt(splitted[1]);
            cserv.setPetExpRate(exp);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Pet Exp Rate has been changed to " + exp + "x."));
        } else if (splitted[0].equals("!mountexprate")) {
            int exp = Integer.parseInt(splitted[1]);
            cserv.setMountRate(exp);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Mount Exp Rate has been changed to " + exp + "x."));
        } else if (splitted[0].equals("!mesorate")) {
            int meso = Integer.parseInt(splitted[1]);
            cserv.setMesoRate(meso);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Meso Rate has been changed to " + meso + "x."));
        } else if (splitted[0].equals("!droprate")) {
            int drop = Integer.parseInt(splitted[1]);
            cserv.setDropRate(drop);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Drop Rate has been changed to " + drop + "x."));
        } else if (splitted[0].equals("!bossdroprate")) {
            int bossdrop = Integer.parseInt(splitted[1]);
            cserv.setBossDropRate(bossdrop);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Boss Drop Rate has been changed to " + bossdrop + "x."));
        } else if (splitted[0].equals("!shopmesorate")) {
            int rate = Integer.parseInt(splitted[1]);
            cserv.setShopMesoRate(rate);
            ChannelServer.getInstance(c.getChannel()).broadcastPacket(MaplePacketCreator.serverNotice(6, "Shop Meso Rate has been changed to " + rate + "x."));
        } else if (splitted[0].equals("!anego")) {//MONSTER COMMANDS
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400121), player.getPosition());
        } else if (splitted[0].equals("!balrog")) {
            int[] ids = {8130100, 8150000, 9400536};
            for (int a : ids) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
            }
        } else if (splitted[0].equals("!bird")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300090), player.getPosition());
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300089), player.getPosition());
        } else if (splitted[0].equals("!blackcrow")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400014), player.getPosition());
        } else if (splitted[0].equals("!bob")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400551), player.getPosition());
        } else if (splitted[0].equals("!centipede")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9500177), player.getPosition());
        } else if (splitted[0].equals("!clone")) {
            int[] ids = {9001002, 9001000, 9001003, 9001001};
            for (int a : ids) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
            }
        } else if (splitted[0].equals("!coke")) {
            int[] ids = {9500144, 9500151, 9500152, 9500153, 9500154, 9500143, 9500145, 9500149, 9500147};
            for (int a : ids) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
            }
        } else if (splitted[0].equals("!ergoth")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300028), player.getPosition());
        } else if (splitted[0].equals("!franken")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300139), player.getPosition());
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300140), player.getPosition());
        } else if (splitted[0].equals("!horseman")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400549), player.getPosition());
        } else if (splitted[0].equals("!leafreboss")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8180000), player.getPosition());
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8180001), player.getPosition());
        } else if (splitted[0].equals("!loki")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400567), player.getPosition());
        } else if (splitted[0].equals("!ludimini")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8160000), player.getPosition());
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8170000), player.getPosition());
        } else if (splitted[0].equals("!mushmom")) {
            int[] ids = {6130101, 6300005, 9400205};
            for (int a : ids) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
            }
        } else if (splitted[0].equals("!nx")) {
            for (int x = 0; x < 10; x++) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400202), player.getPosition());
            }
        } else if (splitted[0].equals("!pap")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
        } else if (splitted[0].equals("!papapixie")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300039), player.getPosition());
        } else if (splitted[0].equals("!pianus")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
        } else if (splitted[0].equals("!pirate")) {
            int[] ids = {9300119, 9300107, 9300105, 9300106};
            for (int a : ids) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
            }
        } else if (splitted[0].equals("!snackbar")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9500179), player.getPosition());
        } else if (splitted[0].equals("!theboss")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400300), player.getPosition());
        } else if (splitted[0].equals("!str") || splitted[0].equals("!dex") || splitted[0].equals("!int") || splitted[0].equals("!luk")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);//STAT COMMANDS
            int up = Integer.parseInt(splitted[2]);
            if (splitted[0].equals("!str")) {
                victim.setStr(up);
                victim.updateSingleStat(MapleStat.STR, victim.getStr());
            } else if (splitted[0].equals("!dex")) {
                victim.setDex(up);
                victim.updateSingleStat(MapleStat.DEX, victim.getDex());
            } else if (splitted[0].equals("!luk")) {
                victim.setLuk(up);
                victim.updateSingleStat(MapleStat.LUK, victim.getLuk());
            } else {
                victim.setInt(up);
                victim.updateSingleStat(MapleStat.INT, victim.getInt());
            }
        } else if (splitted[0].equals("!ap")) {
            player.setRemainingAp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
        } else if (splitted[0].equals("!sp")) {
            player.setRemainingSp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
        } else if (splitted[0].equals("!allocate")) {
            int up = Integer.parseInt(splitted[2]);
            if (splitted[1].equals("str")) {
                player.setStr(player.getStr() + up);
                player.updateSingleStat(MapleStat.STR, player.getStr());
            } else if (splitted[1].equals("dex")) {
                player.setDex(player.getDex() + up);
                player.updateSingleStat(MapleStat.DEX, player.getDex());
            } else if (splitted[1].equals("int")) {
                player.setInt(player.getInt() + up);
                player.updateSingleStat(MapleStat.INT, player.getInt());
            } else if (splitted[1].equals("luk")) {
                player.setLuk(player.getLuk() + up);
                player.updateSingleStat(MapleStat.LUK, player.getLuk());
            } else if (splitted[1].equals("hp")) {
                player.setMaxHp(player.getMaxHp() + up);
                player.updateSingleStat(MapleStat.MAXHP, player.getMaxHp());
            } else if (splitted[1].equals("mp")) {
                player.setMaxMp(player.getMaxMp() + up);
                player.updateSingleStat(MapleStat.MAXMP, player.getMaxMp());
            } else {
                mc.dropMessage(splitted[1] + " is not a valid stat.");
            }
        } else if (splitted[0].equals("!exp")) {
            int exp = Integer.parseInt(splitted[1]);
            player.setExp(exp);
            player.updateSingleStat(MapleStat.EXP, exp);
        } else if (splitted[0].equals("!level")) {
            player.setLevel(Integer.parseInt(splitted[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
        } else if (splitted[0].equals("!maxall")) {
            player.setStr(32767);
            player.setDex(32767);
            player.setInt(32767);
            player.setLuk(32767);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.STR, 32767);
            player.updateSingleStat(MapleStat.DEX, 32767);
            player.updateSingleStat(MapleStat.INT, 32767);
            player.updateSingleStat(MapleStat.LUK, 32767);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (splitted[0].equals("!setall")) {
            int x = Integer.parseInt(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, player.getStr());
            player.updateSingleStat(MapleStat.DEX, player.getStr());
            player.updateSingleStat(MapleStat.INT, player.getStr());
            player.updateSingleStat(MapleStat.LUK, player.getStr());
        } else {
            if (player.gmLevel() == 3) {
                mc.dropMessage("GM Command " + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }

    public static int getOptionalIntArg(String splitted[], int position, int def) {
        if (splitted.length > position) {
            try {
                return Integer.parseInt(splitted[position]);
            } catch (NumberFormatException nfe) {
                return def;
            }
        }
        return def;
    }
}