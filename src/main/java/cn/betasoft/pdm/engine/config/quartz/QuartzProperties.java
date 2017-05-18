package cn.betasoft.pdm.engine.config.quartz;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "quartz.properties")
public class QuartzProperties {

	private String instanceName;

	private String instanceId;

	private String makeSchedulerThreadDaemon;

	private String threadPoolClass;

	private String threadCount;

	private String makeThreadsDaemons;

	private String jobStoreRamClass;

	private String jobStoreDbClass;

	private String driverDelegateClass;

	private String misfireThreshold;

	private String maxMisfiresToHandleAtATime;

	private String tablePrefix;

	public QuartzProperties(){

	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getMakeSchedulerThreadDaemon() {
		return makeSchedulerThreadDaemon;
	}

	public void setMakeSchedulerThreadDaemon(String makeSchedulerThreadDaemon) {
		this.makeSchedulerThreadDaemon = makeSchedulerThreadDaemon;
	}

	public String getThreadPoolClass() {
		return threadPoolClass;
	}

	public void setThreadPoolClass(String threadPoolClass) {
		this.threadPoolClass = threadPoolClass;
	}

	public String getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(String threadCount) {
		this.threadCount = threadCount;
	}

	public String getMakeThreadsDaemons() {
		return makeThreadsDaemons;
	}

	public void setMakeThreadsDaemons(String makeThreadsDaemons) {
		this.makeThreadsDaemons = makeThreadsDaemons;
	}

	public String getJobStoreRamClass() {
		return jobStoreRamClass;
	}

	public void setJobStoreRamClass(String jobStoreRamClass) {
		this.jobStoreRamClass = jobStoreRamClass;
	}

	public String getJobStoreDbClass() {
		return jobStoreDbClass;
	}

	public void setJobStoreDbClass(String jobStoreDbClass) {
		this.jobStoreDbClass = jobStoreDbClass;
	}

	public String getDriverDelegateClass() {
		return driverDelegateClass;
	}

	public void setDriverDelegateClass(String driverDelegateClass) {
		this.driverDelegateClass = driverDelegateClass;
	}

	public String getMisfireThreshold() {
		return misfireThreshold;
	}

	public void setMisfireThreshold(String misfireThreshold) {
		this.misfireThreshold = misfireThreshold;
	}

	public String getMaxMisfiresToHandleAtATime() {
		return maxMisfiresToHandleAtATime;
	}

	public void setMaxMisfiresToHandleAtATime(String maxMisfiresToHandleAtATime) {
		this.maxMisfiresToHandleAtATime = maxMisfiresToHandleAtATime;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}
}
