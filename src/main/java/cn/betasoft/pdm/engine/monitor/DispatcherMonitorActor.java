package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.dispatch.Dispatcher;
import akka.dispatch.ExecutorServiceDelegate;
import akka.dispatch.Futures;
import akka.dispatch.forkjoin.ForkJoinPool;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.scheduler.JobTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Optional;

@ActorBean
public class DispatcherMonitorActor extends AbstractActor {

	@Autowired
	private ActorSystem actorSystem;

	private String dispatcherName;

	private ForkJoinPool forkJoinPool;

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
		forkJoinPool = (ForkJoinPool) delegate.executor();
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
			logger.info(
					">>>>>>>>>>>>>>>>>>>>>>{},Parallelism : {} , Active Threads : {},Task Count : {}, pool size: {},running thread count: {}, queue submission count: {}",
					dispatcherName, forkJoinPool.getParallelism(), forkJoinPool.getActiveThreadCount(),
					forkJoinPool.getQueuedTaskCount(), forkJoinPool.getPoolSize(), forkJoinPool.getRunningThreadCount(),
					forkJoinPool.getQueuedSubmissionCount());

		}).matchAny(o -> logger.info("received unknown message")).build();
	}
}
