package cn.betasoft.pdm.engine;

import cn.betasoft.pdm.engine.scheduler.CronWeekdays;
import cn.betasoft.pdm.engine.scheduler.HolidayCronBuilder;
import com.cronutils.model.field.expression.Weekdays;
import org.junit.Test;

public class CronBuilderTest {

	@Test
	public void testDayOfWeek(){
		int[] days = { CronWeekdays.SATURDAY.getWeekday(), CronWeekdays.SUNDAY.getWeekday()};
		String cronExrepsstion = HolidayCronBuilder.INSTANCE.buildHourCron();
		System.out.println(cronExrepsstion);
	}
}
