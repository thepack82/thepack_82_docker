package net.sf.odinms.net.login;

import java.rmi.RemoteException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matze
 */

public class LoginWorker implements Runnable {

	private static LoginWorker instance = new LoginWorker();
	private Deque<MapleClient> waiting;
	private Set<String> waitingNames;
	private List<Integer> possibleLoginHistory = new LinkedList<Integer>();

	public static Logger log = LoggerFactory.getLogger(LoginWorker.class);

	private LoginWorker() {
		waiting = new LinkedList<MapleClient>();
		waitingNames = new HashSet<String>();
	}

	public static LoginWorker getInstance() {
		return instance;
	}

	public void registerClient(MapleClient c) {
		synchronized (waiting) {
			if (!waiting.contains(c) && !waitingNames.contains(c.getAccountName().toLowerCase())) {
				waiting.add(c);
				waitingNames.add(c.getAccountName().toLowerCase());
				c.updateLoginState(MapleClient.LOGIN_WAITING);
			}
		}
	}
	
	public void registerGMClient(MapleClient c) {
		synchronized (waiting) {
			if (!waiting.contains(c) && !waitingNames.contains(c.getAccountName().toLowerCase())) {
				waiting.addFirst(c);
				waitingNames.add(c.getAccountName().toLowerCase());
				c.updateLoginState(MapleClient.LOGIN_WAITING);
			}
		}
	}


	public void deregisterClient(MapleClient c) {
		synchronized (waiting) {
			waiting.remove(c);
			if (c.getAccountName() != null) {
				waitingNames.remove(c.getAccountName().toLowerCase());
			}
		}
	}

	public void run() {
		try {
			int possibleLogins = LoginServer.getInstance().getPossibleLogins();
			LoginServer.getInstance().getWorldInterface().isAvailable();

			if (possibleLoginHistory.size() >= (5 * 60 * 1000) / LoginServer.getInstance().getLoginInterval()) {
				possibleLoginHistory.remove(0);
			}
			possibleLoginHistory.add(possibleLogins);

//			log.info("Possible logins: " + possibleLogins + " (Waiting: " + waiting.size() + ")");
			if (possibleLogins == 0 && waiting.peek().isGm()) // Server is full but front of a queue is a GM
				possibleLogins = 1;
			for (int i = 0; i < possibleLogins; i++) {
				final MapleClient client;
				synchronized (waiting) {
					if (waiting.isEmpty()) {
						break;
					}
					client = waiting.removeFirst();
				}
				waitingNames.remove(client.getAccountName().toLowerCase());
				if (client.finishLogin(true) == 0) {
					client.getSession().write(MaplePacketCreator.getAuthSuccessRequestPin(client.getAccountName()));
					client.setIdleTask(TimerManager.getInstance().schedule(new Runnable() {

						public void run() {
							client.getSession().close();
						}
					}, 10 * 60 * 10000));
				} else {
					client.getSession().write(MaplePacketCreator.getLoginFailed(7));
				}
			}

			Map<Integer, Integer> load = LoginServer.getInstance().getWorldInterface().getChannelLoad();
			double loadFactor = 1200 / ((double) LoginServer.getInstance().getUserLimit() / load.size());
			for (Entry<Integer, Integer> entry : load.entrySet()) {
				load.put(entry.getKey(), Math.min(1200, (int) (entry.getValue() * loadFactor)));
			}
			LoginServer.getInstance().setLoad(load);
		} catch (RemoteException ex) {
			LoginServer.getInstance().reconnectWorld();
		}
	}

	public double getPossibleLoginAverage() {
		int sum = 0;
		for (Integer i : possibleLoginHistory) {
			sum += i;
		}
		return (double) sum / (double) possibleLoginHistory.size();
	}
	
	public int getWaitingUsers() {
		return waiting.size();
	}
}
