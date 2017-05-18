package cn.betasoft.pdm.engine.scheduler;

import org.quartz.SchedulerException;

public interface JobService {

	boolean registerJob(PdmJob job) throws SchedulerException;

	boolean existedJob(PdmJob job) throws SchedulerException;

	boolean removeJob(PdmJob job) throws SchedulerException;
}
