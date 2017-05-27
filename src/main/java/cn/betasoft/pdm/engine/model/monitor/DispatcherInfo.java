package cn.betasoft.pdm.engine.model.monitor;

import java.io.Serializable;
import java.util.Date;

/**
 * AKKA线程池信息
 */
public class DispatcherInfo  implements Serializable {

	private Date sampleTime;

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

	public DispatcherInfo() {
	}

	public Date getSampleTime() {
		return sampleTime;
	}

	public void setSampleTime(Date sampleTime) {
		this.sampleTime = sampleTime;
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
}
