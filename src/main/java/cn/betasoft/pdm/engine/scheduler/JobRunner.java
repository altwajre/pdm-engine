package cn.betasoft.pdm.engine.scheduler;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.actor.CollectDataActor;
import cn.betasoft.pdm.engine.actor.Supervisor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JobRunner implements Job {

	@Autowired
	ActorSystem actorSystem;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final static Logger logger = LoggerFactory.getLogger(TargetDynamicJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		final String collectActorPath = (String) context.getMergedJobDataMap().get("collectActorPath");
		JobKey jobKey = context.getJobDetail().getKey();
		TriggerKey triggerKey = context.getTrigger().getKey();

		actorSystem.actorSelection(collectActorPath).tell(new CollectDataActor.Collect(context.getScheduledFireTime()),
				ActorRef.noSender());

		logger.info("[jobKey]" + jobKey + ",[triggerKey]" + triggerKey + ",[execute time]" + sdf.format(new Date())
				+ ",[scheduler time]" + sdf.format(context.getScheduledFireTime())+ ",[fire time]" + sdf.format(context.getFireTime()));
	}
}
