package net.sf.odinms.net.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MapleParty implements Serializable {
	private static final long serialVersionUID = 9179541993413738569L;
	private MaplePartyCharacter leader;
	private List<MaplePartyCharacter> members = new LinkedList<MaplePartyCharacter>();
	private int id;
        private int CP;
        private int team;
        private int totalCP;
	
	public MapleParty(int id, MaplePartyCharacter chrfor) {
		this.leader = chrfor;
		this.members.add(this.leader);
		this.id = id;
	}
	
	public boolean containsMembers (MaplePartyCharacter member) {
		return members.contains(member);
	}
	
	public void addMember (MaplePartyCharacter member) {
		members.add(member);
	}
	
	public void removeMember (MaplePartyCharacter member) {
		members.remove(member);
	}
	
	public void updateMember(MaplePartyCharacter member) {
		for (int i = 0; i < members.size(); i++) {
			MaplePartyCharacter chr = members.get(i);
			if (chr.equals(member)) {
				members.set(i, member);
			}
		}
	}
	
	public MaplePartyCharacter getMemberById(int id) {
		for (MaplePartyCharacter chr : members) {
			if (chr.getId() == id) {
				return chr;
			}
		}
		return null;
	}
	
	public Collection<MaplePartyCharacter> getMembers () {
		return Collections.unmodifiableList(members);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
        
        public int getCP() {
            return this.CP;
        }

        public int getTeam() {
            return this.team;
        }

        public int getTotalCP() {
            return this.totalCP;
        }

        public void setCP(int cp) {
            this.CP = cp;
        }

        public void setTeam(int team) {
            this.team = team;
        }

        public void setTotalCP(int totalcp) {
            this.totalCP = totalcp;
        }

	public MaplePartyCharacter getLeader() {
		return leader;
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
		final MapleParty other = (MapleParty) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
