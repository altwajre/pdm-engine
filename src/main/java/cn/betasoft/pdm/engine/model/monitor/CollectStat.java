package cn.betasoft.pdm.engine.model.monitor;

import cn.betasoft.pdm.engine.perf.actor.ActorStatistics;
import cn.betasoft.pdm.engine.perf.actor.ActorStatisticsType;

import java.util.Date;

/**
 * 数据采集指标
 */
public class CollectStat {

    private Date sampleTime;

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

    public Date getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(Date sampleTime) {
        this.sampleTime = sampleTime;
    }

    public long getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(long totalNumber) {
        this.totalNumber = totalNumber;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getTimeoutNumber() {
        return timeoutNumber;
    }

    public void setTimeoutNumber(long timeoutNumber) {
        this.timeoutNumber = timeoutNumber;
    }

    public long getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(long avgTime) {
        this.avgTime = avgTime;
    }

    public CollectStat add(ActorStatistics actorStat) {

        if (actorStat == null) {
            return this;
        }
        if (actorStat.getType() == ActorStatisticsType.TIMEOUT) {
            this.timeoutNumber++;
        } else if (actorStat.getType() == ActorStatisticsType.NORMAL) {
            this.totalNumber++;
            this.totalTime = this.totalTime + actorStat.getTotalTimeMillis();
        }
        return this;
    }

    public CollectStat computeAvgTime() {
        if ((this.totalNumber + this.timeoutNumber) != 0) {
            this.avgTime = this.totalTime / (this.totalNumber + this.timeoutNumber);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectStat that = (CollectStat) o;

        return sampleTime != null ? sampleTime.equals(that.sampleTime) : that.sampleTime == null;
    }

    @Override
    public int hashCode() {
        return sampleTime != null ? sampleTime.hashCode() : 0;
    }
}
