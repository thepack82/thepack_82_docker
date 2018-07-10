package net.sf.odinms.client;

public class MapleKeyBinding {

    private int type,  action;

    public MapleKeyBinding(int type, int action) {
        super();
        this.type = type;
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public int getAction() {
        return action;
    }
}
