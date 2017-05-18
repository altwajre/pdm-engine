package cn.betasoft.pdm.engine.model;

/**
 * Created by Administrator on 2017/5/11.
 */
public enum ManagedObjectType {

	NETWORK("网络设备"), ORACLE("oracle数据库"), WINDOWS("windows"), LIUNX("liunx"), AIX("aix"), JBOSS("jboss");

	private final String cronExpression;

	ManagedObjectType(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public static ManagedObjectType getTypeByOrdinal(int ordinal) {
		if (ordinal == 0) {
			return ManagedObjectType.NETWORK;
		} else if (ordinal == 1) {
			return ManagedObjectType.ORACLE;
		} else if (ordinal == 2) {
			return ManagedObjectType.WINDOWS;
		} else if (ordinal == 3) {
			return ManagedObjectType.LIUNX;
		} else if (ordinal == 4) {
			return ManagedObjectType.AIX;
		} else if (ordinal == 5) {
			return ManagedObjectType.JBOSS;
		} else {
			return null;
		}
	}
}
