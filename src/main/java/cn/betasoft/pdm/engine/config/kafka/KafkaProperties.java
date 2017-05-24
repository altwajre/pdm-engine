package cn.betasoft.pdm.engine.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

	private  Bootstrap bootstrap;

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public static class Bootstrap {

		private List<String> servers = new ArrayList<>();

		public List<String> getServers() {
			return servers;
		}

		public void setServers(List<String> servers) {
			this.servers = servers;
		}
	}
}
