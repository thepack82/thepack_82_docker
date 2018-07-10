package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.server.maps.FakeCharacter;

public class FaceExpressionHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int emote = slea.readInt();
        if (emote > 7) {
            int emoteid = 5159992 + emote;
            if (c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(emoteid)).findById(emoteid) == null) {
                c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(emoteid));
                return;
            }
        }
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.facialExpression(c.getPlayer(), emote), false);
        for (FakeCharacter ch : c.getPlayer().getFakeChars()) {
            c.getPlayer().getMap().broadcastMessage(ch.getFakeChar(), MaplePacketCreator.facialExpression(ch.getFakeChar(), emote), false);
        }
    }
}
