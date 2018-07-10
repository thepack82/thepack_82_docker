package net.sf.odinms.net.world.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import net.sf.odinms.net.channel.remote.ChannelWorldInterface;
import net.sf.odinms.net.login.remote.LoginWorldInterface;

/**
 *
 * @author Matze
 */
public interface WorldRegistry extends Remote {

	public WorldChannelInterface registerChannelServer(String authKey, ChannelWorldInterface cb) throws RemoteException;
	public void deregisterChannelServer(int channel) throws RemoteException;
	public WorldLoginInterface registerLoginServer(String authKey, LoginWorldInterface cb) throws RemoteException;
	public void deregisterLoginServer(LoginWorldInterface cb) throws RemoteException;
	public String getStatus() throws RemoteException;
	
}
