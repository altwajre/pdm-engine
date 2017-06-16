package cn.betasoft.pdm.engine.stats;


public class ShowData {

	private String actorPath;

	private String value;

	public ShowData(String actorPath, String value) {
		this.actorPath = actorPath;
		this.value = value;
	}

	public String getActorPath() {
		return actorPath;
	}

	public String getValue() {
		return value;
	}
}
