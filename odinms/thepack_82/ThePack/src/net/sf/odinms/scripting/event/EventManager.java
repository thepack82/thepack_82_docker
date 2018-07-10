package net.sf.odinms.scripting.event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptException;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.maps.MapleMap;

/**
 *
 * @author Matze
 */
public class EventManager {

	private Invocable iv;
	private ChannelServer cserv;
	private Map<String,EventInstanceManager> instances = new HashMap<String,EventInstanceManager>();
	private Properties props = new Properties();
	private String name;
	
	public EventManager(ChannelServer cserv, Invocable iv, String name) {
		this.iv = iv;
		this.cserv = cserv;
		this.name = name;
	}
	
	public void cancel() {
		try {
			iv.invokeFunction("cancelSchedule", (Object) null);
		} catch (ScriptException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void schedule(final String methodName, long delay) {
		TimerManager.getInstance().schedule(new Runnable() {

			public void run() {
				try {
					iv.invokeFunction(methodName, (Object) null);
				} catch (ScriptException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			
		}, delay);
	}
	
	public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
		return TimerManager.getInstance().scheduleAtTimestamp(new Runnable() {

			public void run() {
				try {
					iv.invokeFunction(methodName, (Object) null);
				} catch (ScriptException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			
		}, timestamp);		
	}
	
	public ChannelServer getChannelServer() {
		return cserv;
	}
	
	public EventInstanceManager getInstance(String name) {
		return instances.get(name);
	}
	
	public Collection<EventInstanceManager> getInstances() {
		return Collections.unmodifiableCollection(instances.values());
	}
	
	public EventInstanceManager newInstance(String name) {
		EventInstanceManager ret = new EventInstanceManager(this, name);
		instances.put(name, ret);
		return ret;
	}
	
	public void disposeInstance(String name) {
		instances.remove(name);
	}

	public Invocable getIv() {
		return iv;
	}
	
	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getName() {
		return name;
	}
	
	//PQ method: starts a PQ
	public void startInstance(MapleParty party, MapleMap map) {
		try {
			EventInstanceManager eim = (EventInstanceManager)(iv.invokeFunction("setup", (Object) null));
			eim.registerParty(party, map);
		} catch (ScriptException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	//non-PQ method for starting instance
	public void startInstance(EventInstanceManager eim, String leader) {
		try {
			iv.invokeFunction("setup", eim);
			eim.setProperty("leader", leader);
		} catch (ScriptException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
