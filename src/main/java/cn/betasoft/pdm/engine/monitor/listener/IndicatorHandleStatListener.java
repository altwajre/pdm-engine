package cn.betasoft.pdm.engine.monitor.listener;

import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.model.monitor.MonitorMessage;
import cn.betasoft.pdm.engine.model.monitor.MonitorType;
import cn.betasoft.pdm.engine.monitor.stream.TickerWindow;
import cn.betasoft.pdm.engine.monitor.websocket.MonitorMsgSend;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

public class IndicatorHandleStatListener extends Thread {

	private Properties kafkaConsumerProperties;

	private KafkaConsumer<String, String> consumer;

	private MonitorMsgSend monitorMsgSend;

	private static final String GROUP = "monitor";

	private static final String TOPIC = "indicatorHandleStat";

	private static final int OFFSETMINUTE = 0;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(IndicatorHandleStatListener.class);

	public IndicatorHandleStatListener(Properties kafkaConsumerProperties, MonitorMsgSend monitorMsgSend) {
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

					TickerWindow tickerWindow = objectMapper.readValue(record.key(),TickerWindow.class);
					CollectStat collectStat = objectMapper.readValue(record.value(),CollectStat.class);
					collectStat.setSampleTime(new Date(tickerWindow.getTimestamp()));
					String collectStatValue = objectMapper.writeValueAsString(collectStat);

					MonitorMessage monitorMessage = new MonitorMessage(MonitorType.INDICATORHANDLESTAT, collectStatValue);
					String value = objectMapper.writeValueAsString(monitorMessage);
					monitorMsgSend.sendMessage(value);
					Thread.sleep(1000);
					//logger.info("offset = {}, time ={},key = {}, value = {}" ,record.offset(),sdf.format(tickerWindow.getTimestamp()), record.key(), record.value());
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
		consumer.subscribe(Arrays.asList(TOPIC));
	}
}
