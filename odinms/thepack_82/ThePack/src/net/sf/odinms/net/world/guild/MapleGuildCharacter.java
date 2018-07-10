package net.sf.odinms.net.world.guild;

import net.sf.odinms.client.MapleCharacter;

public class MapleGuildCharacter implements java.io.Serializable {

    private int level,  id,  channel,  jobid,  guildrank,  guildid;
    private boolean online;
    private String name;

    public MapleGuildCharacter(MapleCharacter c) {
        name = c.getName();
        level = c.getLevel();
        id = c.getId();
        channel = c.getClient().getChannel();
        jobid = c.getJob().getId();
        guildrank = c.getGuildRank();
        guildid = c.getGuildId();
        online = true;
    }

    public MapleGuildCharacter(int _id, int _lv, String _name, int _channel, int _job, int _rank, int _gid, boolean _on) {
        level = _lv;
        id = _id;
        name = _name;
        if (_on) {
            channel = _channel;
        }
        jobid = _job;
        online = _on;
        guildrank = _rank;
        guildid = _gid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int l) {
        level = l;
    }

    public int getId() {
        return id;
    }

    public void setChannel(int ch) {
        channel = ch;
    }

    public int getChannel() {
        return channel;
    }

    public int getJobId() {
        return jobid;
    }

    public void setJobId(int job) {
        jobid = job;
    }

    public int getGuildId() {
        return guildid;
    }

    public void setGuildId(int gid) {
        guildid = gid;
    }

    public void setGuildRank(int rank) {
        guildrank = rank;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public boolean isOnline() {
        return online;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MapleGuildCharacter)) {
            return false;
        }
        MapleGuildCharacter o = (MapleGuildCharacter) other;
        return (o.getId() == id && o.getName().equals(name));
    }

    public void setOnline(boolean f) {
        online = f;
    }
}
