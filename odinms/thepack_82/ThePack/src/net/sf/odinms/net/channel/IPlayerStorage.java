package net.sf.odinms.net.channel;

import java.util.Collection;
import net.sf.odinms.client.MapleCharacter;

public interface IPlayerStorage {
	public MapleCharacter getCharacterByName(String name);
	public MapleCharacter getCharacterById(int id);
	Collection<MapleCharacter> getAllCharacters();
}
