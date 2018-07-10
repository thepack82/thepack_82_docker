package net.sf.odinms.net.mina;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class MapleCodecFactory implements ProtocolCodecFactory {
	private final ProtocolEncoder encoder;
	private final ProtocolDecoder decoder;

	public MapleCodecFactory() {
		encoder = new MaplePacketEncoder();
		decoder = new MaplePacketDecoder();
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

	public ProtocolDecoder getDecoder() throws Exception {
		return decoder;
	}
}
