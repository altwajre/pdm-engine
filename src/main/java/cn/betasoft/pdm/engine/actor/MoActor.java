package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.ManagedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 被监控一个资源，例如数据库，中间件，服务器
 */
@ActorBean
public class MoActor extends AbstractActor {

	@Autowired
	private ActorSystem actorSystem;

	// key:indicatorName
	private Map<String, ActorRef> indicatorActorRefs = new HashMap<>();

	private ManagedObject mo;

	private static final Logger logger = LoggerFactory.getLogger(MoActor.class);

	public MoActor(ManagedObject mo) {
		this.mo = mo;
	}

	@Override
	public void preStart() {
		logger.info("preStart,mo is:" + mo.toString());

		mo.getIndicators().stream().forEach(indicator -> {
			if (!indicatorActorRefs.containsKey(indicator.getName())) {
				createIndicatorActor(indicator);
			}
		});
	}

	@Override
	public void postRestart(Throwable reason) {
		logger.info("postRestart,mo is:" + mo.toString());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		logger.info("preRestart,mo is:" + mo.toString());
		// Keep the call to postStop(), but no stopping of children
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, s -> {
			logger.info("Received String message: {}", s);
		}).matchAny(o -> logger.info("received unknown message")).build();
	}

	private void createIndicatorActor(Indicator indicator) {
		Props props = SpringProps.create(actorSystem, IndicatorActor.class, new Object[] { indicator });
		ActorRef actorRef = getContext().actorOf(props, "indi-" + indicator.getName());
		this.getContext().watch(actorRef);
		indicatorActorRefs.put(indicator.getName(), actorRef);
	}
}
