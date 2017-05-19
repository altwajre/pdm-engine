package cn.betasoft.pdm.engine.perf.mailbox;

public class MailboxStatistics {

	private Integer queueSize;

	//信息接收者
	private String receiver;

	private String sender;

	//信息抵达MailBox时间
	private Long entryTime;

	//信息离开MailBox时间
	private Long exitTime;

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

	public String getReceiver() {
		return receiver;
	}

	public String getSender() {
		return sender;
	}

	public Long getEntryTime() {
		return entryTime;
	}

	public Long getExitTime() {
		return exitTime;
	}

}

