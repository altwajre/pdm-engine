package cn.betasoft.pdm.engine.perf.mailbox;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Envelope;
import akka.dispatch.MessageQueue;
import akka.dispatch.UnboundedMessageQueueSemantics;
import akka.event.LoggerMessageQueueSemantics;

public class MonitorQueue implements MessageQueue, UnboundedMessageQueueSemantics, LoggerMessageQueueSemantics {

	// 通过system获取eventStream
	private ActorSystem system;

	private final Queue<MonitorEnvelope> queue = new ConcurrentLinkedQueue<>();

	public MonitorQueue(ActorSystem system) {
		this.system = system;
	}

	@Override
	public void cleanUp(ActorRef owner, MessageQueue deadLetters) {
		if (hasMessages()) {
			Envelope envelope = dequeue();
			while (envelope != null) {
				// 送所有信息到dead-letter队列
				deadLetters.enqueue(owner, envelope);
				envelope = dequeue();
			}
		}
	}

	@Override
	public Envelope dequeue() {
		MonitorEnvelope monitor = queue.poll();
		if (monitor != null) {
			// 所有的消息都使用这个队列，如果是MailboxStatistics消息，不处理
			if (!(monitor.getHandle().message() instanceof MailboxStatistics)) {
				MailboxStatistics stat = new MailboxStatistics(monitor.getQueueSize(), monitor.getReceiver(),
						monitor.getHandle().sender().toString(), monitor.getEntryTime(), System.currentTimeMillis());
				this.system.eventStream().publish(stat);
				// 返回AKKA系统的原始消息
				return monitor.getHandle();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 创建一个MonitorEnvelop,加入队列中
	 */
	@Override
	public void enqueue(ActorRef receiver, Envelope handle) {
		MonitorEnvelope env = new MonitorEnvelope(queue.size() + 1, receiver.toString(), System.currentTimeMillis(),
				handle);
		queue.add(env);
		MailboxStatistics stat = new MailboxStatistics(env.getQueueSize(), env.getReceiver(),
				handle.sender().toString(), System.currentTimeMillis(),null);
		this.system.eventStream().publish(stat);
	}

	@Override
	public boolean hasMessages() {
		return !queue.isEmpty();
	}

	@Override
	public int numberOfMessages() {
		return queue.size();
	}
}
