package net.sf.odinms.scripting.portal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.server.MaplePortal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalScriptManager {
	private static final Logger log = LoggerFactory.getLogger(PortalScriptManager.class);
	private static PortalScriptManager instance = new PortalScriptManager();
	private Map<String, PortalScript> scripts = new HashMap<String, PortalScript>();
	private ScriptEngineFactory sef;

	private PortalScriptManager() {
		ScriptEngineManager sem = new ScriptEngineManager();
		sef = sem.getEngineByName("javascript").getFactory();
	}
	
	public static PortalScriptManager getInstance() {
		return instance;
	}

	private PortalScript getPortalScript(String scriptName) {
		if (scripts.containsKey(scriptName)) {
			return scripts.get(scriptName);
		}
		File scriptFile = new File("scripts/portal/" + scriptName + ".js");
		if (!scriptFile.exists()) {
			scripts.put(scriptName, null);
			return null;
		}
		FileReader fr = null;
		ScriptEngine portal = sef.getScriptEngine();
		try {
			fr = new FileReader(scriptFile);
			CompiledScript compiled = ((Compilable) portal).compile(fr);
			compiled.eval();
		} catch (ScriptException e) {
			log.error("THROW", e);
		} catch (IOException e) {
			log.error("THROW", e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					log.error("ERROR CLOSING", e);
				}
			}
		}
		PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
		scripts.put(scriptName, script);
		return script;
	}

    public boolean executePortalScript(MaplePortal portal, MapleClient c) {
		PortalScript script = getPortalScript(portal.getScriptName());

		if (script != null) {
			return script.enter(new PortalPlayerInteraction(c, portal));
		} else {
			return false;
		}
	}
	
	public void clearScripts() {
		scripts.clear();
	}
}
