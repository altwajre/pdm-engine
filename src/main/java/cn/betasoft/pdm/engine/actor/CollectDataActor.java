package cn.betasoft.pdm.engine.actor;

import akka.actor.*;

import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.event.PdmEventBusImpl;
import cn.betasoft.pdm.engine.event.PdmMsgEnvelope;
import cn.betasoft.pdm.engine.exception.DataCollectTimeOut;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.SingleIndicatorTask;
import cn.betasoft.pdm.engine.model.TaskType;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import cn.betasoft.pdm.engine.perf.actor.ActorStatisticsType;
import cn.betasoft.pdm.engine.stats.ShowData;
import com.google.common.base.Stopwatch;
import com.google.common.collect.EvictingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import scala.concurrent.duration.Duration;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 通过指标参数，采集指标的值
 */
@ActorBean
public class CollectDataActor extends AbstractActor {

	static public class Collect {

		private final Date scheduledFireTime;

		public Collect(Date scheduledFireTime) {
			this.scheduledFireTime = scheduledFireTime;
		}

		public Date getScheduledFireTime() {
			return scheduledFireTime;
		}
	}

	static public class GetShowData {
	}

	@Autowired
	private PdmEventBusImpl pdmEventBusImpl;

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	private Indicator indicator;

	private Date scheduledFireTime;

	private ActorRef httpGetDataActorRef;

	// 采集命令
	private EvictingQueue<Date> commandQueue = EvictingQueue.create(5);

	// 采集结果
	private EvictingQueue<SingleIndicatorTaskActor.Result> resultQueue = EvictingQueue.create(5);

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(CollectDataActor.class);

	public CollectDataActor(Indicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public void preStart() {
		this.scheduledFireTime = new Date();
		Props props = SpringProps.create(actorSystem, HttpGetDataActor.class, null)
				.withDispatcher(akkaProperties.getWorkDispatch());
		httpGetDataActorRef = getContext().actorOf(props, "httpGetData");
		this.getContext().watch(httpGetDataActorRef);
	}

	@Override
	public void postRestart(Throwable reason) {
		logger.info("postRestart,indicator is:" + indicator.getName());
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		logger.info("preRestart,indicator is:" + indicator.getName());
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Collect.class, collect -> {
			Calendar collectCalendar = Calendar.getInstance();
			collectCalendar.setTime(this.scheduledFireTime);
			collectCalendar.set(Calendar.MILLISECOND, 0);

			Calendar fireCalendar = Calendar.getInstance();
			fireCalendar.setTime(collect.getScheduledFireTime());
			fireCalendar.set(Calendar.MILLISECOND, 0);

			if (fireCalendar.after(collectCalendar)) {
				logger.debug("indicator name is {},task time is {} ,start collect...", indicator.getName(),
						sdf.format(collect.getScheduledFireTime()));

				this.scheduledFireTime = collect.getScheduledFireTime();

				this.commandQueue.add(this.scheduledFireTime);

				HttpGetDataActor.HttpGetData httpGetData = new HttpGetDataActor.HttpGetData(
						collect.getScheduledFireTime(), indicator.getName());
				httpGetDataActorRef.tell(httpGetData, self());
			} else {
				logger.debug("current time is {},and task time is {},discard....", sdf.format(this.scheduledFireTime),
						sdf.format(collect.getScheduledFireTime()));
			}
		}).match(SingleIndicatorTaskActor.Result.class, result -> {
			logger.debug("result...task name is{}, time is {},[value] {}", indicator.getName(),
					sdf.format(result.getScheduledFireTime()), result.getValue());

			this.resultQueue.add(result);

			this.getContext().actorSelection("../st-*").tell(result, this.getSender());

			// 把消息发送给消息队列，以便综合指标处理actor使用
			StringBuilder sb = new StringBuilder();
			sb.append(indicator.getMo().getDevice().getIp()).append("-");
			sb.append(indicator.getMo().getMoPath()).append("-");
			sb.append(indicator.getName());
			pdmEventBusImpl.publish(new PdmMsgEnvelope(sb.toString(), result));
		}).match(Status.Failure.class, f -> {
			DataCollectTimeOut exception = (DataCollectTimeOut) f.cause();
			ActorStatistics stat = new ActorStatistics("/httpGetData", this.getSelf().toString(), "",
					new Date().getTime(), 2000l, ActorStatisticsType.TIMEOUT);
			this.actorSystem.eventStream().publish(stat);
			logger.debug("timeout........" + exception.getMessage());
		}).match(GetShowData.class, message -> {
			getSender().tell(createShowData(), self());
		}).matchAny(o -> {
			logger.debug("received unknown message" + o.getClass());
		}).build();
	}

	private ShowData createShowData() {
		LocalActorRef ref = (LocalActorRef) this.getSelf();
		Cell cell = ref.underlying();
		ActorCell ac = (ActorCell) cell;
		int mailboxSize = ac.mailbox().numberOfMessages();

		JsonArrayBuilder commandBuilder = Json.createArrayBuilder();
		Iterator<Date> commandIte = commandQueue.iterator();
		while (commandIte.hasNext()) {
			commandBuilder.add(sdf.format(commandIte.next()));
		}

		JsonArrayBuilder resultBuilder = Json.createArrayBuilder();
		Iterator<SingleIndicatorTaskActor.Result> resultIte = resultQueue.iterator();
		while (resultIte.hasNext()) {
			SingleIndicatorTaskActor.Result result = resultIte.next();
			resultBuilder.add(Json.createObjectBuilder().add("时间点", sdf.format(result.getScheduledFireTime()))
					.add("采集结果", result.getValue()));
		}

		JsonObject value = Json.createObjectBuilder().add("mailbox", mailboxSize).add("采集命令", commandBuilder)
				.add("采集结果", resultBuilder).build();

		String path = this.getSelf().path().toString();
		int beginIndex = path.indexOf("/user/");
		return new ShowData(path.substring(beginIndex), value.toString());
	}

}
