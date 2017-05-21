package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;

import java.util.Map;

@ActorBean
public class HeapMonitorActor extends AbstractActor {

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    private static final Logger logger = LoggerFactory.getLogger(HeapMonitorActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("Tick", m -> {
            Map<String,Object> result = metricsEndpoint.invoke();

            Long heap = (Long) result.get("heap") / 1024;
            Long heapInit = (Long)result.get("heap.init") / 1024;
            Long heapCommitted = (Long)result.get("heap.committed") / 1024;
            Long heapUsed = (Long)result.get("heap.used") / 1024;

            logger.info(
                    ">>>heap metrics,heap : {} , heap init : {},heap committed : {}, heap used: {}",
                    heap,heapInit,heapCommitted,heapUsed);

        }).matchAny(o -> logger.info("received unknown message")).build();
    }
}
