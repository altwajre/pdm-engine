package cn.betasoft.pdm.engine.monitor;

import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.actor.ActorSystem;
import akka.actor.AbstractActor;

@Aspect
@Component
public class MonitorActorAspect {

	@Autowired
	ActorSystem system;

	@Around("@annotation(LogExecutionTime)")
	public Object aroundOnReceive(ProceedingJoinPoint pjp) throws Throwable {
		long start = System.currentTimeMillis();
		Object retVal = pjp.proceed();
		long end = System.currentTimeMillis();
		//AbstractActor actor = (AbstractActor) pjp.getTarget();
		//ActorStatistics stat = new ActorStatistics(actor.getSelf().toString(), actor.getSender().toString(), start,
				//end);
		//system.eventStream().publish(stat);
		return retVal;
	}
}
