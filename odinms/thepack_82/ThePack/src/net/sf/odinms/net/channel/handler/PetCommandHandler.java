package net.sf.odinms.net.channel.handler;

import java.util.Random;

import net.sf.odinms.client.ExpTable;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.PetCommand;
import net.sf.odinms.client.PetDataFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PetCommandHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        int petIndex = c.getPlayer().getPetIndex(petId);
        MaplePet pet = null;
        if (petIndex == -1) {
            return;
        } else {
            pet = c.getPlayer().getPet(petIndex);
        }
        slea.readInt();
        slea.readByte();
        byte command = slea.readByte();
        PetCommand petCommand = PetDataFactory.getPetCommand(pet.getItemId(), (int) command);
        boolean success = false;
        Random rand = new Random();
        if (rand.nextInt(101) <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + (petCommand.getIncrease() * c.getChannelServer().getPetExpRate());
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
                }
                c.getSession().write(MaplePacketCreator.updatePet(pet, true));
            }
        }
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), command, petIndex, success, false), true);
    }
}
