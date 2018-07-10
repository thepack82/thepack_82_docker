package net.sf.odinms.client.messages;

public interface CommandProcessorMBean {
	String processCommandJMX(int cserver, int mapid, String command);
}