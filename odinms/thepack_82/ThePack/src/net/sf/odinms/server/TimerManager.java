package net.sf.odinms.server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sf.odinms.client.messages.MessageCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerManager implements TimerManagerMBean {

    private static Logger log = LoggerFactory.getLogger(TimerManager.class);
    private static TimerManager instance = new TimerManager();
    private ScheduledThreadPoolExecutor ses;

    private TimerManager() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mBeanServer.registerMBean(this, new ObjectName("net.sf.odinms.server:type=TimerManger"));
        } catch (Exception e) {
            log.error("Error registering MBean", e);
        }
    }

    public static TimerManager getInstance() {
        return instance;
    }

    public void start() {
        if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
            return; //starting the same timermanager twice is no - op
        }
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {

            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Timermanager-Worker-" + threadNumber.getAndIncrement());
                return t;
            }
        });
        stpe.setMaximumPoolSize(4);
        stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        ses = stpe;
    }

    public void stop() {
        ses.shutdown();
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
        return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime) {
        return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable r, long delay) {
        return ses.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
        return schedule(r, timestamp - System.currentTimeMillis());
    }

    public void dropDebugInfo(MessageCallback callback) {
        StringBuilder builder = new StringBuilder();
        builder.append("Terminated: ");
        builder.append(ses.isTerminated());
        builder.append(" Shutdown: ");
        builder.append(ses.isShutdown());
        callback.dropMessage(builder.toString());

        builder = new StringBuilder();
        builder.append("Completed Tasks: ");
        builder.append(ses.getCompletedTaskCount());
        builder.append(" Active Tasks: ");
        builder.append(ses.getActiveCount());
        builder.append(" Task Count: ");
        builder.append(ses.getTaskCount());
        callback.dropMessage(builder.toString());

        builder = new StringBuilder();
        builder.append("Queued Tasks: ");
        builder.append(ses.getQueue().toArray().length);
        callback.dropMessage(builder.toString());
    }

    @Override
    public long getActiveCount() {
        return ses.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return ses.getCompletedTaskCount();
    }

    @Override
    public int getQueuedTasks() {
        return ses.getQueue().toArray().length;
    }

    @Override
    public long getTaskCount() {
        return ses.getTaskCount();
    }

    @Override
    public boolean isShutdown() {
        return ses.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return ses.isTerminated();
    }

    private static class LoggingSaveRunnable implements Runnable {

        Runnable r;

        public LoggingSaveRunnable(Runnable r) {
            this.r = r;
        }

        @Override
        public void run() {
            try {
                r.run();
            } catch (Throwable t) {
                log.error("ERROR", t);
            }
        }
    }
}