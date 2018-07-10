package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.net.login.LoginWorker;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.KoreanDateUtil;
import java.util.Calendar;
import net.sf.odinms.client.AutoRegister;

public class LoginPasswordHandler implements MaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();

        c.setAccountName(login);

        int loginok = 0;
        boolean ipBan = c.hasBannedIP();
        boolean macBan = c.hasBannedMac();
        if (AutoRegister.getAccountExists(login) != false) {
            loginok = c.login(login, pwd, ipBan || macBan);
        } else if (AutoRegister.autoRegister != false && (!ipBan || !macBan)) {
            AutoRegister.createAccount(login, pwd, c.getSession().getRemoteAddress().toString());
            if (AutoRegister.success != false) {
                loginok = c.login(login, pwd, ipBan || macBan);
            }
        }
        Calendar tempbannedTill = c.getTempBanCalendar();
        if (loginok == 0 && (ipBan || macBan)) {
            loginok = 3;

            if (macBan) {
                // this is only an ipban o.O" - maybe we should refactor this a bit so it's more readable
                String[] ipSplit = c.getSession().getRemoteAddress().toString().split(":");
                MapleCharacter.ban(ipSplit[0], "Enforcing account ban, account " + login, false);
            }
        }

        if (loginok != 0) {
            c.getSession().write(MaplePacketCreator.getLoginFailed(loginok));
            return;
        } else if (tempbannedTill.getTimeInMillis() != 0) {
            long tempban = KoreanDateUtil.getTempBanTimestamp(tempbannedTill.getTimeInMillis());
            byte reason = c.getBanReason();
            c.getSession().write(MaplePacketCreator.getTempBan(tempban, reason));
            return;
        }
        if (c.isGm()) {
            LoginWorker.getInstance().registerGMClient(c);
        } else {
            LoginWorker.getInstance().registerClient(c);
        }
    }
}
