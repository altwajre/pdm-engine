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
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;


@ActorBean
public class DispatcherMonitor extends AbstractActor {

	@Autowired
	private ActorSystem actorSystem;

	private String dispatcherName;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	public DispatcherMonitor(String dispatcherName) {
		this.dispatcherName = dispatcherName;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchEquals("Tick", m -> {
			Dispatcher dispatcher = (Dispatcher) actorSystem.dispatchers().lookup(dispatcherName);
			ExecutorServiceDelegate delegate = dispatcher.executorService();
			ForkJoinPool forkJoinPool = (ForkJoinPool) delegate.executor();
			log.info(">>>>>>>>>>>>>>>>>>>>>>Parallelism : {} , Active Threads : {},Task Count : {}", forkJoinPool.getParallelism(),
					forkJoinPool.getActiveThreadCount(), forkJoinPool.getQueuedTaskCount());

		}).matchAny(o -> log.info("received unknown message")).build();
	}
}
