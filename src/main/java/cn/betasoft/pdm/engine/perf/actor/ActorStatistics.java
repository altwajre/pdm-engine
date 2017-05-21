package cn.betasoft.pdm.engine.perf.actor;

public class ActorStatistics {

	private String receiver;

	private String sender;

	private long entryTime;

	private long totalTimeMillis;

	public ActorStatistics(String receiver, String sender, long entryTime, long totalTimeMillis) {
		this.receiver = receiver;
		this.sender = sender;
		this.entryTime = entryTime;
		this.totalTimeMillis = totalTimeMillis;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getSender() {
		return sender;
	}

	public long getEntryTime() {
		return entryTime;
	}

	public long getTotalTimeMillis() {
		return totalTimeMillis;
	}
}
