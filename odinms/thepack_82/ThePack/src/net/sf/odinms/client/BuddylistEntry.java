package net.sf.odinms.client;

public class BuddylistEntry {
	private String name;
	private int cid;
	private int channel;
	private boolean visible;

	/**
	 * 
	 * @param name
	 * @param characterId
	 * @param channel should be -1 if the buddy is offline
	 * @param visible
	 */
	public BuddylistEntry(String name, int characterId, int channel, boolean visible) {
		super();
		this.name = name;
		this.cid = characterId;
		this.channel = channel;
		this.visible = visible;
	}

	/**
	 * @return the channel the character is on. If the character is offline returns -1.
	 */
	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public boolean isOnline() {
		return channel >= 0;
	}

	public void setOffline() {
		channel = -1;
	}

	public String getName() {
		return name;
	}

	public int getCharacterId() {
		return cid;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cid;
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
		final BuddylistEntry other = (BuddylistEntry) obj;
		if (cid != other.cid)
			return false;
		return true;
	}
}