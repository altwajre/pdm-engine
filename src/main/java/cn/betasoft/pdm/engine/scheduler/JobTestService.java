package cn.betasoft.pdm.engine.scheduler;

public interface JobTestService {

	void testActorModel();

	boolean addDynamicJob(String name);

	void removeJob(String name);
}
