package cn.betasoft.pdm.engine.config.quartz;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@ConditionalOnExpression("'${quartz.enabled}'=='true'")
public class QuartzConfig {

	@Autowired
	@Qualifier("quartzDataSource")
	DataSource dataSource;

	@Autowired
	private QuartzJobFactory quartzJobFacory;

	@Autowired
	private QuartzProperties quartzProperties;

	// true:使用内存 false:使用数据库 存储工作计划
	@Value("${quartz.useRam}")
	private boolean useRam;

	private static final Logger logger = LoggerFactory.getLogger(QuartzConfig.class);

	@PostConstruct
	public void init() {
		logger.info("quartz config finish...");
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();

		// this allows to update triggers in DB when updating settings in config
		// file:
		factory.setOverwriteExistingJobs(true);

		// 延时启动
		factory.setStartupDelay(5);

		if (!useRam) {
			factory.setDataSource(dataSource);
		}

		// 加载quartz数据源配置
		factory.setQuartzProperties(getQuartzProperties());

		// 自定义Job Factory，用于Spring注入
		factory.setJobFactory(quartzJobFacory);

		return factory;
	}

	@Bean
	public Properties getQuartzProperties() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("org.quartz.scheduler.instanceName", quartzProperties.getInstanceName());
		properties.setProperty("org.quartz.scheduler.instanceId", quartzProperties.getInstanceId());
		properties.setProperty("org.quartz.scheduler.makeSchedulerThreadDaemon",
				quartzProperties.getMakeSchedulerThreadDaemon());
		properties.setProperty("org.quartz.threadPool.class", quartzProperties.getThreadPoolClass());
		properties.setProperty("org.quartz.threadPool.threadCount", quartzProperties.getThreadCount());
		properties.setProperty("org.quartz.threadPool.makeThreadsDaemons", quartzProperties.getMakeThreadsDaemons());
		if (useRam) {
			properties.setProperty("org.quartz.jobStore.class", quartzProperties.getJobStoreRamClass());
		} else {
			properties.setProperty("org.quartz.jobStore.class", quartzProperties.getJobStoreDbClass());
			properties.setProperty("org.quartz.jobStore.driverDelegateClass",
					quartzProperties.getDriverDelegateClass());
			properties.setProperty("org.quartz.jobStore.useProperties", "true");
			properties.setProperty("org.quartz.jobStore.misfireThreshold", quartzProperties.getMisfireThreshold());
			properties.setProperty("org.quartz.jobStore.maxMisfiresToHandleAtATime",
					quartzProperties.getMaxMisfiresToHandleAtATime());
			properties.setProperty("org.quartz.jobStore.tablePrefix", quartzProperties.getTablePrefix());
		}

		return properties;
	}
}
