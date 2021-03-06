package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.dispatch.Dispatcher;
import akka.dispatch.ExecutorServiceDelegate;
import akka.dispatch.forkjoin.ForkJoinPool;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.DispatcherType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@ActorBean
public class DispatcherMonitorActor extends AbstractActor {

	@Autowired
	private ActorSystem actorSystem;

	private String dispatcherName;

	private ForkJoinPool forkJoinPool;

	private ThreadPoolExecutor threadPool;

	private DispatcherType dispatcherType;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(DispatcherMonitorActor.class);

	public DispatcherMonitorActor(String dispatcherName) {
		this.dispatcherName = dispatcherName;
	}

	@Override
	public void preStart() {
		logger.info("preStart,dispatcher monitor name is {}", dispatcherName);
		Dispatcher dispatcher = (Dispatcher) actorSystem.dispatchers().lookup(dispatcherName);
		ExecutorServiceDelegate delegate = dispatcher.executorService();
		if(delegate.executor() instanceof ForkJoinPool){
			forkJoinPool = (ForkJoinPool) delegate.executor();
			dispatcherType = DispatcherType.FORKJOIN;
		}else if(delegate.executor() instanceof ThreadPoolExecutor){
			threadPool = (ThreadPoolExecutor) delegate.executor();
			dispatcherType = DispatcherType.THREADPOOL;
		}

	}

	@Override
	public void postRestart(Throwable reason) {
		logger.info("postRestart,dispatcher monitor name is {}", dispatcherName);
	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
		logger.info("preRestart dispatcher monitor name is {}", dispatcherName);
		// Keep the call to postStop(), but no stopping of children
		postStop();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchEquals("Tick", m -> {
			DispatcherInfo info = new DispatcherInfo();
			info.setSampleTime(new Date());
			info.setType(this.dispatcherType);

			if(this.dispatcherType == DispatcherType.FORKJOIN){
				long parallelism = forkJoinPool.getParallelism();
				long activeThreadCount = forkJoinPool.getActiveThreadCount();
				long queuedTaskCount = forkJoinPool.getQueuedTaskCount();
				long poolSize = forkJoinPool.getPoolSize();
				long runningThreadCount = forkJoinPool.getRunningThreadCount();
				long queuedSubmissionCount = forkJoinPool.getQueuedSubmissionCount();

				info.setParallelism(parallelism);
				info.setActiveThreadCount(activeThreadCount);
				info.setQueuedTaskCount(queuedTaskCount);
				info.setPoolSize(poolSize);
				info.setRunningThreadCount(runningThreadCount);
				info.setQueuedSubmissionCount(queuedSubmissionCount);

			}else {
				long corePoolSize = threadPool.getCorePoolSize();
				long maximumPoolSize = threadPool.getMaximumPoolSize();
				long poolSize = threadPool.getPoolSize();
				long activeCount = threadPool.getActiveCount();
				long queuedTaskCount = threadPool.getQueue().size();

				info.setCorePoolSize(corePoolSize);
				info.setMaximumPoolSize(maximumPoolSize);
				info.setPoolSize(poolSize);
				info.setActiveThreadCount(activeCount);
				info.setQueuedSubmissionCount(queuedTaskCount);
			}

			ObjectMapper objectMapper = new ObjectMapper();
			String value = objectMapper.writeValueAsString(info);

			actorSystem.actorSelection("/user/monitorSupervisor/kafkaProduce").tell(
					new KafkaProduceActor.MonitorMessage(dispatcherName, "", value),
					this.getSelf());

			if (logger.isDebugEnabled()) {
				logger.debug(
						">>>>>>>>>>>>>>>>>>>>>>{},Parallelism : {} , Active Threads : {},Task Count : {}, pool size: {},running thread count: {}, queue submission count: {}",
						dispatcherName, forkJoinPool.getParallelism(), forkJoinPool.getActiveThreadCount(),
						forkJoinPool.getQueuedTaskCount(), forkJoinPool.getPoolSize(),
						forkJoinPool.getRunningThreadCount(), forkJoinPool.getQueuedSubmissionCount());
			}
		}).matchAny(o -> logger.info("received unknown message")).build();
	}
}
