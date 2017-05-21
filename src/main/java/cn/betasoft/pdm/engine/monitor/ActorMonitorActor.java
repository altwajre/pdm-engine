package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

@ActorBean
public class ActorMonitorActor extends AbstractActor {

	@Autowired
	private ActorSystem actorSystem;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(ActorMonitorActor.class);

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ActorStatistics.class, statistics -> {
			logger.info(
					"************************ actor statistics, actor is : {}, entry time is {}, run time is: {}",
					statistics.getReceiver(), sdf.format(new Date(statistics.getEntryTime())),
					statistics.getTotalTimeMillis());
		}).matchAny(o -> {
			logger.info("received unknown message");
		}).build();
	}
}
