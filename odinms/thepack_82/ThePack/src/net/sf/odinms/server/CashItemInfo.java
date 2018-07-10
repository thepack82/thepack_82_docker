/*
 * This file is part of the OdinMS Maple Story Server
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

package net.sf.odinms.server;

/**
 *
 * @author Lerk
 */
public class CashItemInfo {
	private int itemId;
	private int count;
	private int price;
	
	public CashItemInfo(int itemId, int count, int price) {
		this.itemId = itemId;
		this.count = count;
		this.price = price;
	}
	
	public int getId() {
		return itemId;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getPrice() {
		return price;
	}
}