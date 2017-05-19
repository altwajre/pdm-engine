package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Device;
import cn.betasoft.pdm.engine.model.ManagedObject;
import cn.betasoft.pdm.engine.model.MultiIndicatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 被监控的一个网络设备，可以是一台服务器，或交换机
 */
@ActorBean
public class DeviceActor extends AbstractActor {

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
	public void preStart() {
		logger.info("preStart,device is:" + device.toString());
		device.getMos().stream().forEach(mo -> {
			if (!moActorRefs.containsKey(mo.getMoPath())) {
				createMoActor(mo);
			}
		});
		device.getMultiIndicatorTasks().stream().forEach(mia -> {
			createMultiIndicatorTaskActor(mia);
		});
	}

	@Override
	public void postRestart(Throwable reason) {
		logger.info("postRestart,device is:" + device.toString());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		logger.info("preRestart,device is:" + device.toString());
		// Keep the call to postStop(), but no stopping of children
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, s -> {
			logger.info("Received String message: {}", s);
		}).matchAny(o -> logger.info("received unknown message")).build();
	}

	private void createMoActor(ManagedObject mo) {
		Props props = SpringProps.create(actorSystem, MoActor.class, new Object[] { mo });
		ActorRef actorRef = getContext().actorOf(props, "mo-" + mo.getMoPath());
		this.getContext().watch(actorRef);
		moActorRefs.put(mo.getMoPath(), actorRef);
	}

	private void createMultiIndicatorTaskActor(MultiIndicatorTask multiIndicatorTask) {
		Props props = null;
		if (multiIndicatorTask.getTopLevel() != null && multiIndicatorTask.getTopLevel()) {
			props = SpringProps
					.create(actorSystem, MultipleIndicatorTaskActor.class, new Object[] { multiIndicatorTask })
					.withDispatcher(akkaProperties.getPinnedDispatcher());
		} else {
			props = SpringProps.create(actorSystem, MultipleIndicatorTaskActor.class,
					new Object[] { multiIndicatorTask }).withDispatcher(akkaProperties.getWorkDispatch());
		}
		ActorRef actorRef = getContext().actorOf(props, "mulIndi-" + multiIndicatorTask.getName());
		this.getContext().watch(actorRef);
		moActorRefs.put(multiIndicatorTask.getName(), actorRef);
	}
}
