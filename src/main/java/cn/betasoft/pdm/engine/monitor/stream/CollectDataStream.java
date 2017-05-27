package cn.betasoft.pdm.engine.monitor.stream;

import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class CollectDataStream {

	@Autowired
	private Properties kafkaStreamProperties;

	public CollectDataStream() {

	}

	@PostConstruct
	public void init() {
		kafkaStreamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "collectStat");
		kafkaStreamProperties.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, CollectDataSerde.class.getName());

		KStreamBuilder builder = new KStreamBuilder();
		KStream<String, ActorStatistics> source = builder.stream("collectData");

		KStream<TickerWindow, CollectStat> stats = source.groupByKey()
				.aggregate(CollectStat::new, (k, v, collectstats) -> collectstats.add(v),
						TimeWindows.of(5 * 1000).advanceBy(1000), new CollectStatSerde(), "collect-stats-store")
				.toStream((key, value) -> new TickerWindow("collectData", key.window().start()))
				.mapValues((collectStat) -> (collectStat.computeAvgTime()));

		stats.to(new TickerWindowSerde(), new CollectStatSerde(), "collectStat");

		KafkaStreams streams = new KafkaStreams(builder, kafkaStreamProperties);

		streams.cleanUp();

		streams.start();
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
