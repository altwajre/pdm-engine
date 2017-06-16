package cn.betasoft.pdm.engine.config.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {

	@Bean
	public MetricRegistry metricRegistry(){
       return new MetricRegistry();
	}

	@Bean
	public ConsoleReporter consoleReport(){
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry())
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.start(5, TimeUnit.SECONDS);
		return reporter;
	}
}
