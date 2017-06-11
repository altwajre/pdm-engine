package cn.betasoft.pdm.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 多指标监控任务，比例同时监控cpu,内存
 */
public class MultiIndicatorTask {

	private String name;

	private Device device;

	//最高级别任务，数据的采集使用独立线程池处理
	private Boolean topLevel;

	private TaskType type;

	private List<Indicator> indicators = new ArrayList<>();

	public MultiIndicatorTask(){

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	public Boolean getTopLevel() {
		return topLevel;
	}

	public void setTopLevel(Boolean topLevel) {
		this.topLevel = topLevel;
	}

	public List<Indicator> getIndicators() {
		return indicators;
	}

	public void setIndicators(List<Indicator> indicators) {
		this.indicators = indicators;
	}
}
