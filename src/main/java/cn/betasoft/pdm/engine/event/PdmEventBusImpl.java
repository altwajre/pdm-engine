package cn.betasoft.pdm.engine.event;

import akka.actor.ActorRef;
import akka.event.japi.LookupEventBus;
import cn.betasoft.pdm.engine.monitor.LogExecutionTime;
import org.springframework.stereotype.Component;

@Component()
public class PdmEventBusImpl extends LookupEventBus<PdmMsgEnvelope, ActorRef, String> {

	@Override
	public String classify(PdmMsgEnvelope event) {
		return event.topic;
	}

	@Override
    public void publish(PdmMsgEnvelope event, ActorRef subscriber) {

		subscriber.tell(event.payload, ActorRef.noSender());
	}

	@Override
	public int compareSubscribers(ActorRef a, ActorRef b) {
		return a.compareTo(b);
	}

	@Override
	public int mapSize() {
		return 128;
	}

}