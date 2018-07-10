package net.sf.odinms.server.life;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.tools.Pair;

/**
 *
 * @author Danny (Leifde)
 */

public class MobSkillFactory {
	private static Map<Pair<Integer, Integer>, MobSkill> mobSkills = new HashMap<Pair<Integer, Integer>, MobSkill>();
	private static MapleDataProvider dataSource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Skill.wz"));
	private static MapleData skillRoot = dataSource.getData("MobSkill.img");
	
	public static MobSkill getMobSkill(int skillId, int level) {
		MobSkill ret = mobSkills.get(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)));
		if (ret != null) {
			return ret;
		}
		synchronized (mobSkills) {
			ret = mobSkills.get(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)));
			if (ret == null) {
				MapleData skillData = skillRoot.getChildByPath(skillId + "/level/" + level);
				if (skillData != null) {
					int mpCon = MapleDataTool.getInt(skillData.getChildByPath("mpCon"), 0);
					List<Integer> toSummon = new ArrayList<Integer>();
					for (int i = 0; i > -1; i++) {
						if (skillData.getChildByPath(String.valueOf(i)) == null) {
							break;
						}
						toSummon.add(Integer.valueOf(MapleDataTool.getInt(skillData.getChildByPath(String.valueOf(i)), 0)));
					}
					int effect = MapleDataTool.getInt("summonEffect", skillData, 0);
					int hp = MapleDataTool.getInt("hp", skillData, 100);
					int x = MapleDataTool.getInt("x", skillData, 1);
					int y = MapleDataTool.getInt("y", skillData, 1);
					long duration = MapleDataTool.getInt("time", skillData, 0) * 1000;
					long cooltime = MapleDataTool.getInt("interval", skillData, 0) * 1000;
					int iprop = MapleDataTool.getInt("prop", skillData, 100);
					float prop = iprop / 100;
					int limit = MapleDataTool.getInt("limit", skillData, 0);
					MapleData ltd = skillData.getChildByPath("lt");
					Point lt = null;
					Point rb = null;
					if (ltd != null) {
						lt = (Point) ltd.getData();
						rb = (Point) skillData.getChildByPath("rb").getData();
					}
					ret = new MobSkill(skillId, level);
					ret.addSummons(toSummon);
					ret.setCoolTime(cooltime);
					ret.setDuration(duration);
					ret.setHp(hp);
					ret.setMpCon(mpCon);
					ret.setSpawnEffect(effect);
					ret.setX(x);
					ret.setY(y);
					ret.setProp(prop);
					ret.setLimit(limit);
					ret.setLtRb(lt, rb);
				}
				mobSkills.put(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)), ret);
			}
			return ret;
		}
	}
}
