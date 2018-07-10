package net.sf.odinms.net.world;

import java.io.Serializable;

/**
 *
 * @author Danny
 */
public class PlayerCoolDownValueHolder implements Serializable {

    public int skillId;
    public long startTime;
    public long length;
    private int id;

    public PlayerCoolDownValueHolder(int skillId, long startTime, long length) {
        this.skillId = skillId;
        this.startTime = startTime;
        this.length = length;
        this.id = (int) (Math.random() * 100);
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerCoolDownValueHolder other = (PlayerCoolDownValueHolder) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}