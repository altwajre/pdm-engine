package cn.betasoft.pdm.engine.actor;

import akka.actor.*;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.event.PdmEventBusImpl;
import cn.betasoft.pdm.engine.event.PdmMsgEnvelope;
import cn.betasoft.pdm.engine.exception.DataCollectTimeOut;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.monitor.LogExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

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

	@Autowired
	private PdmEventBusImpl pdmEventBusImpl;

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	private Indicator indicator;

	private Date scheduledFireTime;

	private ActorRef httpGetDataActorRef;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(CollectDataActor.class);

	public CollectDataActor(Indicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public void preStart() {
		logger.info("preStart,indicator is:" + indicator.getName());
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
	@LogExecutionTime
	public Receive createReceive() {
		return receiveBuilder().match(Collect.class, collect -> {
			Calendar collectCalendar = Calendar.getInstance();
			collectCalendar.setTime(this.scheduledFireTime);
			collectCalendar.set(Calendar.MILLISECOND, 0);

			Calendar fireCalendar = Calendar.getInstance();
			fireCalendar.setTime(collect.getScheduledFireTime());
			fireCalendar.set(Calendar.MILLISECOND, 0);

			if (fireCalendar.after(collectCalendar)) {
				logger.info("task time is {} ,start collect...", sdf.format(collect.getScheduledFireTime()));

				this.scheduledFireTime = collect.getScheduledFireTime();

				HttpGetDataActor.HttpGetData httpGetData = new HttpGetDataActor.HttpGetData(
						collect.getScheduledFireTime(), indicator.getName());
				httpGetDataActorRef.tell(httpGetData, self());
			} else {
				logger.info("current time is {},and task time is {},discard....", sdf.format(this.scheduledFireTime),
						sdf.format(collect.getScheduledFireTime()));
			}
		}).match(SingleIndicatorTaskActor.Result.class, result -> {
			logger.info("result [value] {}", result.getValue());
			this.getContext().actorSelection("../st-*").tell(result, this.getSender());

			// 把消息发送给消息队列，以便综合指标处理actor使用
			StringBuilder sb = new StringBuilder();
			sb.append(indicator.getMo().getDevice().getIp()).append("-");
			sb.append(indicator.getMo().getMoPath()).append("-");
			sb.append(indicator.getName());
			pdmEventBusImpl.publish(new PdmMsgEnvelope(sb.toString(), result));
		}).match(Status.Failure.class, f -> {
			DataCollectTimeOut exception = (DataCollectTimeOut) f.cause();
			logger.info("timeout........" + exception.getMessage());
		}).matchAny(o -> {
			logger.info("received unknown message" + o.getClass());
		}).build();
	}

}
