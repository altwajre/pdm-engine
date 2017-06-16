package cn.betasoft.pdm.engine.monitor.query;

import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.monitor.stream.TickerWindow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IndicatorHandleStatQuery {

	private String groupId;

	private String topic;

	private Long offsetTime;

	private Properties kafkaConsumerProperties;

	private KafkaConsumer<String, String> consumer;

	private static final Logger logger = LoggerFactory.getLogger(IndicatorHandleStatQuery.class);

	public IndicatorHandleStatQuery(String groupId, String topic, Long offsetTime, Properties kafkaConsumerProperties) {
		this.groupId = groupId;
		this.topic = topic;
		this.offsetTime = offsetTime;
		this.kafkaConsumerProperties = kafkaConsumerProperties;

		this.createConsumer();
	}

	public List<CollectStat> query() {
		Set<CollectStat> collectStats = new HashSet<>();

		try {
			ConsumerRecords<String, String> records = consumer.poll(500);
			Set<TopicPartition> assignments = consumer.assignment();
			Map<TopicPartition, Long> query = new HashMap<>();
			for (TopicPartition topicPartition : assignments) {
				query.put(topicPartition, offsetTime);
			}

			Map<TopicPartition, OffsetAndTimestamp> result = consumer.offsetsForTimes(query);
			result.entrySet().stream().forEach(entry -> {
				consumer.seek(entry.getKey(),
						Optional.ofNullable(entry.getValue()).map(OffsetAndTimestamp::offset).orElse(new Long(0)));
			});
			for (ConsumerRecord<String, String> record : records) {
				ObjectMapper objectMapper = new ObjectMapper();

				TickerWindow tickerWindow = objectMapper.readValue(record.key(),TickerWindow.class);
				CollectStat collectStat = objectMapper.readValue(record.value(),CollectStat.class);
				collectStat.setSampleTime(new Date(tickerWindow.getTimestamp()));
				collectStats.add(collectStat);
			}
		} catch (Exception ex) {
			logger.info("query collect data error", ex);
		} finally {
			consumer.close();
		}
		return new ArrayList<>(collectStats);
	}

	private void createConsumer() {
		kafkaConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
		consumer = new KafkaConsumer<>(kafkaConsumerProperties);
		consumer.subscribe(Arrays.asList(this.topic));
	}
}
