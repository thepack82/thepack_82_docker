package net.sf.odinms.net.mina;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.tools.MapleCustomEncryption;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MaplePacketEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            byte[] input = ((MaplePacket) message).getBytes();
            byte[] unencrypted = new byte[input.length];
            System.arraycopy(input, 0, unencrypted, 0, input.length);
            byte[] ret = new byte[unencrypted.length + 4];
            byte[] header = client.getSendCrypto().getPacketHeader(unencrypted.length);
            synchronized (client.getSendCrypto()) {
                MapleCustomEncryption.encryptData(unencrypted);
                client.getSendCrypto().crypt(unencrypted);
                System.arraycopy(header, 0, ret, 0, 4);
                System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
                ByteBuffer out_buffer = ByteBuffer.wrap(ret);
                out.write(out_buffer);
            }
        } else { // no client object created yet, send unencrypted (hello)
            out.write(ByteBuffer.wrap(((MaplePacket) message).getBytes()));
        }
    }

    @Override
    public void dispose(IoSession session) throws Exception {
    }
}
