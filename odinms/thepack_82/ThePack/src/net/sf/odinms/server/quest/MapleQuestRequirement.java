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
 * MapleQuestRequirement.java
 *
 * Created on 10. Dezember 2007, 23:15
 */

package net.sf.odinms.server.quest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.server.MapleItemInformationProvider;

/**
 *
 * @author Matze
 */

public class MapleQuestRequirement {
	private MapleQuestRequirementType type;
	private MapleData data;
	private MapleQuest quest;
	
	/** Creates a new instance of MapleQuestRequirement */
	public MapleQuestRequirement(MapleQuest quest, MapleQuestRequirementType type, MapleData data) {
		this.type = type;
		this.data = data;
		this.quest = quest;
	}
	
	boolean check(MapleCharacter c, Integer npcid) {
		switch (getType()) {
			case JOB:
				for (MapleData jobEntry : getData().getChildren())
					if (c.getJob().equals(MapleJob.getById(MapleDataTool.getInt(jobEntry))) || c.isGM())
						return true;
				return false;
			case QUEST:
				for (MapleData questEntry : getData().getChildren()) {
					MapleQuestStatus q = c.getQuest(MapleQuest.getInstance(MapleDataTool.getInt(questEntry.getChildByPath("id"))));
					if (q == null && MapleQuestStatus.Status.getById(MapleDataTool.getInt(questEntry.getChildByPath("state"))).equals(MapleQuestStatus.Status.NOT_STARTED))
						continue;
					if (q == null || !q.getStatus().equals(MapleQuestStatus.Status.getById(MapleDataTool.getInt(questEntry.getChildByPath("state")))))
						return false;
				}
				return true;
			case ITEM:
				MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
				for (MapleData itemEntry : getData().getChildren()) {
					int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
					short quantity = 0;
					MapleInventoryType iType = ii.getInventoryType(itemId);
					for (IItem item : c.getInventory(iType).listById(itemId)) {
						quantity += item.getQuantity();
					}
					if (quantity < MapleDataTool.getInt(itemEntry.getChildByPath("count")) || MapleDataTool.getInt(itemEntry.getChildByPath("count")) <= 0 && quantity > 0) return false;
				}
				return true;
			case MIN_LEVEL:
				return c.getLevel() >= MapleDataTool.getInt(getData());
			case MAX_LEVEL:
				return c.getLevel() <= MapleDataTool.getInt(getData());
			case END_DATE:
				String timeStr = MapleDataTool.getString(getData());
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(timeStr.substring(0, 4)), Integer.parseInt(timeStr.substring(4, 6)), Integer.parseInt(timeStr.substring(6, 8)), Integer.parseInt(timeStr.substring(8, 10)), 0);
				return cal.getTimeInMillis() >= System.currentTimeMillis();
			case MOB:
				for (MapleData mobEntry : getData().getChildren()) {
					int mobId = MapleDataTool.getInt(mobEntry.getChildByPath("id"));
					int killReq = MapleDataTool.getInt(mobEntry.getChildByPath("count"));
					if (c.getQuest(quest).getMobKills(mobId) < killReq)
						return false;
				}
				return true;
			case NPC:
				return npcid == null || npcid == MapleDataTool.getInt(getData());
			case FIELD_ENTER:
				MapleData zeroField = getData().getChildByPath("0");
				if (zeroField != null) {
					return MapleDataTool.getInt(zeroField) == c.getMapId();
				}
				return false;
			case INTERVAL:
				return !c.getQuest(quest).getStatus().equals(MapleQuestStatus.Status.COMPLETED) || c.getQuest(quest).getCompletionTime() <= System.currentTimeMillis() - MapleDataTool.getInt(getData()) * 60 * 1000;
//			case PET:
//			case MIN_PET_TAMENESS:
			default:
				return true;
		}
	}

	public MapleQuestRequirementType getType() {
		return type;
	}

	public MapleData getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return type.toString() + " " + data.toString() + " " + quest.toString();
	}
	
	public List<Integer> getQuestItemsToShowOnlyIfQuestIsActivated() {
		if (type != MapleQuestRequirementType.ITEM) return null;
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		List<Integer> delta = new ArrayList<Integer>();
		for (MapleData itemEntry : getData().getChildren()) {
			int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
			if(ii.isQuestItem(itemId)) delta.add(itemId);
		}
		return Collections.unmodifiableList(delta);
	}
}
