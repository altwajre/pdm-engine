package cn.betasoft.pdm.engine.config.metrics;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.logback.InstrumentedAppender;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {

	@Bean
	public MetricRegistry metricRegistry(){
		MetricRegistry registry =  new MetricRegistry();
		logbackInstrumentation(registry);
		return registry;
	}

	@Bean
	public ConsoleReporter consoleReport(){
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry())
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(10, TimeUnit.SECONDS);
		return reporter;
	}

	@Bean
	public Slf4jReporter slf4jReporter(){
		Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricRegistry())
				.outputTo(LoggerFactory.getLogger("monitor.timer"))
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		slf4jReporter.start(10, TimeUnit.SECONDS);
		return slf4jReporter;
	}

	private void logbackInstrumentation(MetricRegistry registry){
		LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

		InstrumentedAppender metrics = new InstrumentedAppender(registry);
		metrics.setName("monitor.logback");
		metrics.setContext(root.getLoggerContext());
		metrics.start();
		root.addAppender(metrics);
	}
}
