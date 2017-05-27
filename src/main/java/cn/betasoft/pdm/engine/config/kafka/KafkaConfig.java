package cn.betasoft.pdm.engine.config.kafka;

import cn.betasoft.pdm.engine.config.quartz.QuartzProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

	@Autowired
	private KafkaProperties kafkaProperties;

	@Bean(destroyMethod = "close")
	public KafkaProducer kafkaProducer() {
		KafkaProducer producer = new KafkaProducer<String, String>(producerProperties());
		return producer;
	}

	private Properties producerProperties() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				String.join(",", kafkaProperties.getBootstrap().getServers()));
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return kafkaProps;
	}

	@Bean(name = "kafkaConsumerProperties")
	public Properties kafkaConsumerProperties() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				String.join(",", kafkaProperties.getBootstrap().getServers()));
		kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		return kafkaProps;
	}

	@Bean(name = "kafkaStreamProperties")
	public Properties kafkaStreamProperties() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
				String.join(",", kafkaProperties.getBootstrap().getServers()));
		kafkaProps.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG,  Serdes.String().getClass().getName());
		kafkaProps.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		return kafkaProps;
	}
}
