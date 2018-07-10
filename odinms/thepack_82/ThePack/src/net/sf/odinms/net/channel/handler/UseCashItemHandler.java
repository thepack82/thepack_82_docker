package net.sf.odinms.net.channel.handler;

import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.sf.odinms.client.ExpTable;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.world.remote.WorldLocation;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMist;
import net.sf.odinms.server.maps.MapleTVEffect;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class UseCashItemHandler extends AbstractMaplePacketHandler {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UseCashItemHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte mode = slea.readByte();
        slea.readByte();
        int itemId = slea.readInt();
        int itemType = itemId / 10000;
        try {
            if (itemType == 505) { // AP/SP reset
                if (itemId > 5050000) {
                    int SPTo = slea.readInt();
                    int SPFrom = slea.readInt();
                    ISkill skillSPTo = SkillFactory.getSkill(SPTo);
                    ISkill skillSPFrom = SkillFactory.getSkill(SPFrom);
                    int maxlevel = skillSPTo.getMaxLevel();
                    int curLevel = player.getSkillLevel(skillSPTo);
                    int curLevelSPFrom = player.getSkillLevel(skillSPFrom);
                    if ((curLevel + 1 <= maxlevel) && curLevelSPFrom > 0) {
                        player.changeSkillLevel(skillSPFrom, curLevelSPFrom - 1, player.getMasterLevel(skillSPFrom));
                        player.changeSkillLevel(skillSPTo, curLevel + 1, player.getMasterLevel(skillSPTo));
                    }
                } else {
                    List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
                    int APTo = slea.readInt();
                    int APFrom = slea.readInt();
                    switch (APFrom) {
                        case 64: // str
                            if (c.getPlayer().getStr() <= 4) {
                                return;
                            }
                            c.getPlayer().setStr(c.getPlayer().getStr() - 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, c.getPlayer().getStr()));
                            break;
                        case 128: // dex
                            if (c.getPlayer().getDex() <= 4) {
                                return;
                            }
                            c.getPlayer().setDex(c.getPlayer().getDex() - 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, c.getPlayer().getDex()));
                            break;
                        case 256: // int
                            if (c.getPlayer().getInt() <= 4) {
                                return;
                            }
                            c.getPlayer().setInt(c.getPlayer().getInt() - 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, c.getPlayer().getInt()));
                            break;
                        case 512: // luk
                            if (c.getPlayer().getLuk() <= 4) {
                                return;
                            }
                            c.getPlayer().setLuk(c.getPlayer().getLuk() - 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, c.getPlayer().getLuk()));
                            break;
                        case 2048: // HP
                            if (c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() == 10000) {
                                return;
                            }
                            int maxhp = c.getPlayer().getMaxHp();
                            if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
                                maxhp -= 12;
                            } else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
                                ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
                                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                                maxhp -= 24;
                                if (improvingMaxHPLevel >= 1) {
                                    maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                                }
                            } else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
                                maxhp -= 10;
                            } else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
                                maxhp -= 20;
                            } else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
                                maxhp -= 20;
                            } else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
                                ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
                                int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                                maxhp -= 20;
                                if (improvingMaxHPLevel >= 1) {
                                    maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                                }
                            }
                            if (maxhp < ((c.getPlayer().getLevel() * 2) + 148)) {
                                return;
                            }
                            c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() - 1);
                            c.getPlayer().setHp(maxhp);
                            c.getPlayer().setMaxHp(maxhp);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, c.getPlayer().getMaxHp()));
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, c.getPlayer().getMaxHp()));
                            break;
                        case 8192: // MP
                            if (c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getMpApUsed() == 10000) {
                                return;
                            }
                            int maxmp = c.getPlayer().getMaxMp();
                            if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
                                maxmp -= 8;
                            } else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
                                maxmp -= 4;
                            } else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
                                ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
                                int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                                maxmp -= 20;
                                if (improvingMaxMPLevel >= 1) {
                                    maxmp -= improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
                                }
                            } else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
                                maxmp -= 12;
                            } else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
                                maxmp -= 12;
                            } else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
                                maxmp -= 16;
                            }
                            if (maxmp < ((c.getPlayer().getLevel() * 2) + 148)) {
                                return;
                            }
                            c.getPlayer().setMpApUsed(c.getPlayer().getMpApUsed() - 1);
                            c.getPlayer().setMp(maxmp);
                            c.getPlayer().setMaxMp(maxmp);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, c.getPlayer().getMaxMp()));
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, c.getPlayer().getMaxMp()));
                            break;
                        default:
                            c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                            return;
                    }
                    switch (APTo) {
                        case 64: // str
                            if (c.getPlayer().getStr() >= 999) {
                                return;
                            }
                            c.getPlayer().setStr(c.getPlayer().getStr() + 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, c.getPlayer().getStr()));
                            break;
                        case 128: // dex
                            if (c.getPlayer().getDex() >= 999) {
                                return;
                            }
                            c.getPlayer().setDex(c.getPlayer().getDex() + 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, c.getPlayer().getDex()));
                            break;
                        case 256: // int
                            if (c.getPlayer().getInt() >= 999) {
                                return;
                            }
                            c.getPlayer().setInt(c.getPlayer().getInt() + 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, c.getPlayer().getInt()));
                            break;
                        case 512: // luk
                            if (c.getPlayer().getLuk() >= 999) {
                                return;
                            }
                            c.getPlayer().setLuk(c.getPlayer().getLuk() + 1);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, c.getPlayer().getLuk()));
                            break;
                        case 2048: // hp
                            int maxhp = c.getPlayer().getMaxHp();
                            if (maxhp >= 30000) {
                                c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                                return;
                            } else {
                                if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
                                    maxhp += rand(8, 12);
                                } else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
                                    ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
                                    int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                                    maxhp += rand(20, 25);
                                    if (improvingMaxHPLevel >= 1) {
                                        maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                                    }
                                } else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
                                    maxhp += rand(10, 20);
                                } else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
                                    maxhp += rand(16, 20);
                                } else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
                                    maxhp += rand(16, 20);
                                } else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
                                    ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
                                    int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
                                    maxhp += 20;
                                    if (improvingMaxHPLevel >= 1) {
                                        maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                                    }
                                }
                                maxhp = Math.min(30000, maxhp);
                                c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() + 1);
                                c.getPlayer().setMaxHp(maxhp);
                                statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, c.getPlayer().getMaxHp()));
                                break;
                            }
                        case 8192: // mp
                            int maxmp = c.getPlayer().getMaxMp();
                            if (maxmp >= 30000) {
                                return;
                            } else {
                                if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
                                    maxmp += rand(6, 8);
                                } else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
                                    maxmp += rand(2, 4);
                                } else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
                                    ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
                                    int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
                                    maxmp += rand(18, 20);
                                    if (improvingMaxMPLevel >= 1) {
                                        maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
                                    }
                                } else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
                                    maxmp += rand(10, 12);
                                } else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
                                    maxmp += rand(10, 12);
                                } else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
                                    maxmp += rand(10, 12);
                                }
                                maxmp = Math.min(30000, maxmp);
                                c.getPlayer().setMpApUsed(c.getPlayer().getMpApUsed() + 1);
                                c.getPlayer().setMaxMp(maxmp);
                                statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, c.getPlayer().getMaxMp()));
                                break;
                            }
                        default:
                            c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                            return;
                    }
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true));
                }
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 507) {
                switch (itemId / 1000 % 10) {
                    case 1: // Megaphone
                        if (player.getLevel() > 9) {
                            player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(2, player.getName() + " : " + slea.readMapleAsciiString()));
                        } else {
                            player.dropMessage("You may not use this until you're level 10");
                        }
                        break;
                    case 2: // Super megaphone
                        c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(3, c.getChannel(), player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)).getBytes());
                        break;
                    case 3: // Heart megaphone
                    case 4: // Skull megaphone
                        break;
                    case 5: // Maple TV
                        int tvType = itemId % 10;
                        boolean megassenger = false;
                        boolean ear = false;
                        MapleCharacter victim = null;
                        if (tvType != 1) { // 1 is the odd one out since it doesnt allow 2 players.
                            if (tvType >= 3) {
                                megassenger = true;
                                if (tvType == 3) {
                                    slea.readByte();
                                }
                                ear = 1 == slea.readByte();
                            } else if (tvType != 2) {
                                slea.readByte();
                            }
                            if (tvType != 4) {
                                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                            }
                        }
                        List<String> messages = new LinkedList<String>();
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < 5; i++) {
                            String message = slea.readMapleAsciiString();
                            if (megassenger) {
                                builder.append(" ");
                                builder.append(message); // builder.append(" "+message);
                            }
                            messages.add(message);
                        }
                        slea.readInt(); // some random shit
                        if (megassenger) {
                            c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(3, c.getChannel(), player.getName() + " : " + builder.toString(), ear).getBytes());
                        }
                        if (!MapleTVEffect.isActive()) {
                            new MapleTVEffect(player, victim, messages, tvType);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
                        } else {
                            player.dropMessage("MapleTV is already in use");
                            return;
                        }
                        break;
                }
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 509) {
                String sendTo = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                try {
                    c.getPlayer().sendNote(sendTo, msg);
                } catch (SQLException e) {
                    log.error("SAVING NOTE", e);
                }
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 510) {
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 512) {
                c.getPlayer().getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", c.getPlayer().getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 517) {
                MaplePet pet = c.getPlayer().getPet(0);
                if (pet == null) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                String newName = slea.readMapleAsciiString();
                pet.setName(newName);
                c.getSession().write(MaplePacketCreator.updatePet(pet, true));
                c.getSession().write(MaplePacketCreator.enableActions());
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.changePetName(c.getPlayer(), newName, 1), true);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 504) { // vip teleport rock
                byte rocktype = slea.readByte();
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
                if (rocktype == 0x00) {
                    int mapId = slea.readInt();
                    MapleMap target = c.getChannelServer().getMapFactory().getMap(mapId);
                    MaplePortal targetPortal = target.getPortal(0);
                    if (target.getForcedReturnId() == 999999999) {//Makes sure this map doesn't have a forced return map
                        c.getPlayer().changeMap(target, targetPortal);
                    } else {
                        MapleInventoryManipulator.addById(c, itemId, (short) 1, "Teleport Rock Error (Not found)");
                        new ServernoticeMapleClientMessageCallback(1, c).dropMessage("Either the player could not be found or you were trying to teleport to an illegal location.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                } else {
                    String name = slea.readMapleAsciiString();
                    MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                    if (victim != null) {
                        MapleMap target = victim.getMap();
                        WorldLocation loc = c.getChannelServer().getWorldInterface().getLocation(name);
                        if (c.getChannelServer().getMapFactory().getMap(loc.map).getForcedReturnId() == 999999999 || c.getChannelServer().getMapFactory().getMap(loc.map).getId() < 100000000) {//This doesn't allow tele to GM map, zakum and etc...
                            if (!victim.isHidden()) {
                                c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                            } else {
                                MapleInventoryManipulator.addById(c, itemId, (short) 1, "Teleport Rock Error (Not found)");
                                new ServernoticeMapleClientMessageCallback(1, c).dropMessage("Either the player could not be found or you were trying to teleport to an illegal location.");
                                c.getSession().write(MaplePacketCreator.enableActions());
                            }
                        } else {
                            MapleInventoryManipulator.addById(c, itemId, (short) 1, "Teleport Rock Error (Can't Teleport)");
                            new ServernoticeMapleClientMessageCallback(1, c).dropMessage("You cannot teleport to this map.");
                            c.getSession().write(MaplePacketCreator.enableActions());
                        }
                    } else {
                        MapleInventoryManipulator.addById(c, itemId, (short) 1, "Teleport Rock Error (Not found)");
                        new ServernoticeMapleClientMessageCallback(1, c).dropMessage("Player could not be found.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                }
            } else if (itemType == 520) {
                c.getPlayer().gainMeso(ii.getMeso(itemId), true, false, true);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
                c.getSession().write(MaplePacketCreator.enableActions());
            } else if (itemType == 524) {
                MaplePet pet = c.getPlayer().getPet(0);
                if (pet == null) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (!pet.canConsume(itemId)) {
                    pet = c.getPlayer().getPet(1);
                    if (pet != null) {
                        if (!pet.canConsume(itemId)) {
                            pet = c.getPlayer().getPet(2);
                            if (pet != null) {
                                if (!pet.canConsume(itemId)) {
                                    c.getSession().write(MaplePacketCreator.enableActions());
                                    return;
                                }
                            } else {
                                c.getSession().write(MaplePacketCreator.enableActions());
                                return;
                            }
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                }
                pet.setFullness(100);
                int closeGain = 100 * c.getChannelServer().getPetExpRate();
                if (pet.getCloseness() < 30000) {
                    if (pet.getCloseness() + closeGain > 30000) {
                        pet.setCloseness(30000);
                    } else {
                        pet.setCloseness(pet.getCloseness() + closeGain);
                    }
                    while (pet.getCloseness() >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                        pet.setLevel(pet.getLevel() + 1);
                        c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
                    }
                }
                c.getSession().write(MaplePacketCreator.updatePet(pet, true));
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), (byte) 1, 0, true, true), true);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 528) {
                if (itemId == 5281000) {
                    Rectangle bounds = new Rectangle((int) c.getPlayer().getPosition().getX(), (int) c.getPlayer().getPosition().getY(), 1, 1);
                    MapleStatEffect mse = new MapleStatEffect();
                    mse.setSourceId(2111003);
                    MapleMist mist = new MapleMist(bounds, c.getPlayer(), mse);
                    c.getPlayer().getMap().spawnMist(mist, 10000, false, true);
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), " ", false, 1));
                }
            } else if (itemType == 530) {
                ii.getItemEffect(itemId).applyTo(c.getPlayer());
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            } else if (itemType == 537) {
                String text = slea.readMapleAsciiString();
                c.getPlayer().setChalkboard(text);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.useChalkboard(c.getPlayer(), false));
                c.getPlayer().getClient().getSession().write(MaplePacketCreator.enableActions());
            } else if (itemType == 539) {
                List<String> lines = new LinkedList<String>();
                for (int i = 0; i < 4; i++) {
                    lines.add(slea.readMapleAsciiString());
                }
                c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, lines, (slea.readByte() != 0)).getBytes());
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            }
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
            log.error("UseCashItemHadler Error", e);
        }
    }

    private static int rand(int lbound, int ubound) {
        return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
    }
}