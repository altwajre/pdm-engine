package cn.betasoft.pdm.engine.monitor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.AkkaProperties;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

import static akka.pattern.Patterns.ask;

@Component
public class MonitorService {

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private AkkaProperties akkaProperties;

	private ActorRef monitorSupervisor;

	private String[] dispatcherNames = { "akka.actor.default-dispatcher", "pdm-work-dispatcher", "pdm-future-dispatcher" };

	@PostConstruct
	public void init() {
		Props props = SpringProps.create(actorSystem, MonitorSupervisor.class, null)
				.withDispatcher(akkaProperties.getMonitorDispatch());
		monitorSupervisor = actorSystem.actorOf(props, "monitorSupervisor");
		monitorSupervisor.tell(new MonitorSupervisor.CreateDispatcherMonitor(Arrays.asList(dispatcherNames)),
				ActorRef.noSender());
		monitorSupervisor.tell(new MonitorSupervisor.CreateMailboxMonitor(), ActorRef.noSender());
		monitorSupervisor.tell(new MonitorSupervisor.CreateActorMonitor(), ActorRef.noSender());
	}
}
