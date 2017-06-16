package cn.betasoft.pdm.engine.actor;

import akka.actor.*;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.ManagedObject;
import cn.betasoft.pdm.engine.model.SingleIndicatorTask;
import cn.betasoft.pdm.engine.model.TaskType;
import cn.betasoft.pdm.engine.stats.PdmEngineStatusActor;

import cn.betasoft.pdm.engine.stats.ShowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	static public class GetShowData {
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
		actorSystem.actorSelection("/user/supervisor/status").tell(new PdmEngineStatusActor.MoAdd(), this.getSelf());
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		actorSystem.actorSelection("/user/supervisor/status").tell(new PdmEngineStatusActor.MoMinus(), this.getSelf());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(IndicatorInfo.class, indicatorInfo -> {
			indicatorInfo.getIndicatorList().stream().forEach(indicator -> {
				if (!indicatorActorRefs.containsKey(indicator.getName())) {
					createIndicatorActor(indicator);
				}
			});
		}).match(GetShowData.class, message -> {
			getSender().tell(createShowData(), self());
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

	private ShowData createShowData() {

		int indicatorNum = 0;
		int alarmNum = 0;
		int ruleNum = 0;

		for (Indicator indicator : this.mo.getIndicators()) {
			indicatorNum++;

			for (SingleIndicatorTask singleTask : indicator.getSingleIndicatorTasks()) {
				if (singleTask.getType() == TaskType.ALARM) {
					alarmNum++;
				} else {
					ruleNum++;
				}
			}
		}

		JsonObject value = Json.createObjectBuilder().add("MO名称", this.mo.getName()).add("moPath", this.mo.getMoPath())
				.add("子结点", Json.createObjectBuilder().add("采集指标", indicatorNum).add("告警规则", alarmNum).add("智维规则", ruleNum))
				.build();
		String path = this.getSelf().path().toString();
		int beginIndex = path.indexOf("/user/");
		return new ShowData(path.substring(beginIndex), value.toString());
	}
}
