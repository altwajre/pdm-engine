package cn.betasoft.pdm.engine.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetDynamicJob implements Job {

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final static Logger logger = LoggerFactory.getLogger(TargetDynamicJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		TriggerKey triggerKey = context.getTrigger().getKey();
		Date fireTime = context.getFireTime();
		logger.info("[jobKey]" + jobKey + ",[triggerKey]" + triggerKey + ",[execute time]" + sdf.format(new Date())
				+ ",[fire time]" + sdf.format(fireTime));
	}
}
