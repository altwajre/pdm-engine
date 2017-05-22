package cn.betasoft.pdm.engine.scheduler;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.expression.FieldExpression;

import java.util.ArrayList;
import java.util.List;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;
import static com.cronutils.model.field.expression.FieldExpressionFactory.questionMark;

/**
 * 用于非工作日时间设置
 * https://www.freeformatter.com/cron-expression-generator-quartz.html#crongenerator
 */
public enum HolidayCronBuilder {

	INSTANCE;

	public String buildWeeklyCron(int[] dayOfWeeks) {
		//（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT
		CronBuilder builder = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		if(dayOfWeeks.length == 1){
			builder.withDoW(on(dayOfWeeks[0]));
		}else {
			List<FieldExpression> days = new ArrayList<>();
			for(int i=0;i<dayOfWeeks.length;i++){
				days.add(on(dayOfWeeks[i]));
			}
			builder.withDoW(and(days));
		}

		builder.withMonth(always());
		builder.withDoM(questionMark());
		builder.withHour(always());
		builder.withMinute(always());
		builder.withSecond(always());
		Cron cron = builder.instance();
		String cronAsString = cron.asString();
		return cronAsString;
	}

	public String buildHourCron() {
		//（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT
		CronBuilder builder = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		builder.withYear(always());
        builder.withDoW(on(2).and(on(3)).and(on(4)).and(on(5)).and(on(6)));
		builder.withMonth(always());
		builder.withDoM(questionMark());
		builder.withHour(between(8,17));
		builder.withMinute(on(0));
		builder.withSecond(on(0));
		Cron cron = builder.instance();
		String cronAsString = cron.asString();
		return cronAsString;
	}
}
