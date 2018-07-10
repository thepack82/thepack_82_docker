package net.sf.odinms.net.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.maps.AnimatedMapleMapObject;
import net.sf.odinms.server.movement.AbsoluteLifeMovement;
import net.sf.odinms.server.movement.ChairMovement;
import net.sf.odinms.server.movement.ChangeEquipSpecialAwesome;
import net.sf.odinms.server.movement.JumpDownMovement;
import net.sf.odinms.server.movement.LifeMovement;
import net.sf.odinms.server.movement.LifeMovementFragment;
import net.sf.odinms.server.movement.RelativeLifeMovement;
import net.sf.odinms.server.movement.TeleportMovement;
import net.sf.odinms.tools.data.input.LittleEndianAccessor;

public abstract class AbstractMovementPacketHandler extends AbstractMaplePacketHandler {

    protected List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea) {
        List<LifeMovementFragment> res = new ArrayList<LifeMovementFragment>();
        int numCommands = lea.readByte();
        for (int i = 0; i < numCommands; i++) {
            int command = lea.readByte();
            switch (command) {
                case 0: // normal move
                case 5:
                case 17: // Float
                {
                    int xpos = lea.readShort();
                    int ypos = lea.readShort();
                    int xwobble = lea.readShort();
                    int ywobble = lea.readShort();
                    int unk = lea.readShort();
                    int newstate = lea.readByte();
                    int duration = lea.readShort();
                    AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 6: // fj
                case 12:
                case 13: // Shot-jump-back thing
                case 16: // Float
                {
                    int xmod = lea.readShort();
                    int ymod = lea.readShort();
                    int newstate = lea.readByte();
                    int duration = lea.readShort();
                    RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);
                    res.add(rlm);
                    break;
                }
                case 3:
                case 4: // tele... -.-
                case 7: // assaulter
                case 8: // assassinate
                case 9: // rush
                case 14: {
                    int xpos = lea.readShort();
                    int ypos = lea.readShort();
                    int xwobble = lea.readShort();
                    int ywobble = lea.readShort();
                    int newstate = lea.readByte();
                    TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
                    tm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(tm);
                    break;
                }
                case 10: // change equip ???
                {
                    res.add(new ChangeEquipSpecialAwesome(lea.readByte()));
                    break;
                }
                case 11: // chair
                {
                    int xpos = lea.readShort();
                    int ypos = lea.readShort();
                    int unk = lea.readShort();
                    int newstate = lea.readByte();
                    int duration = lea.readShort();
                    ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setUnk(unk);
                    res.add(cm);
                    break;
                }
                case 15: {
                    int xpos = lea.readShort();
                    int ypos = lea.readShort();
                    int xwobble = lea.readShort();
                    int ywobble = lea.readShort();
                    int unk = lea.readShort();
                    int fh = lea.readShort();
                    int newstate = lea.readByte();
                    int duration = lea.readShort();
                    JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
                    jdm.setUnk(unk);
                    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    jdm.setFH(fh);
                    res.add(jdm);
                    break;
                }
                default:
                    return null;
            }
        }
        return res;
    }

    protected void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}