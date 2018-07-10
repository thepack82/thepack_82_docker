package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class GeneralchatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (!player.getCanTalk()) {
            player.dropMessage("Your chat has been disabled");
            return;
        }
        String text = slea.readMapleAsciiString();
        if (!CommandProcessor.processCommand(c, text)) {
            player.getMap().broadcastMessage(MaplePacketCreator.getChatText(player.getId(), text, player.gmLevel() > 2 && player.getGMChat() && c.getChannelServer().allowGmWhiteText(), slea.readByte()));
        }
    }
}