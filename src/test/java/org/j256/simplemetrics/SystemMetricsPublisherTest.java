package org.j256.simplemetrics;

import org.junit.Test;

import com.j256.simplejmx.server.JmxServer;
import com.j256.simplemetrics.MetricsManager;
import com.j256.simplemetrics.SystemMetricsPublisher;

public class SystemMetricsPublisherTest {

	@Test
	public void testPollBeforeInit() {
		MetricsManager manager = new MetricsManager();
		manager.setJmxServer(new JmxServer());
		SystemMetricsPublisher publisher = new SystemMetricsPublisher();
		publisher.setMetricsManager(manager);
		publisher.initialize();
		publisher.updateMetrics();
	}
}
