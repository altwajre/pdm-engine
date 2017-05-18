package cn.betasoft.pdm.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Device implements Serializable {

	private String name;

	private String ip;

	private List<ManagedObject> mos = new ArrayList<>();

	private List<MultiIndicatorTask> multiIndicatorTasks = new ArrayList<>();

	public Device() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<ManagedObject> getMos() {
		return mos;
	}

	public void setMos(List<ManagedObject> mos) {
		this.mos = mos;
	}

	public List<MultiIndicatorTask> getMultiIndicatorTasks() {
		return multiIndicatorTasks;
	}

	public void setMultiIndicatorTasks(List<MultiIndicatorTask> multiIndicatorTasks) {
		this.multiIndicatorTasks = multiIndicatorTasks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Device device = (Device) o;

		return ip != null ? ip.equals(device.ip) : device.ip == null;
	}

	@Override
	public int hashCode() {
		return ip != null ? ip.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "Device{" + "name='" + name + '\'' + ", ip='" + ip + '\'' + '}';
	}
}
