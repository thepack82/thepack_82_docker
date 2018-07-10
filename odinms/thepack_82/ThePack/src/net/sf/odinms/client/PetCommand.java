package net.sf.odinms.client;

/*
 * @author Danny (Leifde)
 */
public class PetCommand {

    private int petId ,skillId, prob, inc;

    public PetCommand(int petId, int skillId, int prob, int inc) {
        this.petId = petId;
        this.skillId = skillId;
        this.prob = prob;
        this.inc = inc;
    }

    public int getPetId() {
        return petId;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getProbability() {
        return prob;
    }

    public int getIncrease() {
        return inc;
    }
}
