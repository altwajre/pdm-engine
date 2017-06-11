package cn.betasoft.pdm.engine.stats;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import cn.betasoft.pdm.engine.model.Device;
import cn.betasoft.pdm.engine.model.Indicator;
import cn.betasoft.pdm.engine.model.ManagedObject;
import cn.betasoft.pdm.engine.model.SingleIndicatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ActorBean
public class RecipientList extends AbstractActor {

    static public class RootActor {

        private Device device;

        public RootActor(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }
    }

    private List<EngineActorStatus> rootActors = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(RecipientList.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RootActor.class, device -> {

        }).matchAny(o -> logger.info("received unknown message")).build();
    }

}
