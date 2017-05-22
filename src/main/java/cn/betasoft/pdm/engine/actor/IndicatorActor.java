package cn.betasoft.pdm.engine.actor;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import static akka.actor.SupervisorStrategy.escalate;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.SingleIndicatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一个采集指标，例如CPU，内存，接口
 */
@ActorBean
public class IndicatorActor extends AbstractActor {

	static public class SingleIndicatorTaskInfo {

		private List<SingleIndicatorTask> singleIndicatorTasks;

		public SingleIndicatorTaskInfo(List<SingleIndicatorTask> singleIndicatorTasks) {
			this.singleIndicatorTasks = singleIndicatorTasks;
		}

		public List<SingleIndicatorTask> getSingleIndicatorTasks() {
			return singleIndicatorTasks;
		}
	}

	static public class CollectDataInfo {
		public CollectDataInfo() {
		}
	}

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	// key:taskKey
	private Map<String, ActorRef> taskActorRefs = new HashMap<>();

	private ActorRef collectActorRef;

	public Indicator indicator;

	private static final Logger logger = LoggerFactory.getLogger(IndicatorActor.class);

	public IndicatorActor(Indicator indicator) {
		this.indicator = indicator;
	}

	private static SupervisorStrategy strategy = new OneForOneStrategy(10, Duration.create("1 minute"),
			DeciderBuilder.match(ArithmeticException.class, e -> resume()).match(NullPointerException.class, e -> {
				return restart();
			}).match(IllegalArgumentException.class, e -> {
				return stop();
			}).matchAny(o -> escalate()).build());

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SingleIndicatorTaskInfo.class, singleIndicatorTaskInfo -> {
			singleIndicatorTaskInfo.getSingleIndicatorTasks().stream().forEach(task -> {
				if (!taskActorRefs.containsKey(task.getKey())) {
					createTaskActor(task);
				}
			});
		}).match(CollectDataInfo.class, collectDataInfo -> {
			Props props = SpringProps.create(actorSystem, CollectDataActor.class, new Object[] { indicator })
					.withDispatcher(akkaProperties.getWorkDispatch());
			collectActorRef = getContext().actorOf(props, "collect");
			this.getContext().watch(collectActorRef);
		}).match(Terminated.class, t -> t.getActor().path().name().startsWith("st-"), t -> {
			taskActorRefs = taskActorRefs.entrySet().stream().filter(map -> !map.getValue().equals(t.getActor()))
					.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
			logger.info("childActorRefs size is:" + taskActorRefs.size());
		}).matchAny(o -> {
			logger.info("received unknown message" + o.toString());
		}).build();
	}

	private void createTaskActor(SingleIndicatorTask singleIndicatorTask) {
		Props props = null;
		if (singleIndicatorTask.getTopLevel() != null && singleIndicatorTask.getTopLevel()) {
			props = SpringProps
					.create(actorSystem, SingleIndicatorTaskActor.class, new Object[] { singleIndicatorTask })
					.withDispatcher(akkaProperties.getPinnedDispatcher());
		} else {
			props = SpringProps
					.create(actorSystem, SingleIndicatorTaskActor.class, new Object[] { singleIndicatorTask })
					.withDispatcher(akkaProperties.getWorkDispatch());
		}
		ActorRef actorRef = getContext().actorOf(props, "st-" + singleIndicatorTask.getKey());
		this.getContext().watch(actorRef);
		taskActorRefs.put(singleIndicatorTask.getKey(), actorRef);
	}
}
