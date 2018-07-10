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

package net.sf.odinms.server.maps;

import java.awt.Point;
import java.awt.Rectangle;

import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.tools.MaplePacketCreator;

public class MapleMist extends AbstractMapleMapObject {
	private Rectangle mistPosition;
	private MapleCharacter owner;
	private MapleStatEffect source;
	
	public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
		this.mistPosition = mistPosition;
		this.owner = owner;
		this.source = source;
	}
	
	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.MIST;
	}
	
	@Override
	public Point getPosition() {
		return mistPosition.getLocation();
	}
	
	public MapleCharacter getOwner() {
		return owner;
	}
	
	public ISkill getSourceSkill() {
		return SkillFactory.getSkill(source.getSourceId());
	}
	
	public Rectangle getBox() {
		return mistPosition;
	}

	@Override
	public void setPosition(Point position) {
		throw new UnsupportedOperationException();
	}

	public MaplePacket makeDestroyData() {
		return MaplePacketCreator.removeMist(getObjectId());
	}
	
	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(makeDestroyData());
	}
	
	public MaplePacket makeSpawnData() {
		int level = owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId()));
		return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), source.getSourceId(), mistPosition, level);
	}
	
	public MaplePacket makeFakeSpawnData(int level) {
		return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), source.getSourceId(), mistPosition, level);
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		client.getSession().write(makeSpawnData());
	}
	
	public boolean makeChanceResult() {
		return source.makeChanceResult();
	}
}
