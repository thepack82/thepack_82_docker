package net.sf.odinms.server;

import java.rmi.RemoteException;
import java.sql.SQLException;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.channel.ChannelServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Frz
 */
public class ShutdownServer implements Runnable {

    private static Logger log = LoggerFactory.getLogger(ShutdownServer.class);
    private int myChannel;

    public ShutdownServer(int channel) {
        myChannel = channel;
    }

    @Override
    public void run() {
        try {
            ChannelServer.getInstance(myChannel).shutdown();
        } catch (Throwable t) {
            log.error("SHUTDOWN ERROR", t);
        }
        int c = 200;
        while (ChannelServer.getInstance(myChannel).getConnectedClients() > 0 && c > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("ERROR", e);
            }
            c--;
        }
        try {
            ChannelServer.getWorldRegistry().deregisterChannelServer(myChannel);
        } catch (RemoteException e) {
        }
        try {
            ChannelServer.getInstance(myChannel).unbind();
        } catch (Throwable t) {
            log.error("SHUTDOWN ERROR", t);
        }
        boolean allShutdownFinished = true;
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            if (!cserv.hasFinishedShutdown()) {
                allShutdownFinished = false;
            }
        }
        if (allShutdownFinished) {
            TimerManager.getInstance().stop();
            try {
                DatabaseConnection.closeAll();
            } catch (SQLException e) {
                log.error("THROW", e);
            }
            System.exit(0);
        }
    }
}