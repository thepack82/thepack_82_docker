package net.sf.odinms.client.anticheat;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.StringUtil;

public class CheatTracker {

    private Map<CheatingOffense, CheatingOffenseEntry> offenses = Collections.synchronizedMap(new LinkedHashMap<CheatingOffense, CheatingOffenseEntry>());
    private WeakReference<MapleCharacter> chr;
    private long regenHPSince,  regenMPSince,  lastAttackTime,  lastDamage,  takingDamageSince,  lastDamageTakenTime,  summonSummonTime,  attackingSince,  lastSmegaTime;
    private int numSequentialDamage,  numSequentialSummonAttack,  numSameDamage,  sMegaSpamCount,  monsterMoveCount,  attacksWithoutHit,  numHPRegens,  numMPRegens,  numSequentialAttacks;
    private Point lastMonsterMove;
    private Boolean pickupComplete = Boolean.TRUE;
    private ScheduledFuture<?> invalidationTask;

    public CheatTracker(MapleCharacter chr) {
        this.chr = new WeakReference<MapleCharacter>(chr);
        invalidationTask = TimerManager.getInstance().register(new InvalidationTask(), 60000);
        takingDamageSince = attackingSince = regenMPSince = regenHPSince = System.currentTimeMillis();
    }

    public synchronized void checkSMega() {
        long oldLastSmegaTime = lastSmegaTime;
        lastSmegaTime = System.currentTimeMillis();
        if (lastSmegaTime - oldLastSmegaTime > 3000) {
            sMegaSpamCount = 0;
        } else {
            sMegaSpamCount++;
        }
        if (sMegaSpamCount > 3 && !(chr.get().isGM())) {
            chr.get().ban("Megaphone Spam");
        }
    }

    public boolean checkAttack(int skillId) {
        numSequentialAttacks++;
        long oldLastAttackTime = lastAttackTime;
        lastAttackTime = System.currentTimeMillis();
        long attackTime = lastAttackTime - attackingSince;
        if (numSequentialAttacks > 3) {
            final int divisor;
            if (skillId == 3121004 || skillId == 5221004) { // hurricane
                divisor = 30;
            } else {
                divisor = 300;
            }
            if (attackTime / divisor < numSequentialAttacks) {
                registerOffense(CheatingOffense.FASTATTACK);
                return false;
            }
        }
        if (lastAttackTime - oldLastAttackTime > 1500) {
            attackingSince = lastAttackTime;
            numSequentialAttacks = 0;
        }
        return true;
    }

    public void checkTakeDamage() {
        numSequentialDamage++;
        long oldLastDamageTakenTime = lastDamageTakenTime;
        lastDamageTakenTime = System.currentTimeMillis();
        long timeBetweenDamage = lastDamageTakenTime - takingDamageSince;
        if (timeBetweenDamage / 500 < numSequentialDamage) {
            registerOffense(CheatingOffense.FAST_TAKE_DAMAGE);
        }
        if (lastDamageTakenTime - oldLastDamageTakenTime > 4500) {
            takingDamageSince = lastDamageTakenTime;
            numSequentialDamage = 0;
        }
    }

    public int checkDamage(long dmg) {
        if (dmg > 1 && lastDamage == dmg) {
            numSameDamage++;
        } else {
            lastDamage = dmg;
            numSameDamage = 0;
        }
        return numSameDamage;
    }

    public void checkMoveMonster(Point pos) {
        if (pos.equals(lastMonsterMove)) {
            monsterMoveCount++;
            if (monsterMoveCount > 15) {
                registerOffense(CheatingOffense.MOVE_MONSTERS);
            }
        } else {
            lastMonsterMove = pos;
            monsterMoveCount = 1;
        }
    }

    public boolean checkHPRegen() {
        numHPRegens++;
        if ((System.currentTimeMillis() - regenHPSince) / 10000 < numHPRegens) {
            registerOffense(CheatingOffense.FAST_HP_REGEN);
            return false;
        }
        return true;
    }

    public void resetHPRegen() {
        regenHPSince = System.currentTimeMillis();
        numHPRegens = 0;
    }

