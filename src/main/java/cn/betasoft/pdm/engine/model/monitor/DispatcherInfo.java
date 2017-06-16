package cn.betasoft.pdm.engine.model.monitor;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AKKA线程池信息
 */
public class DispatcherInfo  implements Serializable {

	private Date sampleTime;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
	private DispatcherType type;

	//池的并行的级别
	private long parallelism;

	//当前执行任务的线程的数量
	private long activeThreadCount;

	//已经提交给线程池并已经开始执行的任务数
	private long queuedTaskCount;

	//内部线程池的worker线程的数量
	private long poolSize;

	//没有被任何同步机制阻塞的正在工作的线程
	private long runningThreadCount;

	//已经提交给线程池但还没有开始执行的任务数
	private long queuedSubmissionCount;

	//核心线程数
	private long corePoolSize;

	//最大线程池大小
	private long maximumPoolSize;

	public DispatcherInfo() {
	}

	public Date getSampleTime() {
		return sampleTime;
	}

	public void setSampleTime(Date sampleTime) {
		this.sampleTime = sampleTime;
	}

	public DispatcherType getType() {
		return type;
	}

	public void setType(DispatcherType type) {
		this.type = type;
	}

	public long getParallelism() {
		return parallelism;
	}

	public void setParallelism(long parallelism) {
		this.parallelism = parallelism;
	}

	public long getActiveThreadCount() {
		return activeThreadCount;
	}

	public void setActiveThreadCount(long activeThreadCount) {
		this.activeThreadCount = activeThreadCount;
	}

	public long getQueuedTaskCount() {
		return queuedTaskCount;
	}

	public void setQueuedTaskCount(long queuedTaskCount) {
		this.queuedTaskCount = queuedTaskCount;
	}

	public long getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(long poolSize) {
		this.poolSize = poolSize;
	}

	public long getRunningThreadCount() {
		return runningThreadCount;
	}

	public void setRunningThreadCount(long runningThreadCount) {
		this.runningThreadCount = runningThreadCount;
	}

	public long getQueuedSubmissionCount() {
		return queuedSubmissionCount;
	}

	public void setQueuedSubmissionCount(long queuedSubmissionCount) {
		this.queuedSubmissionCount = queuedSubmissionCount;
	}

	public long getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(long corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public long getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(long maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}
}
