package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.SingleIndicatorTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 一个采集指标，例如CPU，内存，接口
 */
@ActorBean
public class IndicatorActor extends AbstractActor {

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	// key:taskKey
	private Map<String, ActorRef> taskActorRefs = new HashMap<>();

	private ActorRef collectActorRef;

	public Indicator indicator;

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public IndicatorActor(Indicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public void preStart() {
		log.info("preStart,indicator is:" + indicator.toString());

		indicator.getSingleIndicatorTasks().stream().forEach(task -> {
			if (!taskActorRefs.containsKey(task.getKey())) {
				createTaskActor(task);
			}
		});

		Props props = SpringProps.create(actorSystem, CollectDataActor.class, new Object[] { indicator });
		collectActorRef = getContext().actorOf(props, "collect");
		this.getContext().watch(collectActorRef);
	}

	@Override
	public void postRestart(Throwable reason) {
		log.info("postRestart,indicator is:" + indicator.toString());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		log.info("preRestart,indicator is:" + indicator.toString());
		// Keep the call to postStop(), but no stopping of children
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, s -> {
			log.info("Received String message: {}", s);
		}).matchAny(o -> log.info("received unknown message")).build();
	}

	private void createTaskActor(SingleIndicatorTask singleIndicatorTask) {
		Props props = null;
		if (singleIndicatorTask.getTopLevel() != null && singleIndicatorTask.getTopLevel()) {
			props = SpringProps
					.create(actorSystem, SingleIndicatorTaskActor.class, new Object[] { singleIndicatorTask })
					.withDispatcher(akkaProperties.getPinnedDispatcher());
		} else {
			props = SpringProps.create(actorSystem, SingleIndicatorTaskActor.class,
					new Object[] { singleIndicatorTask });
		}
		ActorRef actorRef = getContext().actorOf(props, "st-" + singleIndicatorTask.getKey());
		this.getContext().watch(actorRef);
		taskActorRefs.put(singleIndicatorTask.getKey(), actorRef);
	}
}
