package net.sf.odinms.net.channel.handler;

import java.util.Random;

import net.sf.odinms.client.ExpTable;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PetFoodHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (c.getPlayer().getNoPets() == 0) {
			return;
        }
		int previousFullness = 100;
		int slot = 0;
		MaplePet[] pets = c.getPlayer().getPets();
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				if (pets[i].getFullness() < previousFullness) {
					slot = i;
					previousFullness = pets[i].getFullness();
				}
			}
		}
		MaplePet pet = c.getPlayer().getPet(slot);
		slea.readInt();
		slea.readShort();
		int itemId = slea.readInt();
		boolean gainCloseness = false;
		Random rand = new Random();
		int random = rand.nextInt(101);
		if (random <= 50) {
			gainCloseness = true;
		}
		if (pet.getFullness() < 100) {
			int newFullness = pet.getFullness() + 30;
			if (newFullness > 100) {
				newFullness = 100;
			}
			pet.setFullness(newFullness);
			if (gainCloseness && pet.getCloseness() < 30000) {
				int newCloseness = pet.getCloseness() + (1 * c.getChannelServer().getPetExpRate());
				if (newCloseness > 30000) {
				    newCloseness = 30000;
				}
				pet.setCloseness(newCloseness);
				if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
					pet.setLevel(pet.getLevel() + 1);
					c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
					c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
				}
			}
			c.getSession().write(MaplePacketCreator.updatePet(pet, true));
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), (byte) 1, slot, true, true), true);
		} else {
			if (gainCloseness) {
				int newCloseness = pet.getCloseness() - (1 * c.getChannelServer().getPetExpRate());
				if (newCloseness < 0) {
				    newCloseness = 0;
				}
				pet.setCloseness(newCloseness);
				if (newCloseness < ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
					pet.setLevel(pet.getLevel() - 1);
				}
			}
			c.getSession().write(MaplePacketCreator.updatePet(pet, true));
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), (byte) 1, slot, false, true), true);
		}
		MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
	}
}
