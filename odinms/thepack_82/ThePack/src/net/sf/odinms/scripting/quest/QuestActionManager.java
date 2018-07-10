package net.sf.odinms.scripting.quest;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.scripting.npc.NPCConversationManager;
import net.sf.odinms.server.quest.MapleQuest;

/**
 *
 * @author RMZero213
 */

public class QuestActionManager extends NPCConversationManager {

	private boolean start; // this is if the script in question is start or end
	private int quest;

	public QuestActionManager(MapleClient c, int npc, int quest, boolean start) {
		super(c, npc, null);
		this.quest = quest;
		this.start = start;
	}

	public int getQuest() {
		return quest;
	}

	public boolean isStart() {
		return start;
	}

	@Override
	public void dispose() {
		QuestScriptManager.getInstance().dispose(this, getClient());
	}

	public void forceStartQuest() {
		forceStartQuest(quest);
	}

	public void forceStartQuest(int id) {
		MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc());
	}

	public void forceCompleteQuest() {
		forceCompleteQuest(quest);
	}

	public void forceCompleteQuest(int id) {
		MapleQuest.getInstance(id).forceComplete(getPlayer(), getNpc());
	}
}