package cn.betasoft.pdm.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个被管理的资源,可以是一个服务器，一个服务器上的数据库，中间件
 */
public class ManagedObject implements Serializable {

	private String name;

	private String moPath;

	private String description;

	private ManagedObjectType type;

	private Device device;

	private List<Indicator> indicators = new ArrayList<>();

	public ManagedObject() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMoPath() {
		return moPath;
	}

	public void setMoPath(String moPath) {
		this.moPath = moPath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ManagedObjectType getType() {
		return type;
	}

	public void setType(ManagedObjectType type) {
		this.type = type;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public List<Indicator> getIndicators() {
		return indicators;
	}

	public void setIndicators(List<Indicator> indicators) {
		this.indicators = indicators;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ManagedObject that = (ManagedObject) o;

		return moPath != null ? moPath.equals(that.moPath) : that.moPath == null;
	}

	@Override
	public int hashCode() {
		return moPath != null ? moPath.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "ManagedObject{" + "name='" + name + '\'' + ", moPath='" + moPath + '\'' + ", description='"
				+ description + '\'' + '}';
	}
}
