package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.model.SingleIndicatorTask;
import cn.betasoft.pdm.engine.config.aspectj.LogExecutionTime;
import com.google.common.collect.EvictingQueue;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 告警或智维工作类 它们与数据采集Actor属于兄弟关系
 */
@ActorBean
public class SingleIndicatorTaskActor extends AbstractActor {

	static public class Result {

		private final Date scheduledFireTime;

		private final String value;

		public Result(Date scheduledFireTime, String value) {
			this.scheduledFireTime = scheduledFireTime;
			this.value = value;
		}

		public Date getScheduledFireTime() {
			return scheduledFireTime;
		}

		public String getValue() {
			return value;
		}

	}

	private SingleIndicatorTask task;

	private CronExpression fireCronExpression;

	private List<CronExpression> holidayCronExpressions = new ArrayList<>();

	// 采集数据缓存队列，它是一个环形队列
	// 例如需要通过3个采集数据判断状态，队列长度为3
	private EvictingQueue<Result> resultQueue;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(SingleIndicatorTaskActor.class);

	public SingleIndicatorTaskActor(SingleIndicatorTask task) {
		this.task = task;
	}

	@Override
	public void preStart() {
		//logger.info("preStart,indicator is: {}, task is: {}", task.getIndicator().getName(), task.toString());
		init();
	}

	@Override
	public void postRestart(Throwable reason) {
		logger.info("postRestart,task is:" + task.toString());
		init();
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		logger.info("preRestart,task is:" + task.toString());
		postStop();
	}

	@Override public void postStop() throws Exception {
		//logger.info("postStop,task is:" + task.toString());
		super.postStop();
	}

	private void init() {
		try {
			fireCronExpression = new CronExpression(task.getCronExpression());
			for (String holiday : task.getHolidayCronExrpessions()) {

				CronExpression cronExpression = new CronExpression(holiday);
				holidayCronExpressions.add(cronExpression);
			}
		} catch (ParseException ex) {
			logger.info("parse cron error", ex);
		}
		resultQueue = EvictingQueue.create(task.getIndicatorNum());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, s -> {
			logger.info("Received String message: {}", s);
		}).match(Result.class, result -> {
			resultHandler(result);
		}).matchAny(o -> logger.info("received unknown message")).build();
	}

	@LogExecutionTime
	private void resultHandler(Result result) {
		Date schedulerFireTime = result.getScheduledFireTime();
		if (fireCronExpression.isSatisfiedBy(schedulerFireTime)) {
			boolean isHoliday = inHoliday(schedulerFireTime);
			if (isHoliday) {
				logger.debug(
						"receive result,task name is: {}, task type is: {}, schedulerTime is: {} in holiday,value is:{}",
						task.getName(), task.getType(), sdf.format(result.getScheduledFireTime()), result.getValue());
			} else {
				logger.debug(
						"receive result in cron,indicator is {},task name is: {},key is: {}, type is: {}, schedulerTime is: {} ,value is:{}",
						task.getIndicator().getName(), task.getName(), task.getKey(), task.getType(),
						sdf.format(result.getScheduledFireTime()), result.getValue());

				resultQueue.add(result);

				if (resultQueue.size() == task.getIndicatorNum()) {
					Random random = new Random();
					int sleepTime = 100 + random.nextInt(1000);
					try {
						Thread.sleep(sleepTime);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					logger.debug(resultQueue.size() + ">>>>>>>>>>>>>>>>>>>excute finish>>>>>>>>>>>>>>>>>>>>>>");

				}
			}
		}
		//throw new IllegalArgumentException("1111111");
	}

	public boolean inHoliday(Date schedulerFireTime) {
		for (CronExpression cronExpression : this.holidayCronExpressions) {
			if (cronExpression.isSatisfiedBy(schedulerFireTime)) {
				return true;
			}
		}
		return false;
	}
}
