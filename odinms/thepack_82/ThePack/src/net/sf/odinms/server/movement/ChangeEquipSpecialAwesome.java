package net.sf.odinms.server.movement;

import java.awt.Point;
import net.sf.odinms.tools.data.output.LittleEndianWriter;

//WHAT KIND OF NAME IS THIS?
public class ChangeEquipSpecialAwesome implements LifeMovementFragment {
	private int wui;

	public ChangeEquipSpecialAwesome(int wui) {
		this.wui = wui;
	}

	@Override
	public void serialize(LittleEndianWriter lew) {
		lew.write(10);
		lew.write(wui);
	}
	
	@Override
        public Point getPosition() {
            return new Point(0,0);
        }
}
