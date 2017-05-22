package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Device;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.ManagedObject;
import cn.betasoft.pdm.engine.model.MultiIndicatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 被监控的一个网络设备，可以是一台服务器，或交换机
 */
@ActorBean
public class DeviceActor extends AbstractActor {

	static public class MoInfo {

		private List<ManagedObject> moList;

		public MoInfo(List<ManagedObject> moList) {
			this.moList = moList;
		}

		public List<ManagedObject> getMoList() {
			return moList;
		}
	}

	static public class MultiIndicatorTaskInfo {

		private List<MultiIndicatorTask> multiIndicatorTasks;

		public MultiIndicatorTaskInfo(List<MultiIndicatorTask> multiIndicatorTasks) {
			this.multiIndicatorTasks = multiIndicatorTasks;
		}

		public List<MultiIndicatorTask> getMultiIndicatorTasks() {
			return multiIndicatorTasks;
		}
	}

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	// key:moPath
	private Map<String, ActorRef> moActorRefs = new HashMap<>();

	// key: multiIndicatorTask name
	private Map<String, ActorRef> multiIndicatorActorRefs = new HashMap<>();

	private Device device;

	private static final Logger logger = LoggerFactory.getLogger(DeviceActor.class);

	public DeviceActor(Device device) {
		this.device = device;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(MoInfo.class, moInfo -> {
			moInfo.getMoList().stream().forEach(mo -> {
				if (!moActorRefs.containsKey(mo.getMoPath())) {
					createMoActor(mo);
				}
			});
		}).match(MultiIndicatorTaskInfo.class, multiIndicatorTaskInfo -> {
			multiIndicatorTaskInfo.getMultiIndicatorTasks().stream().forEach(mia -> {
				createMultiIndicatorTaskActor(mia);
			});
		}).matchAny(o -> logger.info("received unknown message")).build();
	}

	private void createMoActor(ManagedObject mo) {
		Props props = SpringProps.create(actorSystem, MoActor.class, new Object[] { mo });
		ActorRef actorRef = getContext().actorOf(props, "mo-" + mo.getMoPath());
		this.getContext().watch(actorRef);
		moActorRefs.put(mo.getMoPath(), actorRef);

		// create indicator actor
		actorRef.tell(new MoActor.IndicatorInfo(mo.getIndicators()), this.getSelf());
	}

	private void createMultiIndicatorTaskActor(MultiIndicatorTask multiIndicatorTask) {
		Props props = null;
		if (multiIndicatorTask.getTopLevel() != null && multiIndicatorTask.getTopLevel()) {
			props = SpringProps
					.create(actorSystem, MultipleIndicatorTaskActor.class, new Object[] { multiIndicatorTask })
					.withDispatcher(akkaProperties.getPinnedDispatcher());
		} else {
			props = SpringProps
					.create(actorSystem, MultipleIndicatorTaskActor.class, new Object[] { multiIndicatorTask })
					.withDispatcher(akkaProperties.getWorkDispatch());
		}
		ActorRef actorRef = getContext().actorOf(props, "mulIndi-" + multiIndicatorTask.getName());
		this.getContext().watch(actorRef);
		moActorRefs.put(multiIndicatorTask.getName(), actorRef);
	}
}
