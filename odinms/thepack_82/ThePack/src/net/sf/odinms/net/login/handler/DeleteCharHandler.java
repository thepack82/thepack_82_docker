package net.sf.odinms.net.login.handler;

import java.util.Calendar;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int idate = slea.readInt();
        int cid = slea.readInt();
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        int day = idate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, day);
        boolean shallDelete = c.checkBirthDate(cal);
        int state = 0x12;
        if (shallDelete) {
            state = 0;
            if (!c.deleteCharacter(cid)) {
                state++;
            }
        }
        c.getSession().write(MaplePacketCreator.deleteCharResponse(cid, state));
    }
}
