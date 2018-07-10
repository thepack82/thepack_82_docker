package net.sf.odinms.server.maps;

import java.awt.Point;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.tools.MaplePacketCreator;

/**
 *
 * @author Jan
 */
public class MapleSummon extends AbstractAnimatedMapleMapObject {

    private MapleCharacter owner;
    private int skillLevel;
    private int skill;
    private int hp;
    private SummonMovementType movementType;

    public MapleSummon(MapleCharacter owner, int skill, Point pos, SummonMovementType movementType) {
        super();
        this.owner = owner;
        this.skill = skill;
        this.skillLevel = owner.getSkillLevel(SkillFactory.getSkill(skill));
        if (skillLevel == 0) {
            throw new RuntimeException("Trying to create a summon for a char without the skill");
        }
        this.movementType = movementType;
        setPosition(pos);
    }

    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnSpecialMapObject(this, skillLevel, false));
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removeSpecialMapObject(this, false));
    }

    public MapleCharacter getOwner() {
        return this.owner;
    }

    public int getSkill() {
        return this.skill;
    }

    public int getHP() {
        return this.hp;
    }

    public void addHP(int delta) {
        this.hp += delta;
    }

    public SummonMovementType getMovementType() {
        return movementType;
    }

    public boolean isPuppet() {
        return (skill == 3111002 || skill == 3211002 || skill == 5211001);
    }

    public boolean isSummon() {
        return skill == 2311006 || skill == 2321003 || skill == 2121005 || skill == 2221005 || skill == 5211001 || skill == 5211002 || skill == 5220002;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }
}