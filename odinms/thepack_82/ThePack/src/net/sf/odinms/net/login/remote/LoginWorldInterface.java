package net.sf.odinms.net.login.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Matze
 */
public interface LoginWorldInterface extends Remote {
	
	public void channelOnline(int channel, String ip) throws RemoteException;
	public void channelOffline(int channel) throws RemoteException;
	public void shutdown() throws RemoteException;
	public boolean isAvailable() throws RemoteException;
	public double getPossibleLoginAverage() throws RemoteException;
	public int getWaitingUsers() throws RemoteException;

}
