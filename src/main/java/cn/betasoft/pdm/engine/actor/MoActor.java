package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.ManagedObject;
import cn.betasoft.pdm.engine.stats.EngineStatusActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 被监控一个资源，例如数据库，中间件，服务器
 */
@ActorBean
public class MoActor extends AbstractActor {

	static public class IndicatorInfo {

		private List<Indicator> indicatorList;

		public IndicatorInfo(List<Indicator> indicatorList) {
			this.indicatorList = indicatorList;
		}

		public List<Indicator> getIndicatorList() {
			return indicatorList;
		}
	}

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
		actorSystem.actorSelection("/user/supervisor/status").tell(new EngineStatusActor.MoAdd(), this.getSelf());
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		actorSystem.actorSelection("/user/supervisor/status").tell(new EngineStatusActor.MoMinus(), this.getSelf());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(IndicatorInfo.class, indicatorInfo -> {
			indicatorInfo.getIndicatorList().stream().forEach(indicator -> {
				if (!indicatorActorRefs.containsKey(indicator.getName())) {
					createIndicatorActor(indicator);
				}
			});
		}).matchAny(o -> logger.info("received unknown message")).build();
	}

	private void createIndicatorActor(Indicator indicator) {
		Props props = SpringProps.create(actorSystem, IndicatorActor.class, new Object[] { indicator });
		ActorRef actorRef = getContext().actorOf(props, "indi-" + indicator.getName());
		this.getContext().watch(actorRef);
		indicatorActorRefs.put(indicator.getName(), actorRef);

		// create single task
		actorRef.tell(new IndicatorActor.SingleIndicatorTaskInfo(indicator.getSingleIndicatorTasks()), this.getSelf());
		// create collect
		actorRef.tell(new IndicatorActor.CollectDataInfo(), this.getSelf());
	}
}
