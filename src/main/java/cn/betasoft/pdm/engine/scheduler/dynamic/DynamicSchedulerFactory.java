package cn.betasoft.pdm.engine.scheduler.dynamic;

import java.util.Date;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class DynamicSchedulerFactory {

	private static final Logger LOG = LoggerFactory.getLogger(DynamicSchedulerFactory.class);

	@Autowired
	private Scheduler scheduler;

	public DynamicSchedulerFactory() {
	}

	/**
	 * Register a job 添加(注册)一个动态的JOB
	 *
	 * @param job
	 *            DynamicJob
	 * @return True is register successful
	 * @throws SchedulerException
	 */
	public boolean registerJob(DynamicJob job) throws SchedulerException {
		final TriggerKey triggerKey = job.triggerKey();
		if (scheduler.checkExists(triggerKey)) {
			final Trigger trigger = scheduler.getTrigger(triggerKey);
			throw new SchedulerException(
					"Already exist trigger [" + trigger + "] by key [" + triggerKey + "] in Scheduler");
		}

		final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.cronExpression());
		final CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
				.withSchedule(cronScheduleBuilder).build();

		final JobDetail jobDetail = job.jobDetail();
		final Date date = scheduler.scheduleJob(jobDetail, cronTrigger);

		LOG.debug("Register DynamicJob {} on [{}]", job, date);
		return true;
	}

	/*
	 * Pause exist job 暂停已经存在的JOB
	 */
	public boolean pauseJob(DynamicJob existJob) throws SchedulerException {
		final TriggerKey triggerKey = existJob.triggerKey();
		boolean result = false;
		if (scheduler.checkExists(triggerKey)) {
			scheduler.pauseTrigger(triggerKey);
			result = true;
			LOG.debug("Pause exist DynamicJob {}, triggerKey [{}] successful", existJob, triggerKey);
		} else {
			LOG.debug("Failed pause exist DynamicJob {}, because not fount triggerKey [{}]", existJob, triggerKey);
		}
		return result;
	}

	/*
	 * Resume exist job 重启一个JOB
	 */
	public boolean resumeJob(DynamicJob existJob) throws SchedulerException {
		final TriggerKey triggerKey = existJob.triggerKey();
		boolean result = false;
		if (scheduler.checkExists(triggerKey)) {
			final CronTrigger newTrigger = existJob.cronTrigger();
			final Date date = scheduler.rescheduleJob(triggerKey, newTrigger);

			result = true;
			LOG.debug("Resume exist DynamicJob {}, triggerKey [{}] on [{}] successful", existJob, triggerKey, date);
		} else {
			LOG.debug("Failed resume exist DynamicJob {}, because not fount triggerKey [{}]", existJob, triggerKey);
		}
		return result;
	}

	/**
	 * Checking the job is existed or not 检查JOB是否存在
	 *
	 * @param job
	 *            Job
	 * @return True is existed, otherwise false
	 * @throws SchedulerException
	 */
	public boolean existedJob(DynamicJob job) throws SchedulerException {
		final TriggerKey triggerKey = job.triggerKey();
		return scheduler.checkExists(triggerKey);
	}

	/**
	 * Remove exists job 删除一个已经存在的JOB
	 *
	 * @param existJob
	 *            A DynamicJob which exists in Scheduler
	 * @return True is remove successful
	 * @throws SchedulerException
	 */
	public boolean removeJob(DynamicJob existJob) throws SchedulerException {
		final TriggerKey triggerKey = existJob.triggerKey();
		boolean result = false;
		if (scheduler.checkExists(triggerKey)) {
			result = scheduler.unscheduleJob(triggerKey);
		}

		LOG.debug("Remove DynamicJob {} result [{}]", existJob, result);
		return result;
	}
}