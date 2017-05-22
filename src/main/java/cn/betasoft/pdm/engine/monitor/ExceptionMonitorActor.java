package cn.betasoft.pdm.engine.monitor;

import cn.betasoft.pdm.engine.model.ExceptionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.DeadLetter;
import cn.betasoft.pdm.engine.config.akka.ActorBean;

@ActorBean
public class ExceptionMonitorActor extends AbstractActor {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionMonitorActor.class);

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ExceptionInfo.class, info -> {
			logger.info("exception method: {}, arguments: {},message: {},stack {}", info.getMethodName(),
					info.getArguments(), info.getMessage(), info.getStack());
		}).matchAny(o -> {
			logger.info("received unknown message");
		}).build();
	}
}
