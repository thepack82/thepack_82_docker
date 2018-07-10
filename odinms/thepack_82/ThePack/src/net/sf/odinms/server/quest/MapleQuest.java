/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * MapleQuest.java
 *
 * Created on 10. Dezember 2007, 23:09
 */

package net.sf.odinms.server.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.client.MapleQuestStatus.Status;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.provider.MapleDataTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matze
 */

public class MapleQuest {

	private static Map<Integer,MapleQuest> quests = new HashMap<Integer,MapleQuest>();
	protected int id;
	protected List<MapleQuestRequirement> startReqs;
	protected List<MapleQuestRequirement> completeReqs;
	protected List<MapleQuestAction> startActs;
	protected List<MapleQuestAction> completeActs;
	protected List<Integer> relevantMobs;
	private boolean autoStart;
	private boolean autoPreComplete;
	private boolean repeatable = false;
	private static MapleDataProvider questData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Quest.wz"));
	private static MapleData actions = questData.getData("Act.img");
	private static MapleData requirements = questData.getData("Check.img");
	private static MapleData info = questData.getData("QuestInfo.img");
	
	protected static final Logger log = LoggerFactory.getLogger(MapleQuest.class);
	
	protected MapleQuest() {
		relevantMobs = new LinkedList<Integer>();
	}
	
	/** Creates a new instance of MapleQuest */
	private MapleQuest(int id) {
		this.id = id;
		relevantMobs = new LinkedList<Integer>();
		// read reqs
		MapleData startReqData = requirements.getChildByPath(String.valueOf(id)).getChildByPath("0");
		startReqs = new LinkedList<MapleQuestRequirement>();
		if (startReqData != null) {
			for (MapleData startReq : startReqData.getChildren()) {
				MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(startReq.getName());
				if (type.equals(MapleQuestRequirementType.INTERVAL))
					repeatable = true;
				MapleQuestRequirement req = new MapleQuestRequirement(
					this, type, startReq);
				if (req.getType().equals(MapleQuestRequirementType.MOB)) {
					for (MapleData mob : startReq.getChildren()) {
						relevantMobs.add(MapleDataTool.getInt(mob.getChildByPath("id")));
					}
				}
				startReqs.add(req);
			}
		}
		MapleData completeReqData = requirements.getChildByPath(String.valueOf(id)).getChildByPath("1");
		completeReqs = new LinkedList<MapleQuestRequirement>();
		if (completeReqData != null) {
			for (MapleData completeReq : completeReqData.getChildren()) {
				MapleQuestRequirement req = new MapleQuestRequirement(
					this, MapleQuestRequirementType.getByWZName(completeReq.getName()), completeReq);
				if (req.getType().equals(MapleQuestRequirementType.MOB)) {
					for (MapleData mob : completeReq.getChildren()) {
						relevantMobs.add(MapleDataTool.getInt(mob.getChildByPath("id")));
					}
				}
				completeReqs.add(req);
			}
		}
		// read acts
		MapleData startActData = actions.getChildByPath(String.valueOf(id)).getChildByPath("0");
		startActs = new LinkedList<MapleQuestAction>();
		if (startActData != null) {
			for (MapleData startAct : startActData.getChildren()) {
				MapleQuestActionType questActionType = MapleQuestActionType.getByWZName(startAct.getName());
				startActs.add(new MapleQuestAction(questActionType, startAct, this));
			}
		}
		MapleData completeActData = actions.getChildByPath(String.valueOf(id)).getChildByPath("1");
		completeActs = new LinkedList<MapleQuestAction>();
		if (completeActData != null) {
			for (MapleData completeAct : completeActData.getChildren()) {
				completeActs.add(new MapleQuestAction(
					MapleQuestActionType.getByWZName(completeAct.getName()), completeAct, this));
			}
		}
		MapleData questInfo = info.getChildByPath(String.valueOf(id));
		autoStart = MapleDataTool.getInt("autoStart", questInfo, 0) == 1;
		autoPreComplete = MapleDataTool.getInt("autoPreComplete", questInfo, 0) == 1;
	}
	
