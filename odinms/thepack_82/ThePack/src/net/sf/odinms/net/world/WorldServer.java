package net.sf.odinms.net.world;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import net.sf.odinms.database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matze
 */
public class WorldServer {

	private static WorldServer instance = null;
	private static Logger log = LoggerFactory.getLogger(WorldServer.class);
	private int worldId;
	private Properties dbProp = new Properties();
	private Properties worldProp = new Properties();
	
	private WorldServer() {
		try {
			InputStreamReader is = new FileReader("db.properties");
			dbProp.load(is);
			is.close();
			DatabaseConnection.setProps(dbProp);
			DatabaseConnection.getConnection();
			is = new FileReader("world.properties");
			worldProp.load(is);
			is.close();
		} catch (Exception e) {
			log.error("Could not configuration", e);
		}
	}
	
	public synchronized static WorldServer getInstance() {
		if (instance == null) instance = new WorldServer();
		return instance;
	}

	public int getWorldId() {
		return worldId;
	}

	public Properties getDbProp() {
		return dbProp;
	}

	public Properties getWorldProp() {
		return worldProp;
	}
	
	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT, 
				new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
			registry.rebind("WorldRegistry", WorldRegistryImpl.getInstance());
		} catch (RemoteException ex) {
			log.error("Could not initialize RMI system", ex);
		}
	}
	
}
