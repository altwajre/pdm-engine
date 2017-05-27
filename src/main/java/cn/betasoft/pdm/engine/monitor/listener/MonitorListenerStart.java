package cn.betasoft.pdm.engine.monitor.listener;

import cn.betasoft.pdm.engine.monitor.websocket.MonitorMsgSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class MonitorListenerStart {

	@Autowired
	private Properties kafkaConsumerProperties;

	@Autowired
	private MonitorMsgSend monitorMsgSend;

	private List<Thread> consumers = new ArrayList<>();

	private ExecutorService executor = Executors.newCachedThreadPool();

	private String[] dispatcherNames = { "akka.actor.default-dispatcher", "pdm-work-dispatcher",
			"pdm-future-dispatcher" };

	private static final Logger logger = LoggerFactory.getLogger(MonitorListenerStart.class);

	public MonitorListenerStart() {

	}

	@PostConstruct
	public void start() {
		Thread heapInfoListener = new HeapInfoListener(kafkaConsumerProperties, monitorMsgSend);
		consumers.add(heapInfoListener);
		executor.submit(heapInfoListener);

		for (String dispatcherName : dispatcherNames) {
			Thread dispatcherInfoListener = new DispatcherInfoListener(dispatcherName, kafkaConsumerProperties,
					monitorMsgSend);
			consumers.add(dispatcherInfoListener);
			executor.submit(dispatcherInfoListener);
		}
	}

	@PreDestroy
	public void cleanUp() {
		for (Thread consumer : consumers) {
			consumer.interrupt();
		}
		executor.shutdown();
		try {
			executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
