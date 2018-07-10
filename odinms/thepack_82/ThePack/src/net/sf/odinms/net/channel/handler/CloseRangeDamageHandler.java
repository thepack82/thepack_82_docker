package net.sf.odinms.net.channel.handler;

import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacter.CancelCooldownAction;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.server.maps.FakeCharacter;

public class CloseRangeDamageHandler extends AbstractDealDamageHandler {

    private boolean isFinisher(int skillId) {
        return skillId >= 1111003 && skillId <= 1111006;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        AttackInfo attack = parseDamage(slea, false);
        MapleCharacter player = c.getPlayer();
        MaplePacket packet = MaplePacketCreator.closeRangeAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.speed);
        player.getMap().broadcastMessage(player, packet, false, true);
        int numFinisherOrbs = 0;
        Integer comboBuff = player.getBuffedValue(MapleBuffStat.COMBO);
        ISkill energycharge = SkillFactory.getSkill(5110001);
        int energyChargeSkillLevel = player.getSkillLevel(energycharge);
        if (isFinisher(attack.skill)) {
            if (comboBuff != null) {
                numFinisherOrbs = comboBuff.intValue() - 1;
            }
            player.handleOrbconsume();
        } else if (attack.numAttacked > 0) {
            if ((player.getJob().equals(MapleJob.CRUSADER) || player.getJob().equals(MapleJob.HERO)) || ChannelServer.getInstance(c.getChannel()).getOrbGain()) {
                if (attack.skill != 1111008 && comboBuff != null) { // shout should not give orbs
                    player.handleOrbgain();
                }
            }
            if (energyChargeSkillLevel > 0 && player.getJob().equals(MapleJob.BUCCANEER) || player.getJob().equals(MapleJob.MARAUDER)) {
                for (int i = 0; i < attack.numAttacked; i++) {
                    player.handleEnergyChargeGain();
                }
            }
        }
        // handle sacrifice hp loss
        if (attack.numAttacked > 0 && attack.skill == 1311005) {
            int totDamageToOneMonster = attack.allDamage.get(0).getRight().get(0).intValue(); // sacrifice attacks only 1 mob with 1 attack
            int remainingHP = player.getHp() - totDamageToOneMonster * attack.getAttackEffect(player).getX() / 100;
            if (remainingHP > 1) {
                player.setHp(remainingHP);
            } else {
                player.setHp(1);
            }
            player.updateSingleStat(MapleStat.HP, player.getHp());
            player.checkBerserk();
        }
        // handle charged blow
        if (attack.numAttacked > 0 && attack.skill == 1211002) {
            boolean advcharge_prob = false;
            int advcharge_level = player.getSkillLevel(SkillFactory.getSkill(1220010));
            if (advcharge_level > 0) {
                MapleStatEffect advcharge_effect = SkillFactory.getSkill(1220010).getEffect(advcharge_level);
                advcharge_prob = advcharge_effect.makeChanceResult();
            } else {
                advcharge_prob = false;
            }
            if (!advcharge_prob) {
                player.cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
            }
        }
        int maxdamage = c.getPlayer().getCurrentMaxBaseDamage();
        int attackCount = 1;
        if (attack.skill != 0) {
            MapleStatEffect effect = attack.getAttackEffect(c.getPlayer());
            attackCount = effect.getAttackCount();
            maxdamage *= effect.getDamage() / 100.0;
            maxdamage *= attackCount;
        }
        maxdamage = Math.min(maxdamage, 99999);
        if (attack.skill == 4211006) {
            maxdamage = 700000;
        } else if (numFinisherOrbs > 0) {
            maxdamage *= numFinisherOrbs;
        } else if (comboBuff != null) {
            ISkill combo = SkillFactory.getSkill(1111002);
            int comboLevel = player.getSkillLevel(combo);
            MapleStatEffect comboEffect = combo.getEffect(comboLevel);
            double comboMod = 1.0 + (comboEffect.getDamage() / 100.0 - 1.0) * (comboBuff.intValue() - 1);
            maxdamage *= comboMod;
        }
        if (numFinisherOrbs == 0 && isFinisher(attack.skill)) {
            return; // can only happen when lagging o.o
        }
        if (isFinisher(attack.skill)) {
            maxdamage = 99999; // FIXME reenable damage calculation for finishers
        }
        if (attack.skill > 0) {
            ISkill skill = SkillFactory.getSkill(attack.skill);
            int skillLevel = c.getPlayer().getSkillLevel(skill);
            MapleStatEffect effect_ = skill.getEffect(skillLevel);
            if (effect_.getCooldown() > 0) {
                if (player.skillisCooling(attack.skill)) {
                    player.getCheatTracker().registerOffense(CheatingOffense.COOLDOWN_HACK);
                    return;
                } else {
                    c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
                    ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), attack.skill), effect_.getCooldown() * 1000);
                    player.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, timer);
                }
            }
        }
        applyAttack(attack, player, maxdamage, attackCount);
        if (c.getPlayer().hasFakeChar()) {
            for (FakeCharacter ch : c.getPlayer().getFakeChars()) {
                MaplePacket packett = MaplePacketCreator.closeRangeAttack(ch.getFakeChar().getId(), attack.skill, attack.stance,
                        attack.numAttackedAndDamage, attack.allDamage, attack.speed);
                player.getMap().broadcastMessage(ch.getFakeChar(), packett, false, true);
                applyAttack(attack, ch.getFakeChar(), maxdamage, attackCount);
            }
        }
    }
}