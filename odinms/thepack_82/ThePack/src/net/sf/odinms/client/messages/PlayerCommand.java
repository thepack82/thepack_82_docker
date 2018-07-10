package net.sf.odinms.client.messages;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.tools.MaplePacketCreator;

public class PlayerCommand {

    public static boolean executePlayerCommand(MapleClient c, MessageCallback mc, String line) {
        MapleCharacter player = c.getPlayer();
        String[] splitted = line.split(" ");
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("@version") || splitted[0].equals("@revision")) {
            mc.dropMessage("ThePack Revision 82");
        } else if (splitted[0].equals("@help")) {
            mc.dropMessage("============================================================");
            mc.dropMessage("                 " + LoginServer.getInstance().getServerName() + "Player Commands");
            mc.dropMessage("============================================================");
            mc.dropMessage("@checkkarma         - Checks how much karma you have.");
            mc.dropMessage("@dispose            - Use if you're stuck.");
            mc.dropMessage("@emo                - Sets your HP to 0.");
            mc.dropMessage("@expfix             - Fixes negative EXP.");
            mc.dropMessage("@fmnpc              - Shows the All-In-One NPC.");
            if (player.getKarma() > 39) {
                mc.dropMessage("@karma [raise/drop] [user] - Raises or drops someone's karma by using one of your own.");
            }
            mc.dropMessage("@rebirth            - Reborns you at level 200+");
            mc.dropMessage("@save               - Saves your data.");
            mc.dropMessage("@stat [amount]      - Adds [amount] str/dex/int/luk.");
            mc.dropMessage("@warphere [person]  - Warps [person] to your map if your karma is greater than 4.");
        } else if (splitted[0].equals("@checkkarma")) {
            mc.dropMessage("You currently have: " + player.getKarma() + " karma.");
        } else if (splitted[0].equals("@dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            mc.dropMessage("Disposed.");
            c.getSession().write(MaplePacketCreator.enableActions());
        } else if (splitted[0].equals("@emo") && !player.inBlockedMap()) {
            player.setHp(0);
            player.updateSingleStat(MapleStat.HP, 0);
        } else if (splitted[0].equals("@expfix")) {
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, Integer.valueOf(0));
            mc.dropMessage("Your exp has been reset.");
        } else if (splitted[0].equals("@fmnpc") && !player.inBlockedMap()) {
            NPCScriptManager.getInstance().start(c, 22000, null, null);
        } else if (splitted[0].equals("@karma") && player.getKarma() > 39) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
            if (splitted[1].equals("raise") && victim.getKarma() < 25) {
                player.downKarma();
                victim.upKarma();
                mc.dropMessage("You have raised " + victim + "'s karma.");
            } else if (splitted[1].equals("drop") && victim.getKarma() > -25) {
                player.downKarma();
                victim.downKarma();
                mc.dropMessage("You have dropped " + victim + "'s karma.");
            } else {
                mc.dropMessage("Either you didn't use the correct syntax or " + victim + "'s karma is too high or too low.");
            }
        } else if (splitted[0].equals("@rebirth")) {
            if (player.getLevel() > 199) {
                player.doReborn();
            } else {
                mc.dropMessage("You must be at least level 200.");
                player.update();
            }
        } else if (splitted[0].equals("@save")) {
            player.saveToDB(true);
            mc.dropMessage("Saved.");
        } else if (splitted[0].equals("@toggleexp") && splitted[1].equals("@toggleexp")) {
            player.ban("Trying to hack server");
        } else if (splitted[0].equals("@str") || splitted[0].equals("@int") || splitted[0].equals("@luk") || splitted[0].equals("@dex")) {
            int amount = Integer.parseInt(splitted[1]);
            if (amount > 0 && amount <= player.getRemainingAp() && amount < 31997) {
                if (splitted[0].equals("@str") && amount + player.getStr() < 32001) {
                    player.setStr(player.getStr() + amount);
                    player.updateSingleStat(MapleStat.STR, player.getStr());
                } else if (splitted[0].equals("@int") && amount + player.getInt() < 32001) {
                    player.setInt(player.getInt() + amount);
                    player.updateSingleStat(MapleStat.INT, player.getInt());
                } else if (splitted[0].equals("@luk") && amount + player.getLuk() < 32001) {
                    player.setLuk(player.getLuk() + amount);
                    player.updateSingleStat(MapleStat.LUK, player.getLuk());
                } else if (splitted[0].equals("@dex") && amount + player.getDex() < 32001) {
                    player.setDex(player.getDex() + amount);
                    player.updateSingleStat(MapleStat.DEX, player.getDex());
                } else {
                    mc.dropMessage("Make sure the stat you are trying to raise will not be over 32000.");
                }
                player.setRemainingAp(player.getRemainingAp() - amount);
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
            } else {
                mc.dropMessage("Please make sure your AP is not over 32000 and you have enough to distribute.");
            }
        } else if (splitted[0].equals("@warphere") && player.getKarma() > 4) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (player.isBuddy(MapleCharacter.getIdByName(splitted[1], victim.getWorld())) && !victim.isGM()) {
                victim.changeMap(player.getMap(), player.getMap().findClosestSpawnpoint(player.getPosition()));
            } else {
                mc.dropMessage("Either " + victim + " is not your buddy, or " + victim + " is a GM.");
            }
        } else {
            mc.dropMessage("Player Command " + splitted[0] + " does not exist.");
            return false;
        }
        return true;
    }
}