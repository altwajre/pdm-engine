package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.model.monitor.MonitorMessage;
import cn.betasoft.pdm.engine.model.monitor.MonitorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;

import java.util.Date;
import java.util.Map;

@ActorBean
public class HeapMonitorActor extends AbstractActor {

	@Autowired
	private MetricsEndpoint metricsEndpoint;

	@Autowired
	private ActorSystem actorSystem;

	private static final Logger logger = LoggerFactory.getLogger(HeapMonitorActor.class);

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchEquals("Tick", m -> {
			Map<String, Object> result = metricsEndpoint.invoke();

			Long heap = (Long) result.get("heap") / 1024;
			Long heapInit = (Long) result.get("heap.init") / 1024;
			Long heapCommitted = (Long) result.get("heap.committed") / 1024;
			Long heapUsed = (Long) result.get("heap.used") / 1024;

			HeapInfo heapInfo = new HeapInfo();
			heapInfo.setSampleTime(new Date());
			heapInfo.setHeap(heap);
			heapInfo.setHeapInit(heapInit);
			heapInfo.setHeapCommitted(heapCommitted);
			heapInfo.setHeapUsed(heapUsed);

			ObjectMapper objectMapper = new ObjectMapper();
			String value = objectMapper.writeValueAsString(heapInfo);

			actorSystem.actorSelection("/user/monitorSupervisor/kafkaProduce")
					.tell(new KafkaProduceActor.MonitorMessage("heap", "", value), this.getSelf());
		}).matchAny(o -> logger.info("received unknown message")).build();
	}
}
