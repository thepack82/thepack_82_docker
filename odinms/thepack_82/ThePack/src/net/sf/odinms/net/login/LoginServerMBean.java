package net.sf.odinms.net.login;

public interface LoginServerMBean {
	int getNumberOfSessions();
	int getPossibleLogins();
	int getLoginInterval();
	String getEventMessage();
	int getFlag();
	void setEventMessage(String newMessage);
	void setFlag(int flag);
	int getUserLimit();
	void setUserLimit(int newLimit);
}
