package net.sf.odinms.server.maps;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.tools.MaplePacketCreator;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

public class MapleMapTimer {
	private int duration;
	private Calendar startTime;
	private Calendar predictedStopTime;
	private int mapToWarpTo = -1;
	private int minLevelToWarp = 0;
	private int maxLevelToWarp = 256;
	private ScheduledFuture<?> sf0F;
	
	public MapleMapTimer(ScheduledFuture<?> sfO, int newDuration, int mapToWarpToP, int minLevelToWarpP, int maxLevelToWarpP) {
		this.duration = newDuration;
		this.startTime = Calendar.getInstance();
		this.predictedStopTime = Calendar.getInstance();
		this.predictedStopTime.add(Calendar.SECOND, duration);
		this.mapToWarpTo = mapToWarpToP;
		this.minLevelToWarp = minLevelToWarpP;
		this.maxLevelToWarp = maxLevelToWarpP;
		this.sf0F = sfO;
	}
	
	public MaplePacket makeSpawnData() {
		int timeLeft;
		long StopTimeStamp = this.predictedStopTime.getTimeInMillis();
		long CurrentTimeStamp = Calendar.getInstance().getTimeInMillis();
		timeLeft =(int) (StopTimeStamp - CurrentTimeStamp) / 1000;
		return MaplePacketCreator.getClock(timeLeft);
	}
	
	public void sendSpawnData(MapleClient c) {
		c.getSession().write(makeSpawnData());
	}
	
	public ScheduledFuture<?> getSF0F() {
		return sf0F;
	}
	
	public int warpToMap() {
		return this.mapToWarpTo;
	}
	
	public int minLevelToWarp() {
		return this.minLevelToWarp;
	}
	
	public int maxLevelToWarp() {
		return this.maxLevelToWarp;
	}
	
	public int getTimeLeft() {
		int timeLeft;
		long StopTimeStamp = predictedStopTime.getTimeInMillis();
		long CurrentTimeStamp = Calendar.getInstance().getTimeInMillis();
		timeLeft =(int) (StopTimeStamp - CurrentTimeStamp) / 1000;
		return timeLeft;
	}
}
