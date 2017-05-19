package cn.betasoft.pdm.engine.scheduler;

import javax.annotation.PostConstruct;

import cn.betasoft.pdm.engine.monitor.LogExecutionTime;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobServiceImpl implements JobService {

	@Autowired
	private Scheduler scheduler;

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	@PostConstruct
	public void testStart() {

	}

	/**
	 * 使用这个方法注册一个新的job,一个job有多个trigger
	 *
	 * @param job
	 * @return
	 * @throws SchedulerException
	 */
	@Override
	public boolean registerJob(PdmJob job) throws SchedulerException {
		final JobKey jobKey = job.getJobKey();
		if (scheduler.checkExists(jobKey)) {
			final JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			throw new SchedulerException("Already exist job [" + jobDetail + "] by key [" + jobKey + "] in Scheduler");
		}

		for (Trigger trigger : job.getTriggers()) {
			final TriggerKey triggerKey = trigger.getKey();
			final JobKey triggerJobKey = trigger.getJobKey();
			if (scheduler.checkExists(triggerKey)) {
				throw new SchedulerException("Already exist trigger,job is: [" + triggerJobKey + "] by key ["
						+ triggerKey + "] in Scheduler");
			}
		}

		final JobDetail jobDetail = job.createJobDetail();
		scheduler.scheduleJob(jobDetail, job.getTriggers(), true);
		logger.info("Register PdmJob {}", job);

		return true;
	}

	@Override
	public boolean existedJob(PdmJob job) throws SchedulerException {
		final JobKey jobKey = job.getJobKey();
		return scheduler.checkExists(jobKey);
	}

	/**
	 * 删除job和它所有的trigger
	 *
	 * @param job
	 * @return
	 * @throws SchedulerException
	 */
	@Override
	public boolean removeJob(PdmJob job) throws SchedulerException {
		final JobKey jobKey = job.getJobKey();
		boolean result = false;
		if (scheduler.checkExists(jobKey)) {
			result = scheduler.deleteJob(jobKey);
		}
		logger.info("Remove PdmJob {} result [{}]", jobKey, result);
		return result;
	}
}
