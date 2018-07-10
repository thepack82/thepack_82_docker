package net.sf.odinms.net.channel.handler;

import java.util.Collection;
import java.rmi.RemoteException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class WhisperHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        if (mode == 6) {
            String recipient = slea.readMapleAsciiString();
            String text = slea.readMapleAsciiString();
            if (!CommandProcessor.processCommand(c, text)) {
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    player.getClient().getSession().write(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                    c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                } else {
                    try {
                        if (ChannelServer.getInstance(c.getChannel()).getWorldInterface().isConnected(recipient)) {
                            ChannelServer.getInstance(c.getChannel()).getWorldInterface().whisper(
                                    c.getPlayer().getName(), recipient, c.getChannel(), text);
                            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                        } else {
                            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                        }
                    } catch (RemoteException e) {
                        c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                        c.getChannelServer().reconnectWorld();
                    }
                }
            }
        } else if (mode == 5) {
            String recipient = slea.readMapleAsciiString();
            MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
            if (player != null && (c.getPlayer().isGM() || !player.isHidden())) {
                if (player.inCS()) {
                    c.getSession().write(MaplePacketCreator.getFindReplyWithCS(player.getName()));
                } else {
                    c.getSession().write(MaplePacketCreator.getFindReplyWithMap(player.getName(), player.getMap().getId()));
                }
            } else {
                Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
                for (ChannelServer cserv : cservs) {
                    player = cserv.getPlayerStorage().getCharacterByName(recipient);
                    if (player != null) {
                        break;
                    }
                }
                if (player != null) {
                    c.getSession().write(MaplePacketCreator.getFindReply(player.getName(), (byte) player.getClient().getChannel()));
                } else {
                    c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                }
            }
        }
    }
}
