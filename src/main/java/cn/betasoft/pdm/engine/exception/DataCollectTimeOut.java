package cn.betasoft.pdm.engine.exception;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 采集数据超时异常
 */
public class DataCollectTimeOut extends Exception {

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final long serialVersionUID = -6969910386241388629L;

	public DataCollectTimeOut(Date scheduleTime, String command) {
		super(sdf.format(scheduleTime) + "-" + command);
	}
}
