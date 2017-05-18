package cn.betasoft.pdm.engine.scheduler;

/**
 * Created by Administrator on 2017/5/15.
 */
public enum CronWeekdays {

	SUNDAY(1),
	MONDAY(2),
	TUESDAY(3),
	WEDNESDAY(4),
	THURSDAY(5),
	FRIDAY(6),
	SATURDAY(7);

	private int weekday;

	private CronWeekdays(int weekday) {
		this.weekday = weekday;
	}

	public int getWeekday() {
		return this.weekday;
	}
}
