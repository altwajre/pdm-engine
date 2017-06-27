package cn.betasoft.pdm.engine.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quartz.*;
import org.quartz.impl.calendar.CronCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * pdmJob指一个资源下的一个采集指标 这个采集指标包含多个采集触发器
 */
public class PdmJob {

	// 指标名称
	private String name;

	// 资源名称
	private String group;

	// 工作类，必需继承quartz的job
	private Class<? extends Job> target;

	// 一个jobDetail可以包括很多触发器
	private Set<Trigger> triggers = new HashSet<>();

	private transient JobKey jobKey;

	private transient JobDetail jobDetail;

	private static final Logger logger = LoggerFactory.getLogger(PdmJob.class);

	/**
	 * @param name
	 *            指标名称
	 * @param group
	 *            资源名称
	 * @param target
	 *            quartz工作类
	 */
	public PdmJob(String name, String group, Class<? extends Job> target) {
		this.name = name;
		this.group = group;
		this.target = target;
		this.jobKey = createJobKey();
	}

	public JobKey getJobKey() {
		return jobKey;
	}

	public JobDetail getJobDetail() {
		return jobDetail;
	}

	public Set<Trigger> getTriggers() {
		return triggers;
	}

	private JobKey createJobKey() {
		JobKey jobKey = JobKey.jobKey(this.name, this.group);
		return jobKey;
	}

	public JobDetail createJobDetail() {
		if (jobDetail == null) {
			jobDetail = JobBuilder.newJob(target).withIdentity(this.jobKey).build();
		}
		return jobDetail;
	}

	public void addConTrigger(String triggerName, String cronExpression) {
		// hascode() and equals() use TriggerKey
		// MISFIRE_INSTRUCTION_DO_NOTHING 重启后，对于已经超过执行时间的计划，不在执行，等下一个执行周期执行
		Trigger trigger = newTrigger().withIdentity(createTriggerKey(triggerName))
				.withSchedule(cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
				.forJob(name, group).build();
		triggers.add(trigger);
		logger.info("add a {} job triiger {}",triggerName,cronExpression);
	}

	public PdmJob addJobData(String key, Object value) {
		final JobDetail detail = createJobDetail();
		final JobDataMap jobDataMap = detail.getJobDataMap();
		jobDataMap.put(key, value);
		return this;
	}

	/**
	 * 一个组内的触发器是唯一的,所以组名由jobKey组成,保证唯一性
	 *
	 * @param triggerName
	 * @return
	 */
	private TriggerKey createTriggerKey(String triggerName) {
		TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, this.jobKey.toString());
		return triggerKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PdmJob pdmJob = (PdmJob) o;

		if (name != null ? !name.equals(pdmJob.name) : pdmJob.name != null)
			return false;
		return group != null ? group.equals(pdmJob.group) : pdmJob.group == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (group != null ? group.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "PdmJob{" + "name='" + name + '\'' + ", group='" + group + '\'' + ", triggers size='" + triggers.size()
				+ '\'' + '}';
	}
}
