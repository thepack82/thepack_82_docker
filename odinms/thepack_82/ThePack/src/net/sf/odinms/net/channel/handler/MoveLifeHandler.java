package net.sf.odinms.net.channel.handler;

import java.awt.Point;
import java.util.List;
import java.util.Random;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.movement.LifeMovementFragment;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLifeHandler extends AbstractMovementPacketHandler {
	private static Logger log = LoggerFactory.getLogger(MoveLifeHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int objectid = slea.readInt();
		short moveid = slea.readShort();
		MapleMapObject mmo = c.getPlayer().getMap().getMapObject(objectid);
		if (mmo == null || mmo.getType() != MapleMapObjectType.MONSTER) {
			return;
		}
		MapleMonster monster = (MapleMonster) mmo;
		List<LifeMovementFragment> res = null;
		int skillByte = slea.readByte();
		int skill = slea.readByte();
		int skill_1 = slea.readByte() & 0xFF;
		int skill_2 = slea.readByte();
		int skill_3 = slea.readByte();
		@SuppressWarnings("unused")
		int skill_4 = slea.readByte();
		MobSkill toUse = null;
		Random rand = new Random();
		if (skillByte == 1 && monster.getNoSkills() > 0) {
			int random = rand.nextInt(monster.getNoSkills());
			Pair<Integer, Integer> skillToUse = monster.getSkills().get(random);
			toUse = MobSkillFactory.getMobSkill(skillToUse.getLeft(), skillToUse.getRight());
			int percHpLeft = (int) ((monster.getHp() / monster.getMaxHp()) * 100);
			if (toUse.getHP() < percHpLeft || !monster.canUseSkill(toUse)) {
				toUse = null;
			}
		}
		if ((skill_1 >= 100 && skill_1 <= 200) && monster.hasSkill(skill_1, skill_2)) {
			MobSkill skillData = MobSkillFactory.getMobSkill(skill_1, skill_2);
			if (skillData != null && monster.canUseSkill(skillData)) {
				skillData.applyEffect(c.getPlayer(), monster, true);
			}
		}
		slea.readByte();
		slea.readInt(); // whatever
		int start_x = slea.readShort(); // hmm.. startpos?
		int start_y = slea.readShort(); // hmm...
		Point startPos = new Point(start_x, start_y);
		res = parseMovement(slea);
		if (monster.getController() != c.getPlayer()) {
			if (monster.isAttackedBy(c.getPlayer())) { // aggro and controller change
				monster.switchController(c.getPlayer(), true);
			} else {
				return;
			}
		} else {
			if (skill == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
				monster.setControllerHasAggro(false);
				monster.setControllerKnowsAboutAggro(false);
			}
		}
		boolean aggro = monster.isControllerHasAggro();
		if (toUse != null) {
			c.getSession().write(MaplePacketCreator.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro, toUse.getSkillId(), toUse.getSkillLevel()));
		} else {
			c.getSession().write(MaplePacketCreator.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro));
		}
		if (aggro) {
			monster.setControllerKnowsAboutAggro(true);
		}
		if (res != null) {
			if (slea.available() != 9) {
				log.warn("movement parsing error");
				return;
			}
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.moveMonster(skillByte, skill, skill_1, skill_2, skill_3, objectid, startPos, res), monster.getPosition());
			updatePosition (res, monster, -1);
			c.getPlayer().getMap().moveMonster(monster, monster.getPosition());
			c.getPlayer().getCheatTracker().checkMoveMonster(monster.getPosition());
		}
	}
}
