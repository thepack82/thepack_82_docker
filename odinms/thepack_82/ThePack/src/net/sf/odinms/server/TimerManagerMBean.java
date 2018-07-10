package net.sf.odinms.server;

public interface TimerManagerMBean {
	public boolean isTerminated();
	public boolean isShutdown();
	public long getCompletedTaskCount();
	public long getActiveCount();
	public long getTaskCount();
	public int getQueuedTasks();
}
