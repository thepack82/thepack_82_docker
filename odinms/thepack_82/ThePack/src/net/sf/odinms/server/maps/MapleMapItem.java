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
 * MapleMapItem.java
 *
 * Created on 29. November 2007, 12:54
 */

package net.sf.odinms.server.maps;

import java.awt.Point;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */

public class MapleMapItem extends AbstractMapleMapObject {
	protected IItem item;
	protected MapleMapObject dropper;
	protected MapleCharacter owner;
	protected int meso;
	protected int displayMeso;
	protected boolean pickedUp = false;
	
	/** Creates a new instance of MapleMapItem */
	public MapleMapItem(IItem item, Point position, MapleMapObject dropper, MapleCharacter owner) {
		setPosition(position);
		this.item = item;
		this.dropper = dropper;
		this.owner = owner;
		this.meso = 0;
	}
	
	public MapleMapItem(int meso, int displayMeso, Point position, MapleMapObject dropper, MapleCharacter owner) {
		setPosition(position);
		this.item = null;
		this.meso = meso;
		this.displayMeso = displayMeso;
		this.dropper = dropper;
		this.owner = owner;
	}
	
	public IItem getItem() {
		return item;
	}

	public MapleMapObject getDropper() {
		return dropper;
	}

	public MapleCharacter getOwner() {
		return owner;
	}

	public int getMeso() {
		return meso;
	}

	public boolean isPickedUp() {
		return pickedUp;
	}

	public void setPickedUp(boolean pickedUp) {
		this.pickedUp = pickedUp;
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.removeItemFromMap(getObjectId(), 1, 0));
	}
	
	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.ITEM;
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		if (getMeso() > 0) {
			client.getSession().write(MaplePacketCreator.dropMesoFromMapObject(displayMeso, getObjectId(), 
				getDropper().getObjectId(), getOwner().getId(), null, getPosition(), (byte) 2));
		} else {
			client.getSession().write(MaplePacketCreator.dropItemFromMapObject(getItem().getItemId(), getObjectId(),
				0, getOwner().getId(), null, getPosition(), (byte) 2));
		}
	}
}
