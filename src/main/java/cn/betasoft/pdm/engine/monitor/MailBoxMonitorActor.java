package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.perf.mailbox.MailboxStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

@ActorBean
public class MailBoxMonitorActor extends AbstractActor {

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(MailBoxMonitorActor.class);

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(MailboxStatistics.class, statistics -> {
			logger.debug(
					"************************ mailbox statistics, actor is : {}, queue size is: {},entry time is {}, leave time is: {}",
					statistics.getReceiver(), statistics.getQueueSize(), sdf.format(statistics.getEntryTime()),
					sdf.format(statistics.getExitTime()));

		}).matchAny(o -> {
			logger.info("received unknown message");
		}).build();
	}
}
