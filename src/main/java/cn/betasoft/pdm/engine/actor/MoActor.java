package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.ManagedObject;
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

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public MoActor(ManagedObject mo) {
		this.mo = mo;
	}

	@Override
	public void preStart() {
		log.info("preStart,mo is:" + mo.toString());

		mo.getIndicators().stream().forEach(indicator -> {
			if (!indicatorActorRefs.containsKey(indicator.getName())) {
				createIndicatorActor(indicator);
			}
		});
	}

	@Override
	public void postRestart(Throwable reason) {
		log.info("postRestart,mo is:" + mo.toString());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		log.info("preRestart,mo is:" + mo.toString());
		// Keep the call to postStop(), but no stopping of children
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, s -> {
			log.info("Received String message: {}", s);
		}).matchAny(o -> log.info("received unknown message")).build();
	}

	private void createIndicatorActor(Indicator indicator) {
		Props props = SpringProps.create(actorSystem, IndicatorActor.class, new Object[] { indicator });
		ActorRef actorRef = getContext().actorOf(props, "indi-" + indicator.getName());
		this.getContext().watch(actorRef);
		indicatorActorRefs.put(indicator.getName(), actorRef);
	}
}
