package net.sf.odinms.client;

import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.life.Element;

public interface ISkill {
	int getId();
	MapleStatEffect getEffect(int level);
	int getMaxLevel();
	int getAnimationTime();
	public boolean canBeLearnedBy (MapleJob job);
	public boolean isFourthJob();
	public Element getElement();
	public boolean isBeginnerSkill();
}
