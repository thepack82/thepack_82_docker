package net.sf.odinms.net.channel.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacter.CancelCooldownAction;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class MagicDamageHandler extends AbstractDealDamageHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		AttackInfo attack = parseDamage(slea, false);
		MapleCharacter player = c.getPlayer();
		MaplePacket packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, -1, attack.speed);
		if (attack.skill == 2121001 || attack.skill == 2221001 || attack.skill == 2321001) {
			packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.charge, attack.speed);
		}
		player.getMap().broadcastMessage(player, packet, false, true);
		MapleStatEffect effect = attack.getAttackEffect(c.getPlayer());
		int maxdamage;
		maxdamage = 99999;
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
		applyAttack(attack, player, maxdamage, effect.getAttackCount());
		// MP Eater
		for (int i = 1; i <= 3; i++) {
			ISkill eaterSkill = SkillFactory.getSkill(2000000 + i * 100000);
			int eaterLevel = player.getSkillLevel(eaterSkill);
			if (eaterLevel > 0) {
				for (Pair<Integer, List<Integer>> singleDamage : attack.allDamage) {
					eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage.getLeft()), 0);
				}
				break;
			}
		}
	}
}