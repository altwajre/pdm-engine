package cn.betasoft.pdm.engine.model.monitor;

import cn.betasoft.pdm.engine.perf.mailbox.MailboxStatistics;

import java.util.Date;

/**
 * 数据采集指标
 */
public class MailBoxStat {

	private Date sampleTime;

	// 进入邮箱数量
	private long entryNumber = 0;

	// 等候总时间
	private long totalTime = 0;

	// 离开邮箱数量
	private long exitNumber = 0;

	// 处理平均时间
	private long avgTime = 0;

	public MailBoxStat() {
	}


	public Date getSampleTime() {
		return sampleTime;
	}

	public void setSampleTime(Date sampleTime) {
		this.sampleTime = sampleTime;
	}

	public long getEntryNumber() {
		return entryNumber;
	}

	public void setEntryNumber(long entryNumber) {
		this.entryNumber = entryNumber;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public long getExitNumber() {
		return exitNumber;
	}

	public void setExitNumber(long exitNumber) {
		this.exitNumber = exitNumber;
	}

	public long getAvgTime() {
		return avgTime;
	}

	public void setAvgTime(long avgTime) {
		this.avgTime = avgTime;
	}

	public MailBoxStat add(MailboxStatistics mailboxStatistics) {

		if(mailboxStatistics == null){
			return this;
		}

		if (mailboxStatistics.getExitTime() == null) {
			this.entryNumber++;
		} else {
			this.exitNumber ++;
			this.totalTime = this.totalTime+(mailboxStatistics.getExitTime()-mailboxStatistics.getEntryTime());
		}
		return this;
	}

	public MailBoxStat computeAvgTime() {
		if(this.exitNumber != 0){
			this.avgTime = this.totalTime / this.exitNumber;
		}
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MailBoxStat that = (MailBoxStat) o;

		return sampleTime != null ? sampleTime.equals(that.sampleTime) : that.sampleTime == null;
	}

	@Override
	public int hashCode() {
		return sampleTime != null ? sampleTime.hashCode() : 0;
	}
}
