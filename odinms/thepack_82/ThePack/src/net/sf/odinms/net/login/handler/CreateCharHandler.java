package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleSkinColor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CreateCharHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        int face = slea.readInt();
        int hair = slea.readInt();
        int hairColor = slea.readInt();
        int skinColor = slea.readInt();
        int top = slea.readInt();
        int bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();
        int gender = slea.readByte();
        int str = slea.readByte();
        int dex = slea.readByte();
        int _int = slea.readByte();
        int luk = slea.readByte();
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        if (c.isGm()) {
            newchar.setGMLevel(c.getGMLevel());
        }
        newchar.setWorld(c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair + hairColor);
        newchar.setGender(gender);
        newchar.setStr(str);
        newchar.setDex(dex);
        newchar.setInt(_int);
        newchar.setLuk(luk);
        newchar.setName(name);
        newchar.setSkinColor(MapleSkinColor.getById(skinColor));
        MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        IItem eq_top = MapleItemInformationProvider.getInstance().getEquipById(top);
        eq_top.setPosition((byte) -5);
        equip.addFromDB(eq_top);
        IItem eq_bottom = MapleItemInformationProvider.getInstance().getEquipById(bottom);
        eq_bottom.setPosition((byte) -6);
        equip.addFromDB(eq_bottom);
        IItem eq_shoes = MapleItemInformationProvider.getInstance().getEquipById(shoes);
        eq_shoes.setPosition((byte) -7);
        equip.addFromDB(eq_shoes);
        IItem eq_weapon = MapleItemInformationProvider.getInstance().getEquipById(weapon);
        eq_weapon.setPosition((byte) -11);
        equip.addFromDB(eq_weapon);
        MapleInventory etc = newchar.getInventory(MapleInventoryType.ETC);
        etc.addItem(new Item(4161001, (byte) 0, (short) 1));
        boolean charok = true;
        if (str + dex + _int + luk != 25 || str < 4 || dex < 4 || _int < 4 || luk < 4) {
            charok = false;
        }
        if (gender == 0) {
            if ((bottom != 1060006 && bottom != 1060002)||(top != 1040002 && top != 1040006 && top != 1040010) || (face != 20000 && face != 20001 && face != 20002)||(hair != 30000 && hair != 30020 && hair != 30030)) {
                charok = false;
            }
        } else if (gender == 1) {
            if ((bottom != 1061002 && bottom != 1061008)||(face != 21000 && face != 21001 && face != 21002)||(hair != 31000 && hair != 31040 && hair != 31050)||(top != 1041002 && top != 1041006 && top != 1041010 && top != 1041011)) {
                charok = false;
            }
        } else {
            charok = false;
        }
        if ((hairColor != 0 && hairColor != 2 && hairColor != 3 && hairColor != 7)||(shoes != 1072001 && shoes != 1072005 && shoes != 1072037 && shoes != 1072038)||(weapon != 1302000 && weapon != 1322005 && weapon != 1312004)|| (skinColor < 0 || skinColor > 3)) {
            charok = false;
        }
        if (charok && MapleCharacterUtil.canCreateChar(name, c.getWorld())) {
            newchar.saveToDB(false);
            c.getSession().write(MaplePacketCreator.addNewCharEntry(newchar, charok));
        }
    }
}
