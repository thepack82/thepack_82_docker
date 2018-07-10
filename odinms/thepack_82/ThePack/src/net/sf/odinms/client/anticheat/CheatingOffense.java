package net.sf.odinms.client.anticheat;

public enum CheatingOffense {

    FASTATTACK(1, 60000, 300),
    MOVE_MONSTERS,
    TUBI,
    FAST_HP_REGEN,
    FAST_MP_REGEN(1, 60000, 50000),
    SAME_DAMAGE(10, 300000, 20),
    ATTACK_WITHOUT_GETTING_HIT,
    HIGH_DAMAGE(1),
    ATTACK_FARAWAY_MONSTER(5),
    REGEN_HIGH_HP(50),
    REGEN_HIGH_MP(50),
    ITEMVAC(5),
    SHORT_ITEMVAC(2),
    USING_FARAWAY_PORTAL(30, 300000),
    FAST_TAKE_DAMAGE(1),
    FAST_MOVE(1, 60000),
    HIGH_JUMP(1, 60000),
    MISMATCHING_BULLETCOUNT(50),
    ETC_EXPLOSION(50, 300000),
    FAST_SUMMON_ATTACK,
    ATTACKING_WHILE_DEAD(10, 300000),
    USING_UNAVAILABLE_ITEM(10, 300000),
    FAMING_SELF(10, 300000),
    FAMING_UNDER_15(10, 300000),
    EXPLODING_NONEXISTANT,
    SUMMON_HACK,
    HEAL_ATTACKING_UNDEAD(1, 60000, 5),
    COOLDOWN_HACK(10, 300000),
    MOB_INSTANT_DEATH_HACK(10, 300000, 5),;
    private final int points;
    private final long validityDuration;
    private final int autobancount;
    private boolean enabled = true;

    public int getPoints() {
        return points;
    }

    public long getValidityDuration() {
        return validityDuration;
    }

    public boolean shouldAutoban(int count) {
        if (autobancount == -1) {
            return false;
        }
        return count > autobancount;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private CheatingOffense() {
        this(1);
    }

    private CheatingOffense(int points) {
        this(points, 60000);
    }

    private CheatingOffense(int points, long validityDuration) {
        this(points, validityDuration, -1);
    }

    private CheatingOffense(int points, long validityDuration, int autobancount) {
        this(points, validityDuration, autobancount, true);
    }

    private CheatingOffense(int points, long validityDuration, int autobancount, boolean enabled) {
        this.points = points;
        this.validityDuration = validityDuration;
        this.autobancount = autobancount;
        this.enabled = enabled;
    }
}