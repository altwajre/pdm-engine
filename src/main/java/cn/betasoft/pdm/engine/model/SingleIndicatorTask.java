package cn.betasoft.pdm.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个采集指标下的处理规则：告警、智维
 */
public class SingleIndicatorTask implements Serializable {

	private String name;

	private String key;

	//最高级别任务，数据的采集使用独立线程池处理
	private Boolean topLevel;

	//采集时间
	private String cronExpression;

	//需要判断状态的指标数量
	private int indicatorNum;

	//非工作日集合
	private List<String> holidayCronExrpessions = new ArrayList<>();

	private TaskType type;

	public SingleIndicatorTask() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Boolean getTopLevel() {
		return topLevel;
	}

	public void setTopLevel(Boolean topLevel) {
		this.topLevel = topLevel;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public int getIndicatorNum() {
		return indicatorNum;
	}

	public void setIndicatorNum(int indicatorNum) {
		this.indicatorNum = indicatorNum;
	}

	public List<String> getHolidayCronExrpessions() {
		return holidayCronExrpessions;
	}

	public void setHolidayCronExrpessions(List<String> holidayCronExrpessions) {
		this.holidayCronExrpessions = holidayCronExrpessions;
	}

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SingleIndicatorTask task = (SingleIndicatorTask) o;

		return key != null ? key.equals(task.key) : task.key == null;
	}

	@Override public int hashCode() {

		return key != null ? key.hashCode() : 0;
	}

	@Override public String toString() {
		return "Task{" + "name='" + name + '\'' + ", key='" + key + '\'' + ", type=" + type + '}';
	}
}
