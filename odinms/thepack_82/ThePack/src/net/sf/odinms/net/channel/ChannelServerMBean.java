package net.sf.odinms.net.channel;


public interface ChannelServerMBean {
	void shutdown(int time);
	void shutdownWorld(int time);
	String getServerMessage();
	void setServerMessage(String newMessage);
	int getChannel();
	int getExpRate();
	int getMesoRate();
	int getDropRate();
	int getBossDropRate();
	int getPetExpRate();
    int getMountRate();
    int getShopMesoRate();
	void setExpRate(int expRate);
	void setMesoRate(int mesoRate);
	void setDropRate(int dropRate);
	void setBossDropRate(int bossDropRate);
	void setPetExpRate(int petExpRate);
	void setMountRate(int mountExpRate);
    void setShopMesoRate (int shopMesoRate);
	int getConnectedClients();
	int getLoadedMaps();
}
