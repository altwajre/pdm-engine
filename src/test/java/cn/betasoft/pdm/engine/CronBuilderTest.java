package cn.betasoft.pdm.engine;

import cn.betasoft.pdm.engine.monitor.stream.TickerWindow;
import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import cn.betasoft.pdm.engine.scheduler.CronWeekdays;
import cn.betasoft.pdm.engine.scheduler.HolidayCronBuilder;
import com.cronutils.model.field.expression.Weekdays;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class CronBuilderTest {

	@Test
	public void testDayOfWeek() throws Exception{
		int[] days = { CronWeekdays.SATURDAY.getWeekday(), CronWeekdays.SUNDAY.getWeekday()};
		String cronExrepsstion = HolidayCronBuilder.INSTANCE.buildHourCron();
		System.out.println(cronExrepsstion);

		String json = "{\"receiver\":\"Actor[akka://pdmActorSystem/user/supervisor/d-138.174.65.4/mo-0066b5b4-8a07-40e1-9b8e-69032628fcdb/indi-MEM/collect/httpGetData#995538147]\",\"sender\":\"Actor[akka://pdmActorSystem/deadLetters]\",\"methodName\":\"httpGetHandler\",\"entryTime\":1496065170139,\"totalTimeMillis\":112,\"type\":0}";
		ObjectMapper objectMapper = new ObjectMapper();

		ActorStatistics actorStatistics = objectMapper.readValue(json,ActorStatistics.class);
		System.out.println(actorStatistics.getReceiver());
	}
}
