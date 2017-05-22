package cn.betasoft.pdm.engine.config.aspectj;

import akka.actor.AbstractActor;
import cn.betasoft.pdm.engine.exception.InaccessablePointcutAnnotationException;
import cn.betasoft.pdm.engine.model.ExceptionInfo;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import com.google.common.base.Stopwatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import akka.actor.ActorSystem;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Aspect
public class MonitorActorAspect {

	@Autowired
	ActorSystem system;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Logger logger = LoggerFactory.getLogger(MonitorActorAspect.class);

	@Around("execution(* cn.betasoft.pdm.engine..*(..)) && @annotation(LogExecutionTime)")
	public void aroundHandler(ProceedingJoinPoint joinPoint) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);
		if (annotation == null) {
			throw new InaccessablePointcutAnnotationException();
		}
		String methodName = joinPoint.getSignature().getName();
		long start = System.currentTimeMillis();
		Stopwatch sw = Stopwatch.createStarted();
		joinPoint.proceed();
		sw.stop();
		AbstractActor actor = (AbstractActor) joinPoint.getTarget();
		ActorStatistics stat = new ActorStatistics(actor.getSelf().toString(), actor.getSender().toString(),
				methodName, start, sw.elapsed(TimeUnit.MILLISECONDS));
		system.eventStream().publish(stat);
	}

	@Around("execution(* cn.betasoft.pdm.engine..*(..)) && @annotation(FutureLogExecutionTime)")
	public Object aroundFuture(ProceedingJoinPoint joinPoint) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		FutureLogExecutionTime annotation = method.getAnnotation(FutureLogExecutionTime.class);
		if (annotation == null) {
			throw new InaccessablePointcutAnnotationException();
		}
		String methodName = joinPoint.getSignature().getName();
		long start = System.currentTimeMillis();
		Stopwatch sw = Stopwatch.createStarted();
		try {
			return joinPoint.proceed();
		} finally {
			sw.stop();
			AbstractActor actor = (AbstractActor) joinPoint.getTarget();
			ActorStatistics stat = new ActorStatistics(actor.getSelf().toString(), actor.getSender().toString(),
					methodName, start, sw.elapsed(TimeUnit.MILLISECONDS));
			system.eventStream().publish(stat);
		}
	}

	@AfterThrowing(pointcut = "execution(* cn.betasoft.pdm.engine..*(..))", throwing = "ex")
	public void processException(JoinPoint joinPoint, Throwable ex) {
		Signature signature = joinPoint.getSignature();
		String methodName = signature.getName();
		String arguments = Arrays.toString(joinPoint.getArgs());
		ExceptionInfo info = new ExceptionInfo(methodName,arguments,ex.getMessage(),ex.toString());
		system.eventStream().publish(info);
	}
}