	public static MapleQuest getInstance(int id) {
		MapleQuest ret = quests.get(id);
		if (ret == null) {
			if (id > 99999)
				ret = new MapleCustomQuest(id);
			else
				ret = new MapleQuest(id);
			quests.put(id, ret);
		}
		return ret;
	}
	
	private boolean canStart(MapleCharacter c, Integer npcid) {
		if (c.getQuest(this).getStatus() != Status.NOT_STARTED && !(c.getQuest(this).getStatus() == Status.COMPLETED && repeatable)) {
			return false;
		}
		for (MapleQuestRequirement r : startReqs) {
			if (!r.check(c, npcid)) return false;
		}
		return true;
	}
	
	public boolean canComplete(MapleCharacter c, Integer npcid) {
		if (!c.getQuest(this).getStatus().equals(Status.STARTED)) return false;
		for (MapleQuestRequirement r : completeReqs) {
			if (!r.check(c, npcid)) return false;
		}
		return true;
	}
	
	public void start(MapleCharacter c, int npc) {
		if ((autoStart || checkNPCOnMap(c, npc)) && canStart(c, npc)) {
			for (MapleQuestAction a : startActs) {
				a.run(c, null);
			}
			MapleQuestStatus oldStatus = c.getQuest(this);
			MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.STARTED, npc);
			newStatus.setCompletionTime(oldStatus.getCompletionTime());
			newStatus.setForfeited(oldStatus.getForfeited());
			c.updateQuest(newStatus);
		}
	}

	public void complete(MapleCharacter c, int npc) {
		complete(c, npc, null);
	}
	
	public void complete(MapleCharacter c, int npc, Integer selection) {
		if ((autoPreComplete || checkNPCOnMap(c, npc)) && canComplete(c, npc)) {
			for (MapleQuestAction a : completeActs) {
				if (!a.check(c)) {
					return;
				}
			}
			for (MapleQuestAction a : completeActs) {
				a.run(c, selection);
			}
			// we save forfeits only for logging purposes, they shouldn't matter anymore
			// completion time is set by the constructor
			MapleQuestStatus oldStatus = c.getQuest(this);
			MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.COMPLETED, npc);
			newStatus.setForfeited(oldStatus.getForfeited());
			c.updateQuest(newStatus);
		}
	}
	
	public void forfeit(MapleCharacter c) {
		if (!c.getQuest(this).getStatus().equals(Status.STARTED)) return;
		MapleQuestStatus oldStatus = c.getQuest(this);
		MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.NOT_STARTED);
		newStatus.setForfeited(oldStatus.getForfeited() + 1);
		newStatus.setCompletionTime(oldStatus.getCompletionTime());
		c.updateQuest(newStatus);
	}

	public void forceStart(MapleCharacter c, int npc) {
		MapleQuestStatus oldStatus = c.getQuest(this);
		MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.STARTED, npc);
		newStatus.setForfeited(oldStatus.getForfeited());
		c.updateQuest(newStatus);
	}

	public void forceComplete(MapleCharacter c, int npc) {
		MapleQuestStatus oldStatus = c.getQuest(this);
		MapleQuestStatus newStatus = new MapleQuestStatus(this, MapleQuestStatus.Status.COMPLETED, npc);
		newStatus.setForfeited(oldStatus.getForfeited());
		c.updateQuest(newStatus);
	}

	public int getId() {
		return id;
	}

	public List<Integer> getRelevantMobs() {
		return Collections.unmodifiableList(relevantMobs);
	}
	
	private boolean checkNPCOnMap(MapleCharacter player, int npcid) {
		if (player.getMap().containsNPC(npcid)) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<Integer> getQuestItemsToShowOnlyIfQuestIsActivated() {
		Set<Integer> delta = new HashSet<Integer>();
		for(MapleQuestRequirement mqr : this.completeReqs) {
			if(mqr.getType() != MapleQuestRequirementType.ITEM) continue;
			delta.addAll(mqr.getQuestItemsToShowOnlyIfQuestIsActivated());
		}
		List<Integer> returnThis = new ArrayList<Integer>();
		returnThis.addAll(delta);
		return Collections.unmodifiableList(returnThis); 
	}
}
