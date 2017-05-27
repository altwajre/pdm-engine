package cn.betasoft.pdm.engine.monitor.listener;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Instant;
import java.util.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.betasoft.pdm.engine.model.monitor.MonitorMessage;
import cn.betasoft.pdm.engine.model.monitor.MonitorType;
import cn.betasoft.pdm.engine.monitor.websocket.MonitorMsgSend;

public class DispatcherInfoListener extends Thread {

	private Properties kafkaConsumerProperties;

	private String name;

	private KafkaConsumer<String, String> consumer;

	private MonitorMsgSend monitorMsgSend;

	private static final String GROUP = "monitor";

	private static final int OFFSETMINUTE = 5;

	private static final Logger logger = LoggerFactory.getLogger(DispatcherInfoListener.class);

	public DispatcherInfoListener(String name, Properties kafkaConsumerProperties, MonitorMsgSend monitorMsgSend) {
		this.name = name;
		this.kafkaConsumerProperties = kafkaConsumerProperties;
		this.monitorMsgSend = monitorMsgSend;
		createConsumer();
	}

	@Override
	public void run() {
		boolean flag = true;

		while (!this.isInterrupted()) {
			ConsumerRecords<String, String> records = consumer.poll(100);

			if (flag) {
				Set<TopicPartition> assignments = consumer.assignment();
				Map<TopicPartition, Long> query = new HashMap<>();
				for (TopicPartition topicPartition : assignments) {
					query.put(topicPartition, Instant.now().minus(OFFSETMINUTE, MINUTES).toEpochMilli());
				}

				Map<TopicPartition, OffsetAndTimestamp> result = consumer.offsetsForTimes(query);

				result.entrySet().stream().forEach(entry -> consumer.seek(entry.getKey(),
						Optional.ofNullable(entry.getValue()).map(OffsetAndTimestamp::offset).orElse(new Long(0))));

				flag = false;
			}

			for (ConsumerRecord<String, String> record : records) {
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					MonitorType monitorType = MonitorType.getByName(name);
					MonitorMessage monitorMessage = new MonitorMessage(monitorType, record.value());
					String value = objectMapper.writeValueAsString(monitorMessage);
					monitorMsgSend.sendMessage(value);
					logger.info("offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
				} catch (Exception ex) {
					logger.info("parse heap info error", ex);
				}

			}
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		consumer.wakeup();
	}

	private void createConsumer() {
		kafkaConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
		consumer = new KafkaConsumer<>(kafkaConsumerProperties);
		consumer.subscribe(Arrays.asList(name));
	}
}
