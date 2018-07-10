package net.sf.odinms.server.maps;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleInventoryType;

/*
 * @author Patrick/PurpleMadness
 */
public class FakeCharacter {

    private MapleCharacter ch;

    public FakeCharacter(MapleCharacter player, int id) {
        MapleCharacter fakechr = new MapleCharacter();
        fakechr.setHair(player.getHair());
        fakechr.setFace(player.getFace());
        fakechr.setSkinColor(player.getSkinColor());
        fakechr.setName(player.getName());
        fakechr.setID(id + 100000);
        fakechr.setLevel(player.getLevel());
        fakechr.setJob(player.getJob().getId());
        fakechr.setMap(player.getMap());
        fakechr.setPosition(player.getPosition());
        fakechr.silentGiveBuffs(player.getAllBuffs());
        for (IItem equip : player.getInventory(MapleInventoryType.EQUIPPED)) {
            fakechr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }
        fakechr.isfake = true;
        player.getMap().addBotPlayer(fakechr);
        ch = fakechr;
    }

    public MapleCharacter getFakeChar() {
        return ch;
    }
}