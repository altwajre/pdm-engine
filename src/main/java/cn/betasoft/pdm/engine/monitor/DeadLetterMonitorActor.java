package cn.betasoft.pdm.engine.monitor;

import akka.actor.AbstractActor;
import akka.actor.DeadLetter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActorBean
public class DeadLetterMonitorActor extends AbstractActor {

	private static final Logger logger = LoggerFactory.getLogger(DeadLetterMonitorActor.class);

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(DeadLetter.class, deadLetter -> {
			logger.info("^^^^^^^^^^^^ sender : {}, receiver: {},message: {}", deadLetter.sender(),
					deadLetter.recipient(), deadLetter.message());
		}).matchAny(o -> {
			logger.info("received unknown message");
		}).build();
	}
}
