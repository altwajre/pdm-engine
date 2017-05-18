package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.config.akka.SpringProps;
import org.springframework.beans.factory.annotation.Autowired;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

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

    @Autowired
    private ActorSystem actorSystem;

    private Map<String, ActorRef> dispatcherActorRefs = new HashMap<>();

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public MonitorSupervisor() {

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateDispatcherMonitor.class, dispatcherMonitor -> {
                    dispatcherMonitor.getDispatcherNames().forEach(name -> {
                        Props props = SpringProps.create(actorSystem, DispatcherMonitor.class, new Object[]{name});
                        ActorRef actorRef = actorSystem.actorOf(props, "monitor-" + name);
                        this.getContext().watch(actorRef);
                        dispatcherActorRefs.put(name, actorRef);
                    });

                    dispatcherActorRefs.forEach((name,actorRef) -> {
                        actorSystem.scheduler().schedule(Duration.create(5, TimeUnit.SECONDS),Duration.create(100, TimeUnit.MILLISECONDS),
                                actorRef, "Tick", actorSystem.dispatcher(), null);
                    });
                }).build();
    }
}
