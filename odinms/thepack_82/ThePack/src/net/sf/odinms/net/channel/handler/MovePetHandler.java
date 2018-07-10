package net.sf.odinms.net.channel.handler;

import java.awt.Point;
import java.util.List;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.server.movement.LifeMovementFragment;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.data.input.StreamUtil;

public class MovePetHandler extends AbstractMovementPacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MovePetHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int petId = slea.readInt();
		slea.readInt();
		Point startPos = StreamUtil.readShortPoint(slea);
		List<LifeMovementFragment> res = parseMovement(slea);
		MapleCharacter player = c.getPlayer();
		int slot = player.getPetIndex(petId);
		if (player.inCS() && slot == -1) {
			return;
		} else if (slot == -1) {
			log.warn("[h4x] {} ({}) trying to move a pet he/she does not own.", c.getPlayer().getName(), c.getPlayer().getId());
			return;
		}
		player.getPet(slot).updatePosition(res); 
		player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player.getId(), petId, slot, res), false);
	}
}