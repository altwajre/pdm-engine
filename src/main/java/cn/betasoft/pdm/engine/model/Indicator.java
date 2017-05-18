package cn.betasoft.pdm.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个采集参数下的一个具体监控对象 例如CPU采集参数下的CPU负载 ---端口采集参数下的端口1流量统计
 */
public class Indicator implements Serializable {

	private String name;

	private String parameters;

	private ManagedObject mo;

	private List<SingleIndicatorTask> singleIndicatorTasks = new ArrayList<>();

	public Indicator() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public ManagedObject getMo() {
		return mo;
	}

	public void setMo(ManagedObject mo) {
		this.mo = mo;
	}

	public List<SingleIndicatorTask> getSingleIndicatorTasks() {
		return singleIndicatorTasks;
	}

	public void setSingleIndicatorTasks(List<SingleIndicatorTask> singleIndicatorTasks) {
		this.singleIndicatorTasks = singleIndicatorTasks;
	}

	@Override public String toString() {
		return "Indicator{" + "name='" + name + '\'' + ", parameters='" + parameters + '\'' + '}';
	}
}
