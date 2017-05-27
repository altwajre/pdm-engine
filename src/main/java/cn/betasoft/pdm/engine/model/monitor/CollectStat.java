package cn.betasoft.pdm.engine.model.monitor;

import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import cn.betasoft.pdm.engine.perf.actor.ActorStatisticsType;

/**
 * 数据采集指标
 */
public class CollectStat {

	// 正常处理次数
	private long totalNumber = 0;

	// 处理总时间
	private long totalTime = 0;

	// 处理超时次数
	private long timeoutNumber = 0;

	// 处理平均时间
	private long avgTime = 0;

	public CollectStat() {
	}

	public CollectStat add(ActorStatistics actorStat) {

		if (actorStat.getType() == ActorStatisticsType.TIMEOUT) {
			this.timeoutNumber++;
		} else if (actorStat.getType() == ActorStatisticsType.NORMAL) {
			this.totalNumber++;
		}

		this.totalTime = this.totalTime + actorStat.getTotalTimeMillis();
		return this;
	}

	public CollectStat computeAvgTime() {
		this.avgTime = this.totalTime / (this.totalNumber + this.timeoutNumber);
		return this;
	}
}
