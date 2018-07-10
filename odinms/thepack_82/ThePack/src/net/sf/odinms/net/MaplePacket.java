package net.sf.odinms.net;

public interface MaplePacket extends java.io.Serializable {
	public byte[] getBytes();
	public Runnable getOnSend();
	public void setOnSend(Runnable onSend);
}
