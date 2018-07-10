package net.sf.odinms.net.world;

import java.io.Serializable;
import net.sf.odinms.server.MapleStatEffect;

/**
 *
 * @author Danny
 */
public class PlayerBuffValueHolder implements Serializable {
	static final long serialVersionUID = 9179541993413738569L;
	public long startTime;
	public MapleStatEffect effect;
	private int id;

	public PlayerBuffValueHolder(long startTime, MapleStatEffect effect) {
		this.startTime = startTime;
		this.effect = effect;
		this.id = (int) (Math.random()*100);
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
		final PlayerBuffValueHolder other = (PlayerBuffValueHolder) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
