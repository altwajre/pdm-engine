package cn.betasoft.pdm.engine.monitor.query;

import java.util.*;

import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.betasoft.pdm.engine.model.monitor.HeapInfo;

public class DispatcherInfoQuery {

	private String groupId;

	private String topic;

	private Long offsetTime;

	private Properties kafkaConsumerProperties;

	private KafkaConsumer<String, String> consumer;

	private static final Logger logger = LoggerFactory.getLogger(DispatcherInfoQuery.class);

	public DispatcherInfoQuery(String groupId, String topic, Long offsetTime, Properties kafkaConsumerProperties) {
		this.groupId = groupId;
		this.topic = topic;
		this.offsetTime = offsetTime;
		this.kafkaConsumerProperties = kafkaConsumerProperties;

		this.createConsumer();
	}

	public List<DispatcherInfo> query() {
		List<DispatcherInfo> infos = new ArrayList<>();

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
				DispatcherInfo dispatcherInfo = objectMapper.readValue(record.value(), DispatcherInfo.class);
				infos.add(dispatcherInfo);
			}
		} catch (Exception ex) {
			logger.info("query heap info error", ex);
		} finally {
			consumer.close();
		}
		return infos;
	}

	private void createConsumer() {
		kafkaConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
		consumer = new KafkaConsumer<>(kafkaConsumerProperties);
		consumer.subscribe(Arrays.asList(this.topic));
	}
}
