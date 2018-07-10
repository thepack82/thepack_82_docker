package net.sf.odinms.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.odinms.server.MapleItemInformationProvider;

/**
 * 
 * @author Matze
 */

public class MapleInventory implements Iterable<IItem>, InventoryContainer {

	private Map<Byte, IItem> inventory;
	private byte slotLimit;
	private MapleInventoryType type;

	public MapleInventory(MapleInventoryType type, byte slotLimit) {
		this.inventory = new LinkedHashMap<Byte, IItem>();
		this.slotLimit = slotLimit;
		this.type = type;
	}

	public IItem findById(int itemId) {
		for (IItem item : inventory.values()) {
			if (item.getItemId() == itemId)
				return item;
		}
		return null;
	}
	
	public int countById(int itemId) {
		int possesed = 0;
		for (IItem item : inventory.values()) {
			if (item.getItemId() == itemId)
				possesed += item.getQuantity();
		}
		return possesed;
	}

	public List<IItem> listById(int itemId) {
		List<IItem> ret = new ArrayList<IItem>();
		for (IItem item : inventory.values()) {
			if (item.getItemId() == itemId) {
				ret.add(item);
			}
		}
		if (ret.size() > 1) {
			Collections.sort(ret);
		}
		return ret;
	}

	public Collection<IItem> list() {
		return inventory.values();
	}

	public byte addItem(IItem item) {
		byte slotId = getNextFreeSlot();
		if (slotId < 0) {
			return -1;
		}
		inventory.put(slotId, item);
		item.setPosition(slotId);
		return slotId;
	}
	
	public void addFromDB(IItem item) {
		if (item.getPosition() < 0 && !type.equals(MapleInventoryType.EQUIPPED))
			throw new RuntimeException("Item with negative position in non-equipped IV wtf?");
		inventory.put(item.getPosition(), item);
	}

	public void move(byte sSlot, byte dSlot, short slotMax) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		Item source = (Item) inventory.get(sSlot);
		Item target = (Item) inventory.get(dSlot);
		if (source == null)
			throw new InventoryException("Trying to move empty slot");
		if (target == null) {
			source.setPosition(dSlot);
			inventory.put(dSlot, source);
			inventory.remove(sSlot);
		} else if (target.getItemId() == source.getItemId() && !ii.isThrowingStar(source.getItemId()) && !ii.isBullet(source.getItemId())) {
			if (type.getType() == MapleInventoryType.EQUIP.getType())
				swap(target, source);
			if (source.getQuantity() + target.getQuantity() > slotMax) {
				short rest = (short) ((source.getQuantity() + target.getQuantity()) - slotMax);
				source.setQuantity(rest);
				target.setQuantity(slotMax);
			} else {
				target.setQuantity((short) (source.getQuantity() + target.getQuantity()));
				inventory.remove(sSlot);
			}
		} else {
			swap(target, source);
		}
	}

	private void swap(IItem source, IItem target) {
		inventory.remove(source.getPosition());
		inventory.remove(target.getPosition());
		byte swapPos = source.getPosition();
		source.setPosition(target.getPosition());
		target.setPosition(swapPos);
		inventory.put(source.getPosition(), source);
		inventory.put(target.getPosition(), target);
	}

	public IItem getItem(byte slot) {
		return inventory.get(slot);
	}

	public void removeItem(byte slot) {
		removeItem(slot, (short) 1, false);
	}

	public void removeItem(byte slot, short quantity, boolean allowZero) {
		IItem item = inventory.get(slot);
		if (item == null) // TODO is it ok not to throw an exception here?
			return;
		item.setQuantity((short) (item.getQuantity() - quantity));
		if (item.getQuantity() < 0)
			item.setQuantity((short) 0);
		if (item.getQuantity() == 0 && !allowZero)
			removeSlot(slot);
	}

	public void removeSlot(byte slot) {
		inventory.remove(slot);
	}

	public boolean isFull() {
		return inventory.size() >= slotLimit;
	}
	
	public boolean isFull(int margin) {
		return inventory.size() + margin >= slotLimit;
	}

	public byte getNextFreeSlot() {
		if (isFull())
			return -1;
		for (byte i = 1; i <= slotLimit; i++) {
			if (!inventory.keySet().contains(i))
				return i;
		}
		return -1;
	}

	public MapleInventoryType getType() {
		return type;
	}

	@Override
	public Iterator<IItem> iterator() {
		return Collections.unmodifiableCollection(inventory.values()).iterator();
	}

	@Override
	public Collection<MapleInventory> allInventories() {
		return Collections.singletonList(this);
	}
}
