package cn.betasoft.pdm.engine.model.monitor;

public enum MonitorType {
	HEAP("heap"), DEADLETTER("deadLetter"), DEFAULTDISPSTCHER("defaultDispatcher"), WORKDISPATCHER(
			"workDispatcher"), FUTUREDISPATCHER("futureDispatcher");

	private final String name;

	MonitorType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static MonitorType getByName(String name) {
		if (name.equals("akka.actor.default-dispatcher")) {
			return DEFAULTDISPSTCHER;
		} else if (name.equals("pdm-work-dispatcher")) {
			return WORKDISPATCHER;
		} else if (name.equals("pdm-future-dispatcher")) {
			return FUTUREDISPATCHER;
		} else {
			return null;
		}
	}
}
