package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ActorBean
public class KafkaProduceActor extends AbstractActor {

	static public class MonitorMessage {

		private final String topic;

		private final String key;

		private final String value;

		public MonitorMessage(String topic, String key, String value) {
			this.topic = topic;
			this.key = key;
			this.value = value;
		}

		public String getTopic() {
			return topic;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
	}

	@Autowired
	KafkaProducer kafkaProducer;

	private static final Logger logger = LoggerFactory.getLogger(KafkaProduceActor.class);

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(MonitorMessage.class, monitorMessage -> {
			ProducerRecord<String, String> record = new ProducerRecord<>(monitorMessage.getTopic(),
					monitorMessage.getKey(), monitorMessage.getValue());
			kafkaProducer.send(record);
		}).matchAny(o -> logger.info("received unknown message")).build();
	}
}
