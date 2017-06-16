package cn.betasoft.pdm.engine.perf.actor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public class ActorStatistics {

	private String receiver;

	private String sender;

	private String methodName;

	private long entryTime;

	private long totalTimeMillis;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
	private ActorStatisticsType type;

	public ActorStatistics(){

	}

	public ActorStatistics(String receiver, String sender, String methodName, long entryTime, long totalTimeMillis,
			ActorStatisticsType type) {
		this.receiver = receiver;
		this.sender = sender;
		this.methodName = methodName;
		this.entryTime = entryTime;
		this.totalTimeMillis = totalTimeMillis;
		this.type = type;
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

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public long getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(long entryTime) {
		this.entryTime = entryTime;
	}

	public long getTotalTimeMillis() {
		return totalTimeMillis;
	}

	public void setTotalTimeMillis(long totalTimeMillis) {
		this.totalTimeMillis = totalTimeMillis;
	}

	public ActorStatisticsType getType() {
		return type;
	}

	public void setType(ActorStatisticsType type) {
		this.type = type;
	}
}
