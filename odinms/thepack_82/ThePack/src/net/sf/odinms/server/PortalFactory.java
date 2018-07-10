package net.sf.odinms.server;

import java.awt.Point;

import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.server.maps.MapleGenericPortal;
import net.sf.odinms.server.maps.MapleMapPortal;

public class PortalFactory {

    private int nextDoorPortal;

    public PortalFactory() {
        nextDoorPortal = 0x80;
    }

    public MaplePortal makePortal(int type, MapleData portal) {
        MapleGenericPortal ret = null;
        if (type == MaplePortal.MAP_PORTAL) {
            ret = new MapleMapPortal();
        } else {
            ret = new MapleGenericPortal(type);
        }
        loadPortal(ret, portal);
        return ret;
    }

    private void loadPortal(MapleGenericPortal myPortal, MapleData portal) {
        myPortal.setName(MapleDataTool.getString(portal.getChildByPath("pn")));
        myPortal.setTarget(MapleDataTool.getString(portal.getChildByPath("tn")));
        myPortal.setTargetMapId(MapleDataTool.getInt(portal.getChildByPath("tm")));
        int x = MapleDataTool.getInt(portal.getChildByPath("x"));
        int y = MapleDataTool.getInt(portal.getChildByPath("y"));
        myPortal.setPosition(new Point(x, y));
        String script = MapleDataTool.getString("script", portal, null);
        if (script != null && script.equals("")) {
            script = null;
        }
        myPortal.setScriptName(script);
        if (myPortal.getType() == MaplePortal.DOOR_PORTAL) {
            myPortal.setId(nextDoorPortal);
            nextDoorPortal++;
        } else {
            myPortal.setId(Integer.parseInt(portal.getName()));
        }
    }
}
