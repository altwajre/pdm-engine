package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.*;
import cn.betasoft.pdm.engine.stats.EngineActorStatus;
import cn.betasoft.pdm.engine.stats.EngineStatusActor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	static public class Status { }

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
		actorSystem.actorSelection("/user/supervisor/status").tell(new EngineStatusActor.DeviceAdd(), this.getSelf());
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		actorSystem.actorSelection("/user/supervisor/status").tell(new EngineStatusActor.DeviceMinus(), this.getSelf());
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
		}).match(Status.class,message->{
			EngineActorStatus actorStatus = createActorStatus();
			getSender().tell(actorStatus,self());
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

	private EngineActorStatus createActorStatus(){
		List<EngineActorStatus> actorStatuses = new ArrayList<>();

		EngineActorStatus rootStatus = new EngineActorStatus();
		rootStatus.setName(device.getIp());
		rootStatus.setActorPath("/user/supervisor/d-" + device.getIp());

		actorStatuses.add(rootStatus);
		for(ManagedObject mo : device.getMos()) {
			EngineActorStatus moStatus = new EngineActorStatus();
			moStatus.setName(mo.getName());
			moStatus.setActorPath(rootStatus.getActorPath() + "/mo-" + mo.getMoPath());

			rootStatus.getChildren().add(moStatus);
			actorStatuses.add(moStatus);

			for(Indicator indicator : mo.getIndicators()) {
				EngineActorStatus indicatorStatus = new EngineActorStatus();
				indicatorStatus.setName(indicator.getName());
				indicatorStatus.setActorPath(moStatus.getActorPath() + "/indi-" + indicator.getName());

				moStatus.getChildren().add(indicatorStatus);
				actorStatuses.add(indicatorStatus);

				//collect
				EngineActorStatus collectStatus = new EngineActorStatus();
				collectStatus.setName("collect");
				collectStatus.setActorPath(indicatorStatus.getActorPath() + "/collect");

				indicatorStatus.getChildren().add(collectStatus);
				actorStatuses.add(collectStatus);

				//httpGet
				EngineActorStatus httpStatus = new EngineActorStatus();
				httpStatus.setName("httpGetData");
				httpStatus.setActorPath(collectStatus.getActorPath() + "/httpGetData");

				collectStatus.getChildren().add(httpStatus);
				actorStatuses.add(httpStatus);

				for (SingleIndicatorTask singleTask : indicator.getSingleIndicatorTasks()) {
					EngineActorStatus singleTaskStatus = new EngineActorStatus();
					singleTaskStatus.setName(singleTask.getName());
					singleTaskStatus.setActorPath(indicatorStatus.getActorPath() + "/st-" + singleTask.getKey());

					indicatorStatus.getChildren().add(singleTaskStatus);
					actorStatuses.add(singleTaskStatus);
				}
			}

		}

		return rootStatus;
	}

	private String createShowData(){
		JsonObject value = Json.createObjectBuilder()
				.add("firstName", "John")
				.add("lastName", "Smith")
				.add("age", 25)
				.add("address", Json.createObjectBuilder()
						.add("streetAddress", "21 2nd Street")
						.add("city", "New York")
						.add("state", "NY")
						.add("postalCode", "10021"))
				.add("phoneNumber", Json.createArrayBuilder()
						.add(Json.createObjectBuilder()
								.add("type", "home")
								.add("number", "212 555-1234"))
						.add(Json.createObjectBuilder()
								.add("type", "fax")
								.add("number", "646 555-4567")))
				.build();
		return value.toString();
	}
}
