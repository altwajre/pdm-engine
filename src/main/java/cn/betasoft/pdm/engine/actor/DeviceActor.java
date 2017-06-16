package cn.betasoft.pdm.engine.actor;

import akka.actor.*;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.*;
import cn.betasoft.pdm.engine.stats.EngineActorStatus;
import cn.betasoft.pdm.engine.stats.PdmEngineStatusActor;

import cn.betasoft.pdm.engine.stats.TreeNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.*;

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

	static public class MonitorTree {
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
	public void preStart() {
		actorSystem.actorSelection("/user/supervisor/status").tell(new PdmEngineStatusActor.DeviceAdd(),
				this.getSelf());
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		actorSystem.actorSelection("/user/supervisor/status").tell(new PdmEngineStatusActor.DeviceMinus(),
				this.getSelf());
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
		}).match(MonitorTree.class, message -> {
			EngineActorStatus actorStatus = getMoniorTree();
			getSender().tell(actorStatus, self());
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

	private EngineActorStatus getMoniorTree() {
		EngineActorStatus rootStatus = new EngineActorStatus();
		rootStatus.setName(device.getIp());
		rootStatus.setActorPath("/user/supervisor/d-" + device.getIp());
		rootStatus.setNodeType(TreeNodeType.DEVICE);

		for (ManagedObject mo : device.getMos()) {
			EngineActorStatus moStatus = new EngineActorStatus();
			moStatus.setName(mo.getName());
			moStatus.setActorPath(rootStatus.getActorPath() + "/mo-" + mo.getMoPath());
			moStatus.setNodeType(TreeNodeType.MANAGEDOBJECT);

			rootStatus.getChildren().add(moStatus);
			rootStatus.getAllDescendants().put(moStatus.getActorPath(), moStatus);

			for (Indicator indicator : mo.getIndicators()) {
				EngineActorStatus indicatorStatus = new EngineActorStatus();
				indicatorStatus.setName(indicator.getName());
				indicatorStatus.setActorPath(moStatus.getActorPath() + "/indi-" + indicator.getName());
				indicatorStatus.setNodeType(TreeNodeType.INDICATOR);

				moStatus.getChildren().add(indicatorStatus);
				rootStatus.getAllDescendants().put(indicatorStatus.getActorPath(), indicatorStatus);

				// collect
				EngineActorStatus collectStatus = new EngineActorStatus();
				collectStatus.setName("collect");
				collectStatus.setActorPath(indicatorStatus.getActorPath() + "/collect");
				collectStatus.setNodeType(TreeNodeType.COLLECT);

				indicatorStatus.getChildren().add(collectStatus);
				rootStatus.getAllDescendants().put(collectStatus.getActorPath(), collectStatus);

				// httpGet
				EngineActorStatus httpStatus = new EngineActorStatus();
				httpStatus.setName("httpGetData");
				httpStatus.setActorPath(collectStatus.getActorPath() + "/httpGetData");
				httpStatus.setNodeType(TreeNodeType.HTTPGET);

				collectStatus.getChildren().add(httpStatus);
				rootStatus.getAllDescendants().put(httpStatus.getActorPath(), httpStatus);

				for (SingleIndicatorTask singleTask : indicator.getSingleIndicatorTasks()) {
					EngineActorStatus singleTaskStatus = new EngineActorStatus();
					singleTaskStatus.setName(singleTask.getName());
					singleTaskStatus.setActorPath(indicatorStatus.getActorPath() + "/st-" + singleTask.getKey());
					if (singleTask.getType() == TaskType.ALARM) {
						singleTaskStatus.setNodeType(TreeNodeType.ALARM);
					} else {
						singleTaskStatus.setNodeType(TreeNodeType.RULE);
					}

					rootStatus.getAllDescendants().put(singleTaskStatus.getActorPath(), singleTaskStatus);
				}
			}

		}

		rootStatus.setShowData(createShowData(rootStatus));

		return rootStatus;
	}

	private String createShowData(EngineActorStatus root) {
		LocalActorRef ref = (LocalActorRef) this.getSelf();
		Cell cell = ref.underlying();
		ActorCell ac = (ActorCell) cell;
		int mailboxSize = ac.mailbox().numberOfMessages();

		int moNum = 0;
		int indicatorNum = 0;
		int alarmNum = 0;
		int ruleNum = 0;
		int descendantNum = 0;

		for (Map.Entry<String, EngineActorStatus> descendantMap : root.getAllDescendants().entrySet()) {
			if (descendantMap.getValue().getNodeType() == TreeNodeType.MANAGEDOBJECT) {
				moNum++;
				descendantNum++;
			} else if (descendantMap.getValue().getNodeType() == TreeNodeType.INDICATOR) {
				indicatorNum++;
				descendantNum++;
			} else if (descendantMap.getValue().getNodeType() == TreeNodeType.ALARM) {
				alarmNum++;
			} else if (descendantMap.getValue().getNodeType() == TreeNodeType.RULE) {
				ruleNum++;
			}
		}

		root.setDescendantNum(descendantNum);

		JsonObject value = Json.createObjectBuilder().add("ip地址", this.device.getIp()).add("未处理消息", mailboxSize)
				.add("子结点", Json.createObjectBuilder().add("mo", moNum).add("指标", indicatorNum).add("告警", alarmNum)
						.add("智维", ruleNum))
				.build();
		return value.toString();
	}
}
