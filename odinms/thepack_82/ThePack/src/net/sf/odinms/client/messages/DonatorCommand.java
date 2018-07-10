package net.sf.odinms.client.messages;

import java.util.HashMap;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.channel.ChannelServer;

/**
 * @author Moogra
 */
public class DonatorCommand {

    public static boolean executeDonatorCommand(MapleClient c, MessageCallback mc, String line) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        String[] splitted = line.split(" ");
        if (splitted[0].equals("!buffme")) {
            int[] array = {9001000, 9101002, 9101003, 9101004, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i = 0; i < array.length; i++) {
                SkillFactory.getSkill(array[i]).getEffect(SkillFactory.getSkill(array[i]).getMaxLevel()).applyTo(player);
            }
        } else if (splitted[0].equals("!goto")) {
            HashMap<String, Integer> maps = new HashMap<String, Integer>();
            maps.put("southperry", 60000);
            maps.put("amherst", 1010000);
            maps.put("henesys", 100000000);
            maps.put("ellinia", 101000000);
            maps.put("perion", 102000000);
            maps.put("kerning", 103000000);
            maps.put("lith", 104000000);
            maps.put("sleepywood", 105040300);
            maps.put("florina", 110000000);
            maps.put("gmmap", 180000000);
            maps.put("orbis", 200000000);
            maps.put("happy", 209000000);
            maps.put("elnath", 211000000);
            maps.put("ludi", 220000000);
            maps.put("omega", 221000000);
            maps.put("korean", 222000000);
            maps.put("aqua", 230000000);
            maps.put("leafre", 240000000);
            maps.put("mulung", 250000000);
            maps.put("herb", 251000000);
            maps.put("nlc", 600000000);
            maps.put("shrine", 800000000);
            maps.put("showa", 801000000);
            maps.put("fm", 910000000);
            if (maps.containsKey(splitted[1])) {
                player.changeMap(cserv.getMapFactory().getMap(maps.get(splitted[1])), cserv.getMapFactory().getMap(maps.get(splitted[1])).getPortal(0));
            }
        } else if (splitted[0].equals("!online")) {
            StringBuilder builder = new StringBuilder("Characters online: ");
            for (MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()) + ", ");
            }
            builder.setLength(builder.length() - 2);
            mc.dropMessage(builder.toString());
        } else if (splitted[0].equals("!unlog")) {
            player.setLogged();
            mc.dropMessage("Done");
        } else {
            if (c.getPlayer().gmLevel() == 1) {
                mc.dropMessage("Donator Command " + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }
}
