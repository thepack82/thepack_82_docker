package net.sf.odinms.net.mina;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.tools.MapleAESOFB;
import net.sf.odinms.tools.MapleCustomEncryption;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaplePacketDecoder extends CumulativeProtocolDecoder {
	private static final String DECODER_STATE_KEY = MaplePacketDecoder.class.getName() + ".STATE";
	private static Logger log = LoggerFactory.getLogger(MaplePacketDecoder.class);
	
	private static class DecoderState {
		public int packetlength = -1;
	}

	@Override
	protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
		MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
		DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
		if (decoderState == null) {
			decoderState = new DecoderState();
			session.setAttribute(DECODER_STATE_KEY, decoderState);
		}
		if (in.remaining() >= 4 && decoderState.packetlength == -1) {
			int packetHeader = in.getInt();
			if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
				log.warn(MapleClient.getLogMessage(client, "Client failed packet check -> disconnecting"));
				session.close();
				return false;
			}
			decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
		} else if (in.remaining() < 4 && decoderState.packetlength == -1) {
			log.trace("decode... not enough data");
			return false;
		}
		if (in.remaining() >= decoderState.packetlength) {
			byte decryptedPacket[] = new byte[decoderState.packetlength];
			in.get(decryptedPacket, 0, decoderState.packetlength);
			decoderState.packetlength = -1;
			client.getReceiveCrypto().crypt(decryptedPacket);
			MapleCustomEncryption.decryptData(decryptedPacket);
			out.write(decryptedPacket);
			return true;
		} else {
			log.trace("decode... not enough data to decode (need {})", decoderState.packetlength);
			return false;
		}
	}
}
