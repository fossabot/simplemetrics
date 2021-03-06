package com.j256.simplemetrics.persister;

import java.io.IOException;

import com.j256.simplemetrics.manager.MetricsManager;

/**
 * Metrics persister thread that can be used to call {@link MetricsManager#persistValuesOnly()} at a specific frequency.
 * This is used if you don't have another mechanism to call the persisting on your own. If you are using the no-arg
 * constructor (like with Spring) you will need to make sure that {@link #initialize()} is called.
 * 
 * @author graywatson
 */
public class MetricsPersisterJob implements Runnable {

	private MetricsManager metricsManager;
	private long delayTimeMillis = -1;
	private long periodTimeMillis;
	private boolean daemonThread = true;

	private Thread thread;

	public MetricsPersisterJob() {
		// for spring
	}

	/**
	 * Create the MetricsPersisterThread and calls {@link #initialize()}.
	 */
	public MetricsPersisterJob(MetricsManager metricsManager, long delayTimeMillis, long periodTimeMillis,
			boolean daemonThread) {
		this.metricsManager = metricsManager;
		this.delayTimeMillis = delayTimeMillis;
		this.periodTimeMillis = periodTimeMillis;
		this.daemonThread = daemonThread;
		initialize();
	}

	/**
	 * Should be called if the no-arg construct is being used and after the file metrics have been set. Maybe by Springs
	 * init mechanism?
	 */
	public void initialize() {
		this.thread = new Thread(this, getClass().getSimpleName());
		this.thread.setDaemon(daemonThread);
		this.thread.start();
	}

	/**
	 * Call when you want to shutdown the publisher thread.
	 */
	public void destroy() {
		this.thread.interrupt();
		// NOTE: we are not waiting for the thread to finish on purpose
	}

	/**
	 * Call when you want to join with the the publisher thread.
	 */
	public void join() {
		destroy();
		try {
			this.thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Run by the thread to persist the metrics.
	 */
	@Override
	public void run() {
		if (delayTimeMillis >= 0) {
			try {
				Thread.sleep(delayTimeMillis);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}

		// we do a sleep here so no need to test for Thread.isInterrupted()
		while (true) {
			long lastStartMillis = System.currentTimeMillis();
			try {
				metricsManager.persist();
			} catch (IOException ioe) {
				// ignore I guess
			}

			try {
				// sleep the number of millis so that we start the persisting at the same period each time
				Thread.sleep(lastStartMillis + periodTimeMillis - System.currentTimeMillis());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	// @Required
	public void setMetricsManager(MetricsManager metricsManager) {
		this.metricsManager = metricsManager;
	}

	/**
	 * Number of millis to sleep when the thread starts before we start persisting. By default this is the same as the
	 * period-time-millis.
	 */
	// @NotRequired("Default is the same as the periodTimeMillis")
	public void setDelayTimeMillis(long delayTimeMillis) {
		this.delayTimeMillis = delayTimeMillis;
	}

	/**
	 * Number of millis to sleep between each persisting call.
	 */
	// @Required
	public void setPeriodTimeMillis(long periodTimeMillis) {
		this.periodTimeMillis = periodTimeMillis;
		if (this.delayTimeMillis < 0) {
			this.delayTimeMillis = periodTimeMillis;
		}
	}

	/**
	 * Whether or not the thread is a daemon thread. If true then the JVM will quit even if this thread is still
	 * running.
	 */
	// @NotRequired("Default is false")
	public void setDaemonThread(boolean daemonThread) {
		this.daemonThread = daemonThread;
	}
}
