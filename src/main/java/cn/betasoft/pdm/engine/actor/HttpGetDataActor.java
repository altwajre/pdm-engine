package cn.betasoft.pdm.engine.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.dispatch.*;
import akka.dispatch.forkjoin.ForkJoinPool;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;

import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.exception.DataCollectTimeOut;
import org.springframework.beans.factory.annotation.Autowired;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static akka.dispatch.Futures.future;

/**
 * 通过http方式查询数据
 */
@ActorBean
public class HttpGetDataActor extends AbstractActor {

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	private ExecutionContext ec;

	@Override
	public void preStart() {
		ec = actorSystem.dispatchers().lookup(akkaProperties.getFutureDispatch());
	}

	@Override
	public void postRestart(Throwable reason) {

	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(HttpGetData.class, httpGetData -> {
			Future<SingleIndicatorTaskActor.Result> delayed = Patterns.after(Duration.create(2, "seconds"),
					actorSystem.scheduler(), ec, failExcute(httpGetData));
			Future<SingleIndicatorTaskActor.Result> getDataFuture = getDataByHttp(httpGetData);
			Future<SingleIndicatorTaskActor.Result> result = Futures
					.firstCompletedOf(Arrays.asList(getDataFuture, delayed), ec);
			akka.pattern.Patterns.pipe(result, ec).to(sender());
		}).matchAny(o -> log.info("received unknown message")).build();
	}

	private Future<SingleIndicatorTaskActor.Result> getDataByHttp(HttpGetData httpGetData) {
		Future<SingleIndicatorTaskActor.Result> getDataFuture = future(new Callable<SingleIndicatorTaskActor.Result>() {

			public SingleIndicatorTaskActor.Result call() {
				log.info("command is {},task time is {} ,http collect start...", httpGetData.getCommand(),
						sdf.format(httpGetData.scheduledFireTime));
				Random random = new Random();
				int sleepTime = 100 + random.nextInt(2000);
				try {
					Thread.sleep(sleepTime);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// 把数据发送给所有这个采集指标下的任务，这些任务Actor以st-开头
				String value = httpGetData.getCommand() + "-" + sdf.format(new Date());
				SingleIndicatorTaskActor.Result result = new SingleIndicatorTaskActor.Result(
						httpGetData.getScheduledFireTime(), value);

				log.info("command is {},task time is {} ,http collect finish...", httpGetData.getCommand(),
						sdf.format(httpGetData.scheduledFireTime));
				return result;
			}

		}, ec);

		return getDataFuture;
	}

	private Future<SingleIndicatorTaskActor.Result> failExcute(HttpGetData httpGetData) {
		DataCollectTimeOut timeOut = new DataCollectTimeOut(httpGetData.scheduledFireTime,httpGetData.command);
		Future<SingleIndicatorTaskActor.Result> failExc = Futures.failed(timeOut);
		return failExc;
	}

	static public class HttpGetData {

		private final Date scheduledFireTime;

		private final String command;

		public HttpGetData(Date scheduledFireTime, String command) {
			this.scheduledFireTime = scheduledFireTime;
			this.command = command;
		}

		public Date getScheduledFireTime() {
			return scheduledFireTime;
		}

		public String getCommand() {
			return command;
		}
	}

}
