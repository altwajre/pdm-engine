package cn.betasoft.pdm.engine.monitor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

import static akka.pattern.Patterns.ask;

@Component
public class MonitorService {

	@Autowired
	private ActorSystem actorSystem;

	private ActorRef monitorSupervisor;

	private String[] dispatcherNames = {"akka.actor.default-dispatcher","pdm-future-dispatcher"};

	private static final Logger logger = LoggerFactory.getLogger(MonitorService.class);

	@PostConstruct
	public void init(){
		Props props = SpringProps.create(actorSystem, MonitorSupervisor.class, null);
		monitorSupervisor = actorSystem.actorOf(props, "monitorSupervisor");
		monitorSupervisor.tell(new MonitorSupervisor.CreateDispatcherMonitor(Arrays.asList(dispatcherNames)),ActorRef.noSender());

	}
}