    public boolean checkMPRegen() {
        numMPRegens++;
        long allowedRegens = (System.currentTimeMillis() - regenMPSince) / 10000;
        if (allowedRegens < numMPRegens) {
            registerOffense(CheatingOffense.FAST_MP_REGEN);
            return false;
        }
        return true;
    }

    public void resetMPRegen() {
        regenMPSince = System.currentTimeMillis();
        numMPRegens = 0;
    }

    public void resetSummonAttack() {
        summonSummonTime = System.currentTimeMillis();
        numSequentialSummonAttack = 0;
    }

    public boolean checkSummonAttack() {
        numSequentialSummonAttack++;
        long allowedAttacks = (System.currentTimeMillis() - summonSummonTime) / 2000 + 1;
        if (allowedAttacks < numSequentialAttacks) {
            registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
            return false;
        }
        return true;
    }

    public void checkPickupAgain() {
        synchronized (pickupComplete) {
            if (pickupComplete) {
                pickupComplete = Boolean.FALSE;
            } else {
                registerOffense(CheatingOffense.TUBI);
            }
        }
    }

    public void pickupComplete() {
        synchronized (pickupComplete) {
            pickupComplete = Boolean.TRUE;
        }
    }

    public int getAttacksWithoutHit() {
        return attacksWithoutHit;
    }

    public void setAttacksWithoutHit(int attacksWithoutHit) {
        this.attacksWithoutHit = attacksWithoutHit;
    }

    public void registerOffense(CheatingOffense offense) {
        registerOffense(offense, null);
    }

    public void registerOffense(CheatingOffense offense, String param) {
        MapleCharacter chrhardref = chr.get();
        if (chrhardref == null || !offense.isEnabled()) {
            return;
        }
        CheatingOffenseEntry entry = offenses.get(offense);
        if (entry != null && entry.isExpired()) {
            expireEntry(entry);
            entry = null;
        }
        if (entry == null) {
            entry = new CheatingOffenseEntry(offense, chrhardref);
        }
        if (param != null) {
            entry.setParam(param);
        }
        entry.incrementCount();
        if (offense.shouldAutoban(entry.getCount())) {
            AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
        }
        offenses.put(offense, entry);
        CheatingOffensePersister.getInstance().persistEntry(entry);
    }

    public void expireEntry(CheatingOffenseEntry coe) {
        offenses.remove(coe.getOffense());
    }

    public int getPoints() {
        int ret = 0;
        CheatingOffenseEntry[] offenses_copy;
        synchronized (offenses) {
            offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
        }
        for (CheatingOffenseEntry entry : offenses_copy) {
            if (entry.isExpired()) {
                expireEntry(entry);
            } else {
                ret += entry.getPoints();
            }
        }
        return ret;
    }

    public Map<CheatingOffense, CheatingOffenseEntry> getOffenses() {
        return Collections.unmodifiableMap(offenses);
    }

    public String getSummary() {
        StringBuilder ret = new StringBuilder();
        List<CheatingOffenseEntry> offenseList = new ArrayList<CheatingOffenseEntry>();
        synchronized (offenses) {
            for (CheatingOffenseEntry entry : offenses.values()) {
                if (!entry.isExpired()) {
                    offenseList.add(entry);
                }
            }
        }
        Collections.sort(offenseList, new Comparator<CheatingOffenseEntry>() {

            @Override
            public int compare(CheatingOffenseEntry o1, CheatingOffenseEntry o2) {
                int thisVal = o1.getPoints();
                int anotherVal = o2.getPoints();
                return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
            }
        });
        int to = Math.min(offenseList.size(), 4);
        for (int x = 0; x < to; x++) {
            ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).getOffense().name()) + ": " + offenseList.get(x).getCount());
            if (x != to - 1) {
                ret.append(" ");
            }
        }
        return ret.toString();
    }

    public void dispose() {
        invalidationTask.cancel(false);
    }

    private class InvalidationTask implements Runnable {

        @Override
        public void run() {
            CheatingOffenseEntry[] offenses_copy;
            synchronized (offenses) {
                offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
            }
            for (CheatingOffenseEntry offense : offenses_copy) {
                if (offense.isExpired()) {
                    expireEntry(offense);
                }
            }
            if (chr.get() == null) {
                dispose();
            }
        }
    }
}