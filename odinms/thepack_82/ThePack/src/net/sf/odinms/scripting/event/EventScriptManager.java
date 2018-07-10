package net.sf.odinms.scripting.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.scripting.AbstractScriptManager;

/**
 *
 * @author Matze
 */
public class EventScriptManager extends AbstractScriptManager {

	private class EventEntry {
		public EventEntry(String script, Invocable iv, EventManager em) {
			this.script = script;
			this.iv = iv;
			this.em = em;
		}
		
		public String script;
		public Invocable iv;
		public EventManager em;
	}
	
	private Map<String,EventEntry> events = new LinkedHashMap<String,EventEntry>();
	
	public EventScriptManager(ChannelServer cserv, String[] scripts) {
		super();
		for (String script : scripts) {
			if (!script.equals("")) {
				Invocable iv = getInvocable("event/" + script + ".js", null);
				events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
			}
		}
	}
	
	public EventManager getEventManager(String event) {
		EventEntry entry = events.get(event);
		if (entry == null) {
			return null;
		}
		return entry.em;
	}
	
	public void init() {
		for (EventEntry entry : events.values()) {
			try {
				((ScriptEngine) entry.iv).put("em", entry.em);
				entry.iv.invokeFunction("init", (Object) null);
			} catch (ScriptException ex) {
				Logger.getLogger(EventScriptManager.class.getName()).log(Level.SEVERE, null, ex);
			} catch (NoSuchMethodException ex) {
				Logger.getLogger(EventScriptManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	public void cancel() {
		for (EventEntry entry : events.values()) {
			entry.em.cancel();
		}
	}
	
}
