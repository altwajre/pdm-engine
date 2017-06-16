package cn.betasoft.pdm.engine.monitor.stream;

import cn.betasoft.pdm.engine.config.kafka.KafkaProperties;
import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
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
public class IndicatorHandleStream {

	@Autowired
	private KafkaProperties kafkaProperties;

	public IndicatorHandleStream() {

	}

	@PostConstruct
	public void init() {
		Properties kafkaProps = creatPproperties();
		kafkaProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "indicatorHandleStat");
		kafkaProps.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, CollectDataSerde.class.getName());
		kafkaProps.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "20000");
		KStreamBuilder builder = new KStreamBuilder();
		KStream<String, ActorStatistics> source = builder.stream("indicatorHandleData");

		KStream<TickerWindow, CollectStat> stats = source.groupByKey()
				.aggregate(CollectStat::new, (k, v, collectstats) -> collectstats.add(v),
						TimeWindows.of(10*1000L).until(10*1000L), new CollectStatSerde(), "indicatorHandle-stats-store")
				.toStream((key, value) -> new TickerWindow("indicatorHandleData", key.window().start()))
				.mapValues((collectStat) -> (collectStat.computeAvgTime()));

		stats.to(new TickerWindowSerde(), new CollectStatSerde(), "indicatorHandleStat");

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

	static public final class CollectDataSerde extends WrapperSerde<ActorStatistics> {
		public CollectDataSerde() {
			super(ActorStatistics.class);
		}
	}

	static public final class CollectStatSerde extends WrapperSerde<CollectStat> {
		public CollectStatSerde() {
			super(CollectStat.class);
		}
	}

	static public final class TickerWindowSerde extends WrapperSerde<TickerWindow> {
		public TickerWindowSerde() {
			super(TickerWindow.class);
		}
	}
}
