package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.event.PdmEventBusImpl;
import cn.betasoft.pdm.engine.event.PdmMsgEnvelope;
import cn.betasoft.pdm.engine.model.MultiIndicatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * 从一个设备的多个采集指标中，综合处理数据 通过消息监听机制实现
 */
@ActorBean
public class MultipleIndicatorTaskActor extends AbstractActor {

	private static final Logger logger = LoggerFactory.getLogger(MultipleIndicatorTaskActor.class);

	private MultiIndicatorTask multiIndicatorTask;

	@Autowired
	private PdmEventBusImpl pdmEventBusImpl;

	public MultipleIndicatorTaskActor(MultiIndicatorTask multiIndicatorTask) {
		this.multiIndicatorTask = multiIndicatorTask;
	}

	@Override
	public void preStart() {
		logger.info("preStart,multiIndicatorTask is:" + multiIndicatorTask.getName());
		multiIndicatorTask.getIndicators().forEach(indicator -> {
			StringBuilder sb = new StringBuilder();
			sb.append(indicator.getMo().getDevice().getIp()).append("-");
			sb.append(indicator.getMo().getMoPath()).append("-");
			sb.append(indicator.getName());
			pdmEventBusImpl.subscribe(this.getSelf(), sb.toString());
		});
	}

	@Override
	public void postRestart(Throwable reason) {
		logger.info("postRestart,multiIndicatorTask is:" + multiIndicatorTask.getName());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		logger.info("preRestart,multiIndicatorTask is:" + multiIndicatorTask.getName());
		// Keep the call to postStop(), but no stopping of children
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, s -> {
			logger.info("Received String message: {}", s);
		}).match(SingleIndicatorTaskActor.Result.class, result -> {
			logger.info("receive indicator result: {}", result.getValue());
		}).matchAny(o -> {
			logger.info("received unknown message");
		}).build();
	}
}
