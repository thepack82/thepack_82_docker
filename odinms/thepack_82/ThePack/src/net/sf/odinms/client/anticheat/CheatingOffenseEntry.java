package net.sf.odinms.client.anticheat;

import net.sf.odinms.client.MapleCharacter;

public class CheatingOffenseEntry {

    private CheatingOffense offense;
    private int count = 0;
    private MapleCharacter chrfor;
    private long lastOffense;
    private long firstOffense;
    private String param;
    private int dbid = -1;

    public CheatingOffenseEntry(CheatingOffense offense, MapleCharacter chrfor) {
        super();
        this.offense = offense;
        this.chrfor = chrfor;
        firstOffense = System.currentTimeMillis();
    }

    public CheatingOffense getOffense() {
        return offense;
    }

    public int getCount() {
        return count;
    }

    public MapleCharacter getChrfor() {
        return chrfor;
    }

    public void incrementCount() {
        this.count++;
        lastOffense = System.currentTimeMillis();
    }

    public boolean isExpired() {
        if (lastOffense < (System.currentTimeMillis() - offense.getValidityDuration())) {
            return true;
        }
        return false;
    }

    public int getPoints() {
        return count * offense.getPoints();
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public long getLastOffenseTime() {
        return lastOffense;
    }

    public int getDbId() {
        return dbid;
    }

    public void setDbId(int dbid) {
        this.dbid = dbid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chrfor == null) ? 0 : chrfor.getId());
        result = prime * result + ((offense == null) ? 0 : offense.hashCode());
        result = prime * result + Long.valueOf(firstOffense).hashCode();
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
        final CheatingOffenseEntry other = (CheatingOffenseEntry) obj;
        if (chrfor == null) {
            if (other.chrfor != null) {
                return false;
            }
        } else if (chrfor.getId() != other.chrfor.getId()) {
            return false;
        }
        if (offense == null) {
            if (other.offense != null) {
                return false;
            }
        } else if (!offense.equals(other.offense)) {
            return false;
        }
        if (other.firstOffense != firstOffense) {
            return false;
        }
        return true;
    }
}