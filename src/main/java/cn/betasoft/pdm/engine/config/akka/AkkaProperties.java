package cn.betasoft.pdm.engine.config.akka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.typesafe.config.Config;

@ConfigurationProperties(prefix = "akka")
public class AkkaProperties {

	private String systemName;

	private Config config;

	private String actorBeanClass;

	private String actorName;

	private String pinnedDispatcher;

	private String workDispatch;

	private String futureDispatch;

	private String monitorDispatch;

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = com.typesafe.config.ConfigFactory.load(config);
	}

	public String getActorBeanClass() {
		return actorBeanClass;
	}

	public void setActorBeanClass(String actorBeanClass) {
		this.actorBeanClass = actorBeanClass;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public String getPinnedDispatcher() {
		return pinnedDispatcher;
	}

	public void setPinnedDispatcher(String pinnedDispatcher) {
		this.pinnedDispatcher = pinnedDispatcher;
	}

	public String getWorkDispatch() {
		return workDispatch;
	}

	public void setWorkDispatch(String workDispatch) {
		this.workDispatch = workDispatch;
	}

	public String getFutureDispatch() {
		return futureDispatch;
	}

	public void setFutureDispatch(String futureDispatch) {
		this.futureDispatch = futureDispatch;
	}

	public String getMonitorDispatch() {
		return monitorDispatch;
	}

	public void setMonitorDispatch(String monitorDispatch) {
		this.monitorDispatch = monitorDispatch;
	}
}
