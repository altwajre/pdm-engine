package cn.betasoft.pdm.engine.perf.mailbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public class MailboxStatistics {

	private Integer queueSize;

	//信息接收者
	private String receiver;

	private String sender;

	//信息抵达MailBox时间
	private Long entryTime;

	//信息离开MailBox时间
	private Long exitTime;

	public MailboxStatistics() {
	}

	public MailboxStatistics(Integer queueSize, String receiver, String sender, Long entryTime, Long exitTime) {
		this.queueSize = queueSize;
		this.receiver = receiver;
		this.sender = sender;
		this.entryTime = entryTime;
		this.exitTime = exitTime;
	}

	public Integer getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(Integer queueSize) {
		this.queueSize = queueSize;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Long getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(Long entryTime) {
		this.entryTime = entryTime;
	}

	public Long getExitTime() {
		return exitTime;
	}

	public void setExitTime(Long exitTime) {
		this.exitTime = exitTime;
	}
}

