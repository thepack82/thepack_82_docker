package net.sf.odinms.scripting.npc;

import net.sf.odinms.client.MapleCharacter;

public interface NPCScript {
    public void start();
    public void start(MapleCharacter chr);
    public void action(byte mode, byte type, int selection);
}
