package cn.betasoft.pdm.engine.perf.actor;

public class ActorStatistics {

	private String receiver;

	private String sender;

	private Long entryTime;

	private Long exitTime;

	public ActorStatistics(String receiver, String sender, Long entryTime, Long exitTime) {
		this.receiver = receiver;
		this.sender = sender;
		this.entryTime = entryTime;
		this.exitTime = exitTime;
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
