package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.server.maps.MapleSummon;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import java.util.Iterator;

public class DamageSummonHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); //Bugged? might not be skillid.
        int unkByte = slea.readByte();
        int damage = slea.readInt();
        int monsterIdFrom = slea.readInt();
        slea.readByte(); // stance
        Iterator<MapleSummon> iter = c.getPlayer().getSummons().values().iterator();
        while (iter.hasNext()) {
            MapleSummon summon = iter.next();
            if (summon.isPuppet() && summon.getOwner() == c.getPlayer()) { //We can only have one puppet(AFAIK O.O) so this check is safe.
                summon.addHP(-damage);
                if (summon.getHP() <= 0) {
                    c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
                }
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.damageSummon(c.getPlayer().getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getPosition());
                break;
            }
        }
    }
}