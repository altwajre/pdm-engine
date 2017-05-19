package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
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

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	private Map<String, ActorRef> dispatcherActorRefs = new HashMap<>();

	private ActorRef mailBoxMonitorActorRef;

	private ActorRef actorMonitorActorRef;

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
			dispatcherMonitor.getDispatcherNames().forEach(name -> {
				Props props = SpringProps.create(actorSystem, DispatcherMonitorActor.class, new Object[] { name })
						.withDispatcher(akkaProperties.getMonitorDispatch());
				ActorRef actorRef = this.getContext().actorOf(props, "monitor-" + name);
				this.getContext().watch(actorRef);
				dispatcherActorRefs.put(name, actorRef);
			});

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
		}).build();
	}
}
