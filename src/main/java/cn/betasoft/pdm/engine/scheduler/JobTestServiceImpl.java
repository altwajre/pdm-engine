package cn.betasoft.pdm.engine.scheduler;

import java.util.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.actor.Supervisor;
import cn.betasoft.pdm.engine.model.*;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.betasoft.pdm.engine.scheduler.dynamic.DynamicJob;
import cn.betasoft.pdm.engine.scheduler.dynamic.DynamicSchedulerFactory;

@Component
public class JobTestServiceImpl implements JobTestService {

	@Autowired
	JobService jobService;

	@Autowired
	ActorSystem actorSystem;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	DynamicSchedulerFactory dynamicSchedulerFactory;

	private static final Logger logger = LoggerFactory.getLogger(JobTestService.class);

	@Override
	public void testActorModel() {
		Set<Device> devices = new HashSet<>();

		Set<String> uuids = new HashSet<>();

		// 不能包括中文
		String[] indicatorNames = { "PING", "CPU", "MEM", "DISK", "PORT" };

		for (int i = 0; i < 100; i++) {
			Device device = new Device();
			device.setIp("138.174.65." + i);
			device.setName("device-" + i);

			Random moRandom = new Random();
			int moNum = 1 + moRandom.nextInt(4);

			// 一个设备下有多个资源
			for (int j = 0; j < 5; j++) {
				ManagedObject mo = new ManagedObject();
				mo.setMoPath(UUID.randomUUID().toString());
				mo.setName("mo-" + i);
				mo.setType(ManagedObjectType.getTypeByOrdinal(moNum));
				mo.setDevice(device);

				// 一个资源下有多个采集指标
				Random indicatorRandom = new Random();
				int indicatorNum = indicatorRandom.nextInt(4);
				for (int k = 0; k < indicatorNum; k++) {
					Indicator indicator = new Indicator();
					indicator.setName(indicatorNames[k]);
					indicator.setParameters(UUID.randomUUID().toString());

					indicator.setMo(mo);

					mo.getIndicators().add(indicator);

					// 一个指标下有多个任务
					Random taskRandom = new Random();
					int taskNum = 2 + taskRandom.nextInt(10);
					for (int m = 0; m < taskNum; m++) {
						SingleIndicatorTask task = new SingleIndicatorTask();
						if (m % 2 == 0) {
							task.setType(TaskType.ALARM);
						} else {
							task.setType(TaskType.RULE);
						}

						String uuid = UUID.randomUUID().toString();
						while (uuids.contains(uuid)) {
							uuid = UUID.randomUUID().toString();
						}
						uuids.add(uuid);
						task.setKey(uuid);

						if (m == 1) {
							task.setTopLevel(true);
						} else {
							task.setTopLevel(false);
						}

						int[] days = { CronWeekdays.SATURDAY.getWeekday(), CronWeekdays.SUNDAY.getWeekday() };
						String holidayCronExrepsstion = HolidayCronBuilder.INSTANCE.buildWeeklyCron(days);
						//task.getHolidayCronExrpessions().add(holidayCronExrepsstion);

						String todayExrepsstion = "0 0-4 17 * * ?";
						//task.getHolidayCronExrpessions().add(todayExrepsstion);

						Random cronRandom = new Random();
						int cronNum = cronRandom.nextInt(7);
						CronValue cronValue = CronValue.getValueByOrdinal(cronNum);
						task.setCronExpression(cronValue.getCronExpression());

						task.setName(cronValue.toString());

						Random taskIndicatorNumRandom = new Random();
						int taskIndicatorNum = 1 + taskIndicatorNumRandom.nextInt(4);
						task.setIndicatorNum(taskIndicatorNum);

						indicator.getSingleIndicatorTasks().add(task);
						task.setIndicator(indicator);

						System.out.println("task is: " + task.getKey() + ",indicator is:" + indicator.getName()
								+ ",mo is:" + mo.getMoPath() + ",device is:" + device.getIp());
					}
				}

				device.getMos().add(mo);
			}
			devices.add(device);
		}

		List<Device> deviceList = new ArrayList<>(devices);
		// 第一个设备监听cpu和内存
		Device firstDevice = deviceList.get(0);
		MultiIndicatorTask multiTask01 = new MultiIndicatorTask();
		multiTask01.setName(UUID.randomUUID().toString());
		multiTask01.setDevice(firstDevice);
		firstDevice.getMos().forEach(mo -> {
			mo.getIndicators().forEach(indicator -> {
				if (indicator.getName().equals("CPU") || indicator.getName().equals("MEM")) {
					multiTask01.getIndicators().add(indicator);
				}
			});
		});
		firstDevice.getMultiIndicatorTasks().add(multiTask01);

		actorSystem.actorSelection("/user/supervisor/").tell(new Supervisor.DeviceInfo(devices), ActorRef.noSender());

		testAddPdmJobByDevice(devices);
	}

	private void testAddPdmJobByDevice(Set<Device> devices) {

		devices.stream().forEach(device -> {
			// 组名对应moPath
			// 任务名对应采集指标名称
			device.getMos().forEach(mo -> {
				mo.getIndicators().forEach(indicator -> {
					String groupName = mo.getMoPath();
					String jobName = indicator.getName();
					PdmJob pdmJob = new PdmJob(jobName, groupName, JobRunner.class);

					String collectActorPath = "/user/supervisor/d-" + device.getIp() + "/mo-" + mo.getMoPath()
							+ "/indi-" + indicator.getName() + "/collect";
					pdmJob.addJobData("collectActorPath", collectActorPath);

					indicator.getSingleIndicatorTasks().forEach(task -> {
						pdmJob.addConTrigger(task.getName(), task.getCronExpression());
					});

					try {
						jobService.registerJob(pdmJob);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				});
			});

		});
	}

	/*
	 * 添加一个动态的JOB
	 */
	public boolean addDynamicJob(String name) {
		DynamicJob dynamicJob = createDynamicJob(name);
		dynamicJob.addJobData("uuuid", UUID.randomUUID().toString());// transfer
																		// parameter

		try {
			dynamicSchedulerFactory.registerJob(dynamicJob);
		} catch (SchedulerException e) {
			throw new IllegalStateException(e);
		}

		return true;
	}

	/*
	 * 删除一个JOB
	 */
	public void removeJob(String name) {
		final DynamicJob dynamicJob = createDynamicJob(name);
		try {
			final boolean result = dynamicSchedulerFactory.removeJob(dynamicJob);
			logger.info("Remove DynamicJob [{}] result: {}", dynamicJob, result);
		} catch (SchedulerException e) {
			throw new IllegalStateException(e);
		}
	}

	// 创建 一个动态的JOB, 测试用
	private DynamicJob createDynamicJob(String name) {
		return new DynamicJob(name)
				// 动态定时任务的 cron, 每10秒执行一次
				.cronExpression("0/10 * * * * ?").target(TargetDynamicJob.class);
	}
}