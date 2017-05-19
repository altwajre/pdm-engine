package cn.betasoft.pdm.engine.perf.mailbox;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.MailboxType;
import akka.dispatch.MessageQueue;
import akka.dispatch.ProducesMessageQueue;
import scala.Option;

public class MonitorMailboxType implements MailboxType, ProducesMessageQueue<MonitorQueue> {

	public MonitorMailboxType(ActorSystem.Settings settings, Config config) {

	}

	@Override
	public MessageQueue create(Option<ActorRef> owner, Option<ActorSystem> system) {
		if (system.nonEmpty()) {
			return new MonitorQueue(system.get());
		} else {
			throw new IllegalArgumentException("requires a system");
		}
	}

}
