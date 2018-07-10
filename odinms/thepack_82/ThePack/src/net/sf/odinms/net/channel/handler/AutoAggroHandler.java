package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class AutoAggroHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(oid);
        if (monster != null && monster.getController() != null) {
            if (!monster.isControllerHasAggro()) {
                if (c.getPlayer().getMap().getCharacterById(monster.getController().getId()) == null) {
                    monster.switchController(c.getPlayer(), true);
                } else {
                    monster.switchController(monster.getController(), true);
                }
            } else {
                if (c.getPlayer().getMap().getCharacterById(monster.getController().getId()) == null) {
                    monster.switchController(c.getPlayer(), true);
                }
            }
        } else if (monster != null && monster.getController() == null) {
            monster.switchController(c.getPlayer(), true);
        }
    }
}