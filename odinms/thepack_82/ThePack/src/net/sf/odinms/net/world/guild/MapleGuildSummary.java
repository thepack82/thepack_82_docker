package net.sf.odinms.net.world.guild;

public class MapleGuildSummary implements java.io.Serializable {

    private String name;
    private short logoBG;
    private byte logoBGColor;
    private short logo;
    private byte logoColor;

    public MapleGuildSummary(MapleGuild g) {
        name = g.getName();
        logoBG = (short) g.getLogoBG();
        logoBGColor = (byte) g.getLogoBGColor();
        logo = (short) g.getLogo();
        logoColor = (byte) g.getLogoColor();
    }

    public String getName() {
        return name;
    }

    public short getLogoBG() {
        return logoBG;
    }

    public byte getLogoBGColor() {
        return logoBGColor;
    }

    public short getLogo() {
        return logo;
    }

    public byte getLogoColor() {
        return logoColor;
    }
}
