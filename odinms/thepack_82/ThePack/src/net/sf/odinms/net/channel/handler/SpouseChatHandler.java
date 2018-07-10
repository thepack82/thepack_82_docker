package net.sf.odinms.net.channel.handler;

import java.rmi.RemoteException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class SpouseChatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String recipient = slea.readMapleAsciiString();
        String msg = slea.readMapleAsciiString();
        if (!CommandProcessor.processCommand(c, msg)) {
            if (c.getPlayer().isMarried() == 1) {
                MapleCharacter wife = c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getPartnerId());
                if (wife != null) {
                    wife.getClient().getSession().write(MaplePacketCreator.sendSpouseChat(c.getPlayer(), msg));
                    c.getSession().write(MaplePacketCreator.sendSpouseChat(c.getPlayer(), msg));
                } else {
                    try {
                        if (c.getChannelServer().getWorldInterface().isConnected(wife.getName())) {
                            c.getChannelServer().getWorldInterface().sendSpouseChat(c.getPlayer().getName(), wife.getName(), msg);
                            c.getSession().write(MaplePacketCreator.sendSpouseChat(c.getPlayer(), msg));
                        } else {
                            c.getSession().write(MaplePacketCreator.serverNotice(6, "You are not married or your spouse is currently offline."));
                        }
                    } catch (RemoteException e) {
                        c.getSession().write(MaplePacketCreator.serverNotice(6, "You are not married or your spouse is currently offline."));
                        c.getChannelServer().reconnectWorld();
                    }
                }
            }
        }
    }
}