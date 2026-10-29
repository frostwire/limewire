package net.sf.fmj.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {
	private static ExecutorService executorService = Executors.newFixedThreadPool(1);

	public static synchronized ExecutorService getExecutorService() {
		return executorService;
	}

	public static synchronized void setExecutorService(ExecutorService executor) {
		executorService = executor;
	}
}
