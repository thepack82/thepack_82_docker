package net.sf.odinms.client.status;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import net.sf.odinms.client.ISkill;
import net.sf.odinms.tools.ArrayMap;

public class MonsterStatusEffect {

    private Map<MonsterStatus, Integer> stati;
    private ISkill skill;
    private boolean monsterSkill;
    private ScheduledFuture<?> cancelTask;
    private ScheduledFuture<?> poisonSchedule;

    public MonsterStatusEffect(Map<MonsterStatus, Integer> stati, ISkill skillId, boolean monsterSkill) {
        this.stati = new ArrayMap<MonsterStatus, Integer>(stati);
        this.skill = skillId;
        this.monsterSkill = monsterSkill;
    }

    public Map<MonsterStatus, Integer> getStati() {
        return stati;
    }

    public Integer setValue(MonsterStatus status, Integer newVal) {
        return stati.put(status, newVal);
    }

    public ISkill getSkill() {
        return skill;
    }

    public boolean isMonsterSkill() {
        return monsterSkill;
    }

    public ScheduledFuture<?> getCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(ScheduledFuture<?> cancelTask) {
        this.cancelTask = cancelTask;
    }

    public void removeActiveStatus(MonsterStatus stat) {
        stati.remove(stat);
    }

    public void setPoisonSchedule(ScheduledFuture<?> poisonSchedule) {
        this.poisonSchedule = poisonSchedule;
    }

    public void cancelPoisonSchedule() {
        if (poisonSchedule != null) {
            poisonSchedule.cancel(false);
        }
    }
}