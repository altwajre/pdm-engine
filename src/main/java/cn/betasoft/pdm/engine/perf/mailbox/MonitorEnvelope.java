package cn.betasoft.pdm.engine.perf.mailbox;

import akka.dispatch.Envelope;

/**
 * 监测消息
 */
public class MonitorEnvelope {

	private Integer queueSize;

	private String receiver;

	private Long entryTime;

	//AKKA框架原始Envelope
	private Envelope handle;

	public MonitorEnvelope(Integer queueSize, String receiver, Long entryTime, Envelope handle) {
		this.queueSize = queueSize;
		this.receiver = receiver;
		this.entryTime = entryTime;
		this.handle = handle;
	}

	public Integer getQueueSize() {
		return queueSize;
	}

	public String getReceiver() {
		return receiver;
	}

	public Long getEntryTime() {
		return entryTime;
	}

	public Envelope getHandle() {
		return handle;
	}
}

