package net.sf.odinms.net.world.remote;

import java.io.Serializable;

/**
 *
 * @author Matze
 */
public class WorldLocation implements Serializable {
	
	public int map, channel;

	public WorldLocation(int map, int channel) {
		this.map = map;
		this.channel = channel;
	}
	
}
