package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.perf.mailbox.MailboxStatistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

@ActorBean
public class MailBoxMonitorActor extends AbstractActor {

    @Autowired
    private ActorSystem actorSystem;

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger(MailBoxMonitorActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(MailboxStatistics.class, statistics -> {
            ObjectMapper objectMapper = new ObjectMapper();
            String value = objectMapper.writeValueAsString(statistics);
            actorSystem.actorSelection("/user/monitorSupervisor/kafkaProduce")
                    .tell(new KafkaProduceActor.MonitorMessage("mailboxData", "", value), this.getSelf());
        }).matchAny(o -> {
            logger.info("received unknown message");
        }).build();
    }
}
