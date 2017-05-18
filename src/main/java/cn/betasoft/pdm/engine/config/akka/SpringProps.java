package cn.betasoft.pdm.engine.config.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class SpringProps {

	public static Props create(ActorSystem actorSystem, Class<? extends AbstractActor> requiredType, Object[] args) {
		return SpringExtension.instance().get(actorSystem).create(requiredType, args);
	}

	public static Props create(ActorSystem actorSystem, String actorBeanName,
			Class<? extends AbstractActor> requiredType) {
		return SpringExtension.instance().get(actorSystem).create(actorBeanName, requiredType);
	}
}
