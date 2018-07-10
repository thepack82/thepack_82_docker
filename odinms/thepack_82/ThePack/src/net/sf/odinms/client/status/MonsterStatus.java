package net.sf.odinms.client.status;

import java.io.Serializable;
import net.sf.odinms.net.IntValueHolder;

public enum MonsterStatus implements IntValueHolder, Serializable {

    WATK(0x1),
    WDEF(0x2),
    MATK(0x4),
    MDEF(0x8),
    ACC(0x10),
    AVOID(0x20),
    SPEED(0x40),
    STUN(0x80), //this is possibly only the bowman stun
    FREEZE(0x100),
    POISON(0x200),
    SEAL(0x400),
    SHOWDOWN(0x800),
    WEAPON_ATTACK_UP(0x1000),
    WEAPON_DEFENSE_UP(0x2000),
    MAGIC_ATTACK_UP(0x4000),
    MAGIC_DEFENSE_UP(0x8000),
    DOOM(0x10000),
    SHADOW_WEB(0x20000),
    WEAPON_IMMUNITY(0x40000),
    MAGIC_IMMUNITY(0x80000);
    static final long serialVersionUID = 0L;
    private final int i;

    private MonsterStatus(int i) {
        this.i = i;
    }

    @Override
    public int getValue() {
        return i;
    }
}
