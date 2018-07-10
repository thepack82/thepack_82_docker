package net.sf.odinms.net.world.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import net.sf.odinms.net.world.guild.MapleGuildCharacter;

/**
 *
 * @author Matze
 */
public interface WorldLoginInterface extends Remote {

	public Properties getDatabaseProperties() throws RemoteException;
	public Properties getWorldProperties() throws RemoteException;
	public Map<Integer, Integer> getChannelLoad() throws RemoteException;
	public boolean isAvailable() throws RemoteException;
	public void deleteGuildCharacter(MapleGuildCharacter mgc) throws RemoteException;
}
