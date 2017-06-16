package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import com.fasterxml.jackson.databind.ObjectMapper;
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
			if (statistics.getReceiver().contains("/httpGetData")) {
				// 数据采集actor
				ObjectMapper objectMapper = new ObjectMapper();
				String value = objectMapper.writeValueAsString(statistics);

				actorSystem.actorSelection("/user/monitorSupervisor/kafkaProduce")
						.tell(new KafkaProduceActor.MonitorMessage("collectData", "", value), this.getSelf());
			}else if (statistics.getReceiver().contains("/st-")) {
				// 数据采集actor
				ObjectMapper objectMapper = new ObjectMapper();
				String value = objectMapper.writeValueAsString(statistics);

				actorSystem.actorSelection("/user/monitorSupervisor/kafkaProduce")
						.tell(new KafkaProduceActor.MonitorMessage("indicatorHandleData", "", value), this.getSelf());
			}
			logger.debug(
					"************************ actor statistics, actor is : {}, method is {}, entry time is {}, run time is: {}",
					statistics.getReceiver(), statistics.getMethodName(),
					sdf.format(new Date(statistics.getEntryTime())), statistics.getTotalTimeMillis());
		}).matchAny(o -> {
			logger.info("received unknown message");
		}).build();
	}
}
