package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.scripting.quest.QuestScriptManager;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
public class NPCTalkHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int oid = slea.readInt();
		slea.readInt(); // dont know
		MapleNPC npc = (MapleNPC) c.getPlayer().getMap().getMapObject(oid);
		if (npc.hasShop()) {
			if (c.getPlayer().getShop() != null) {
				c.getPlayer().setShop(null);
				c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 20));
			}
			npc.sendShop(c);
		} else {
			if (c.getCM() != null) NPCScriptManager.getInstance().dispose(c);
			if (c.getQM() != null) QuestScriptManager.getInstance().dispose(c);
			NPCScriptManager.getInstance().start(c, npc.getId());
		}
	}
}
