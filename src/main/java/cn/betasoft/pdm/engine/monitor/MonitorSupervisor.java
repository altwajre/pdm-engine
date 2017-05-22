package cn.betasoft.pdm.engine.monitor;

import akka.actor.*;

import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import cn.betasoft.pdm.engine.model.ExceptionInfo;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import cn.betasoft.pdm.engine.perf.mailbox.MailboxStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

@ActorBean
public class MonitorSupervisor extends AbstractActor {

	static public class CreateDispatcherMonitor {

		private final List<String> dispatcherNames;

		public CreateDispatcherMonitor(List<String> dispatcherNames) {
			this.dispatcherNames = dispatcherNames;
		}

		public List<String> getDispatcherNames() {
			return dispatcherNames;
		}
	}

	static public class CreateMailboxMonitor {

		public CreateMailboxMonitor() {
		}
	}

	static public class CreateActorMonitor {

		public CreateActorMonitor() {
		}
	}

	static public class CreateHeapMonitor {

		public CreateHeapMonitor() {
		}
	}

	static public class CreateDeadLetterMonitor {

		public CreateDeadLetterMonitor() {
		}
	}

	static public class CreateExceptionMonitor {

		public CreateExceptionMonitor() {
		}
	}

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	private Map<String, ActorRef> dispatcherActorRefs = new HashMap<>();

	private ActorRef mailBoxMonitorActorRef;

	private ActorRef actorMonitorActorRef;

	private ActorRef heapMonitorActorRef;

	private ActorRef deadLetterMonitorActorRef;

	private ActorRef exceptionMonitorActorRef;

	private ExecutionContext ec;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public MonitorSupervisor() {

	}

	@Override
	public void preStart() {
		ec = actorSystem.dispatchers().lookup(akkaProperties.getMonitorDispatch());
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
		return receiveBuilder().match(CreateDispatcherMonitor.class, dispatcherMonitor -> {
			//对每个线程池dispathcer,生成一个数据采集Actor
			dispatcherMonitor.getDispatcherNames().forEach(name -> {
				Props props = SpringProps.create(actorSystem, DispatcherMonitorActor.class, new Object[] { name })
						.withDispatcher(akkaProperties.getMonitorDispatch());
				ActorRef actorRef = this.getContext().actorOf(props, "monitor-" + name);
				this.getContext().watch(actorRef);
				dispatcherActorRefs.put(name, actorRef);
			});

			//每隔10秒采集数据
			dispatcherActorRefs.forEach((name, actorRef) -> {
				actorSystem.scheduler().schedule(Duration.create(5, TimeUnit.SECONDS),
						Duration.create(10, TimeUnit.SECONDS), actorRef, "Tick", ec, null);
			});
		}).match(CreateMailboxMonitor.class, mailboxMonitor -> {
			Props props = SpringProps.create(actorSystem, MailBoxMonitorActor.class, null)
					.withDispatcher(akkaProperties.getMonitorDispatch());
			mailBoxMonitorActorRef = this.getContext().actorOf(props, "maiboxMonitor");
			this.getContext().watch(mailBoxMonitorActorRef);
			actorSystem.eventStream().subscribe(mailBoxMonitorActorRef, MailboxStatistics.class);
		}).match(CreateActorMonitor.class, actorMonitor -> {
			Props props = SpringProps.create(actorSystem, ActorMonitorActor.class, null)
					.withDispatcher(akkaProperties.getMonitorDispatch());
			actorMonitorActorRef = this.getContext().actorOf(props, "actorMonitor");
			this.getContext().watch(actorMonitorActorRef);
			actorSystem.eventStream().subscribe(actorMonitorActorRef, ActorStatistics.class);
		}).match(CreateHeapMonitor.class, heapMonitor -> {
			Props props = SpringProps.create(actorSystem, HeapMonitorActor.class, null)
					.withDispatcher(akkaProperties.getMonitorDispatch());
			heapMonitorActorRef = this.getContext().actorOf(props, "heapMonitor");
			this.getContext().watch(actorMonitorActorRef);
			actorSystem.scheduler().schedule(Duration.create(5, TimeUnit.SECONDS),
					Duration.create(10, TimeUnit.SECONDS), heapMonitorActorRef, "Tick", ec, null);
		}).match(CreateDeadLetterMonitor.class, deadLetterMonitor -> {
			Props props = SpringProps.create(actorSystem, DeadLetterMonitorActor.class, null)
					.withDispatcher(akkaProperties.getMonitorDispatch());
			deadLetterMonitorActorRef = this.getContext().actorOf(props, "deadLetterMonitor");
			this.getContext().watch(deadLetterMonitorActorRef);
			actorSystem.eventStream().subscribe(deadLetterMonitorActorRef, DeadLetter.class);
		}).match(CreateExceptionMonitor.class, exceptionMonitor -> {
			Props props = SpringProps.create(actorSystem, ExceptionMonitorActor.class, null)
					.withDispatcher(akkaProperties.getMonitorDispatch());
			exceptionMonitorActorRef = this.getContext().actorOf(props, "exceptionMonitor");
			this.getContext().watch(exceptionMonitorActorRef);
			actorSystem.eventStream().subscribe(exceptionMonitorActorRef, ExceptionInfo.class);
		}).build();
	}
}
