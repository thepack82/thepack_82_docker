package net.sf.odinms.client;

public interface RunnableWithResult extends Runnable {
	Object getResult();
	boolean isDone();
}
