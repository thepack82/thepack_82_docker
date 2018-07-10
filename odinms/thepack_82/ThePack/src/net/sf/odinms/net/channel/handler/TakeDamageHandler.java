package net.sf.odinms.net.channel.handler;

import java.util.Collections;

import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MobAttackInfo;
import net.sf.odinms.server.life.MobAttackInfoFactory;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.MapleMist;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class TakeDamageHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        slea.readInt();
        int damagefrom = slea.readByte();
        slea.readByte();
        int damage = slea.readInt();
        int oid = 0;
        int monsteridfrom = 0;
        int pgmr = 0;
        int direction = 0;
        int pos_x = 0;
        int pos_y = 0;
        int fake = 0;
        boolean is_pgmr = false;
        boolean is_pg = true;
        int mpattack = 0;
        MapleMonster attacker = null;
        if (damagefrom != -2) {
            monsteridfrom = slea.readInt();
            oid = slea.readInt();
            attacker = (MapleMonster) player.getMap().getMapObject(oid);
            direction = slea.readByte();
        }
        if (damagefrom != -1 && damagefrom != -2 && attacker != null) {
            MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, damagefrom);
            if (attackInfo.isDeadlyAttack()) {
                mpattack = player.getMp() - 1;
            }
            mpattack += attackInfo.getMpBurn();
            MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
            if (skill != null && damage > 0) {
                skill.applyEffect(player, attacker, false);
            }
            if (attacker != null) {
                attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
                if (player.getBuffedValue(MapleBuffStat.MANA_REFLECTION) != null && damage > 0 && !attacker.isBoss()) {
                    int[] manaReflectSkillId = {2121002, 2221002, 2321002};
                    for (int manaReflect : manaReflectSkillId) {
                        ISkill manaReflectSkill = SkillFactory.getSkill(manaReflect);
                        if (player.isBuffFrom(MapleBuffStat.MANA_REFLECTION, manaReflectSkill) && player.getSkillLevel(manaReflectSkill) > 0 && manaReflectSkill.getEffect(player.getSkillLevel(manaReflectSkill)).makeChanceResult()) {
                            int bouncedamage = (int) (damage * (manaReflectSkill.getEffect(player.getSkillLevel(manaReflectSkill)).getX() / 100));
                            if (bouncedamage > attacker.getMaxHp() * .2) {
                                bouncedamage = (int) (attacker.getMaxHp() * .2);
                            }
                            player.getMap().damageMonster(player, attacker, bouncedamage);
                            player.getMap().broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), true);
                            player.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(manaReflect, 5));
                            player.getMap().broadcastMessage(player, MaplePacketCreator.showBuffeffect(player.getId(), manaReflect, 5), false);
                            break;
                        }
                    }
                }
            }
        }
        if (damage == -1) {
            int job = (int) (player.getJob().getId() / 10 - 40);
            fake = 4020002 + (job * 100000);
        }
        if (damage < -1 || damage > 60000) {
            AutobanManager.getInstance().addPoints(c, 1000, 60000, "Taking abnormal amounts of damage from " + monsteridfrom + ": " + damage);
            return;
        }
        player.getCheatTracker().checkTakeDamage();
        if (damage > 0) {
            player.getCheatTracker().setAttacksWithoutHit(0);
            player.getCheatTracker().resetHPRegen();
        }
        boolean smokescreen = false;
        for (MapleMapObject mmo : player.getMap().getMapObjects()) {
            if (mmo instanceof MapleMist) {
                MapleMist mist = (MapleMist) mmo;
                if (mist.getSourceSkill().getId() == 4221006) {
                    for (MapleMapObject mmoplayer : player.getMap().getMapObjectsInBox(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                        if (player == (MapleCharacter) mmoplayer) {
                            smokescreen = true;
                        }
                    }
                }
            }
        }
        if (damage > 0 && !player.isHidden() && !smokescreen && !player.isGodMode()) {
            if (attacker != null && !attacker.isBoss()) {
                if (damagefrom == -1 && player.getBuffedValue(MapleBuffStat.POWERGUARD) != null) {
                    int bouncedamage = (int) (damage * (player.getBuffedValue(MapleBuffStat.POWERGUARD).doubleValue() / 100));
                    bouncedamage = Math.min(bouncedamage, attacker.getMaxHp() / 10);
                    player.getMap().damageMonster(player, attacker, bouncedamage);
                    damage -= bouncedamage;
                    player.getMap().broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), true, true);
                    player.checkMonsterAggro(attacker);
                }
            }
            if (damagefrom != -2) {
                Integer achilles = 0;
                ISkill achilles1 = null;
                switch (player.getJob().getId()) {
                    case 112:
                        achilles = player.getSkillLevel(SkillFactory.getSkill(1120004));
                        achilles1 = SkillFactory.getSkill(1120004);
                        break;
                    case 122:
                        achilles = player.getSkillLevel(SkillFactory.getSkill(1220005));
                        achilles1 = SkillFactory.getSkill(1220005);
                        break;
                    case 132:
                        achilles = player.getSkillLevel(SkillFactory.getSkill(1320005));
                        achilles1 = SkillFactory.getSkill(1320005);
                        break;
                }
                if (achilles != 0 && achilles1 != null) {
                    double multiplier = achilles1.getEffect(achilles).getX() / 1000.0;
                    int newdamage = (int) (multiplier * damage);
                    damage = newdamage;
                }
            }
            Integer mesoguard = player.getBuffedValue(MapleBuffStat.MESOGUARD);
            if (player.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null && mpattack == 0) {
                int mploss = (int) (damage * (player.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0));
                int hploss = damage - mploss;
                if (mploss > player.getMp()) {
                    hploss += mploss - player.getMp();
                    mploss = player.getMp();
                }
                player.addMPHP(-hploss, -mploss);
            } else if (mesoguard != null) {
                damage = (damage % 2 == 0) ? damage / 2 : (damage / 2) + 1;
                int mesoloss = (int) (damage * (mesoguard.doubleValue() / 100.0));
                if (player.getMeso() < mesoloss) {
                    player.gainMeso(-player.getMeso(), false);
                    player.cancelBuffStats(MapleBuffStat.MESOGUARD);
                } else {
                    player.gainMeso(-mesoloss, false);
                }
                player.addMPHP(-damage, -mpattack);
            } else {
                player.addMPHP(-damage, -mpattack);
            }
        }
        if (!player.isHidden() && !smokescreen) {
            player.getMap().broadcastMessage(player, MaplePacketCreator.damagePlayer(damagefrom, monsteridfrom, player.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
            player.checkBerserk();
        }
    }
}
