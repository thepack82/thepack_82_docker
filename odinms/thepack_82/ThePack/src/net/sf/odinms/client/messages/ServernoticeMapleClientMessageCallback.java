package net.sf.odinms.client.messages;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.tools.MaplePacketCreator;

public class ServernoticeMapleClientMessageCallback implements MessageCallback {
	private MapleClient client;
	private int mode;
	
	public ServernoticeMapleClientMessageCallback(MapleClient client) {
		this (6, client);
	}
	
	public ServernoticeMapleClientMessageCallback(int mode, MapleClient client) {
		this.client = client;
		this.mode = mode;
	}
	
	@Override
	public void dropMessage(String message) {
		client.getSession().write(MaplePacketCreator.serverNotice(mode, message));
	}
}