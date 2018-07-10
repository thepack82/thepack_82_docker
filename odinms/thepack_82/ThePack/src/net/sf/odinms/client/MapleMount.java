package net.sf.odinms.client;

import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.tools.MaplePacketCreator;

/**
 *
 * @author Patrick/PurpleMadness
 */
public class MapleMount {

    private int itemid;
    private int skillid;
    private int tiredness;
    private int exp;
    private int level;
    private ScheduledFuture<?> tirednessSchedule;
    private MapleCharacter owner;

    public MapleMount(MapleCharacter owner, int id, int skillid) {
        this.itemid = id;
        this.skillid = skillid;
        this.tiredness = 0;
        this.level = 1;
        this.exp = 0;
        this.owner = owner;
    }

    public int getItemId() {
        return itemid;
    }

    public int getSkillId() {
        return skillid;
    }

    public int getId() {
        switch (this.itemid) {
            case 1902000:
                return 1;
            case 1902001:
                return 2;
            case 1902002:
                return 3;
            case 1932000:
                return 4;
            case 1902008:
            case 1902009:
                return 5; //NX ones
            default:
                return 0;
        }
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void setTiredness(int newtiredness) {
        this.tiredness = newtiredness;
        if (tiredness < 0) {
            tiredness = 0;
        }
    }

    public void increaseTiredness() {
        this.tiredness++;
        owner.getMap().broadcastMessage(MaplePacketCreator.updateMount(owner.getId(), this, false));
        if (tiredness > 100) {
            owner.dispelSkill(1004);
        }
    }

    public void setExp(int newexp) {
        this.exp = newexp;
    }

    public void setLevel(int newlevel) {
        this.level = newlevel;
    }

    public void setItemId(int newitemid) {
        this.itemid = newitemid;
    }

    public void startSchedule() {
        this.tirednessSchedule = TimerManager.getInstance().register(new Runnable() {

            public void run() {
                increaseTiredness();
            }
        }, 60000, 60000);
    }

    public void cancelSchedule() {
        this.tirednessSchedule.cancel(false);
    }
}
