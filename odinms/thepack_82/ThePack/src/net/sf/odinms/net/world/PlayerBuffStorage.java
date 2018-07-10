package net.sf.odinms.net.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.sf.odinms.tools.Pair;

/**
 *
 * @author Danny
 */
public class PlayerBuffStorage implements Serializable {
	private List<Pair<Integer, List<PlayerBuffValueHolder>>> buffs = new ArrayList<Pair<Integer, List<PlayerBuffValueHolder>>>();
	private List<Pair<Integer, List<PlayerCoolDownValueHolder>>> coolDowns = new ArrayList<Pair<Integer, List<PlayerCoolDownValueHolder>>>();
	private int id = (int) (Math.random()*100);
	
	public PlayerBuffStorage() {
	}
	
	public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) {
		for (Pair<Integer, List<PlayerBuffValueHolder>> stored : buffs) {
			if (stored.getLeft() == Integer.valueOf(chrid)) {
				buffs.remove(stored);
			}
		}
		buffs.add(new Pair<Integer, List<PlayerBuffValueHolder>>(Integer.valueOf(chrid), toStore));
	}
	
	public void addCooldownsToStorage(int chrid, List<PlayerCoolDownValueHolder> toStore) {
		for (Pair<Integer, List<PlayerCoolDownValueHolder>> stored : coolDowns) {
			if (stored.getLeft() == Integer.valueOf(chrid)) {
				coolDowns.remove(stored);
			}
		}
		coolDowns.add(new Pair<Integer, List<PlayerCoolDownValueHolder>>(Integer.valueOf(chrid), toStore));
	}
	
	public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) {
		List<PlayerBuffValueHolder> ret = null;
		Pair<Integer, List<PlayerBuffValueHolder>> stored;
		for (int i = 0; i < buffs.size(); i++) {
			stored = buffs.get(i);
			if (stored.getLeft().equals(Integer.valueOf(chrid))) {
				ret = stored.getRight();
				buffs.remove(stored);
			}
		}
		return ret;
	}
	
	public List<PlayerCoolDownValueHolder> getCooldownsFromStorage(int chrid) {
		List<PlayerCoolDownValueHolder> ret = null;
		Pair<Integer, List<PlayerCoolDownValueHolder>> stored;
		for (int i = 0; i < coolDowns.size(); i++) {
			stored = coolDowns.get(i);
			if (stored.getLeft().equals(Integer.valueOf(chrid))) {
				ret = stored.getRight();
				coolDowns.remove(stored);
			}
		}
		return ret;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PlayerBuffStorage other = (PlayerBuffStorage) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
