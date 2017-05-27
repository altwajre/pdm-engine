package cn.betasoft.pdm.engine.model.monitor;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MonitorMessage {

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
	private MonitorType type;

	private String message;

	public MonitorMessage(MonitorType type, String message) {
		this.type = type;
		this.message = message;
	}

	public MonitorType getType() {
		return type;
	}

	public void setType(MonitorType type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
