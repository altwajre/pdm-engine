package cn.betasoft.pdm.engine.monitor;


import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.exception.InaccessablePointcutAnnotationException;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import com.google.common.base.Stopwatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.actor.ActorSystem;

import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
public class MonitorActorAspect {

	@Autowired
	ActorSystem system;

	@Around("@annotation(cn.betasoft.pdm.engine.monitor.LogExecutionTime)")
	public Object aroundOnReceive(ProceedingJoinPoint joinPoint) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
		if (annotation == null) {
			throw new InaccessablePointcutAnnotationException();
		}
		long start = System.currentTimeMillis();
		Stopwatch sw = Stopwatch.createStarted();
		try {
			return joinPoint.proceed();
		} finally {
			sw.stop();
			AbstractActor actor = (AbstractActor) joinPoint.getTarget();
			ActorStatistics stat = new ActorStatistics(actor.getSelf().toString(), actor.getSender().toString(), start,
					sw.elapsed(TimeUnit.MILLISECONDS));
			system.eventStream().publish(stat);
		}
	}

}
