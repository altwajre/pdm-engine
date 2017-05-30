package cn.betasoft.pdm.engine.monitor.stream;

import cn.betasoft.pdm.engine.config.kafka.KafkaProperties;
import cn.betasoft.pdm.engine.model.monitor.MailBoxStat;
import cn.betasoft.pdm.engine.perf.mailbox.MailboxStatistics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class MailBoxStream {

	@Autowired
	private KafkaProperties kafkaProperties;

	public MailBoxStream() {

	}

	@PostConstruct
	public void init() {
		Properties kafkaProps = creatPproperties();
		kafkaProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "mailboxStat");
		kafkaProps.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, MailboxStatisticsSerde.class.getName());
		kafkaProps.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "20000");

		KStreamBuilder builder = new KStreamBuilder();
		KStream<String, MailboxStatistics> source = builder.stream("mailboxData");

		KStream<TickerWindow, MailBoxStat> stats = source.groupByKey()
				.aggregate(MailBoxStat::new, (k, v, collectstats) -> collectstats.add(v),
						TimeWindows.of(10*1000L).until(10*1000L), new MailBoxStatSerde(), "mailbox-stats-store")
				.toStream((key, value) -> new TickerWindow("mailboxData", key.window().start()))
				.mapValues((collectStat) -> (collectStat.computeAvgTime()));

		stats.to(new TickerWindowSerde(), new MailBoxStatSerde(), "mailboxStat");

		KafkaStreams streams = new KafkaStreams(builder, kafkaProps);

		streams.cleanUp();

		streams.start();
	}

	private Properties creatPproperties() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
				String.join(",", kafkaProperties.getBootstrap().getServers()));
		kafkaProps.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG,  Serdes.String().getClass().getName());
		return kafkaProps;
	}

	static public final class MailboxStatisticsSerde extends WrapperSerde<MailboxStatistics> {
		public MailboxStatisticsSerde() {
			super(MailboxStatistics.class);
		}
	}

	static public final class MailBoxStatSerde extends WrapperSerde<MailBoxStat> {
		public MailBoxStatSerde() {
			super(MailBoxStat.class);
		}
	}

	static public final class TickerWindowSerde extends WrapperSerde<TickerWindow> {
		public TickerWindowSerde() {
			super(TickerWindow.class);
		}
	}
}
