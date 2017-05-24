package cn.betasoft.pdm.engine.monitor.listener;

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

	private List<Thread> consumers = new ArrayList<>();

	private ExecutorService executor = Executors.newCachedThreadPool();

	private static final Logger logger = LoggerFactory.getLogger(MonitorListenerStart.class);

	public MonitorListenerStart(){

	}

	@PostConstruct
	public void start(){
		Thread heapInfoListener = new HeapInfoListener(kafkaConsumerProperties);
		consumers.add(heapInfoListener);
		executor.submit(heapInfoListener);
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
