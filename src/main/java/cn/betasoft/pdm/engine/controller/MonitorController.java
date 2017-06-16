package cn.betasoft.pdm.engine.controller;

import akka.actor.ActorSystem;

import akka.pattern.PatternsCS;
import cn.betasoft.pdm.engine.actor.CollectDataActor;
import cn.betasoft.pdm.engine.actor.IndicatorActor;
import cn.betasoft.pdm.engine.actor.MoActor;
import cn.betasoft.pdm.engine.stats.ShowData;
import cn.betasoft.pdm.engine.stats.TreeNodeType;
import scala.concurrent.Future;
import scala.concurrent.Await;

import akka.pattern.Patterns;

import akka.util.Timeout;
import cn.betasoft.pdm.engine.actor.DeviceActor;
import cn.betasoft.pdm.engine.model.EngineStatusStat;
import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.model.monitor.MailBoxStat;
import cn.betasoft.pdm.engine.monitor.service.MonitorQueryService;
import cn.betasoft.pdm.engine.stats.EngineActorStatus;
import cn.betasoft.pdm.engine.stats.PdmEngineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/monitor")
public class MonitorController {

	@Autowired
	MonitorQueryService monitorQueryService;

	@Autowired
	ActorSystem actorSystem;

	private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

	@RequestMapping(value = "/heap/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<HeapInfo> queryHeapInfo(@PathVariable int offsetMinute) {
		return monitorQueryService.queryHeap(offsetMinute);
	}

	/**
	 * @param name
	 *            "akka.actor.default-dispatcher",
	 *            "pdm-work-dispatcher","pdm-future-dispatcher"
	 * @param offsetMinute
	 * @return
	 */
	@RequestMapping(value = "/dispatcher/{name}/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<DispatcherInfo> queryDispatcherInfo(@PathVariable String name,
			@PathVariable int offsetMinute) {
		return monitorQueryService.queryDispatcher(name, offsetMinute);
	}

	@RequestMapping(value = "/collectStat/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<CollectStat> queryCollectStat(@PathVariable int offsetMinute) {
		return monitorQueryService.queryCollectStat(offsetMinute);
	}

	@RequestMapping(value = "/indicatorHandleStat/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<CollectStat> queryIndicatorHandleStat(@PathVariable int offsetMinute) {
		return monitorQueryService.queryIndicatorHandleStat(offsetMinute);
	}

	@RequestMapping(value = "/mailboxStat/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<MailBoxStat> queryMailBoxStat(@PathVariable int offsetMinute) {
		return monitorQueryService.queryMailBoxStat(offsetMinute);
	}

	@RequestMapping(value = "/engine/status", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody EngineStatusStat queryEngineStatus() {
		EngineStatusStat stat = new EngineStatusStat();
		stat.setDeviceNum(PdmEngineStatus.INSTANCE.getDeviceNum());
		stat.setMoNum(PdmEngineStatus.INSTANCE.getMoNum());
		stat.setIndicatorNum(PdmEngineStatus.INSTANCE.getIndicatorNum());
		stat.setAlarmTaskNum(PdmEngineStatus.INSTANCE.getAlarmTaskNum());
		stat.setRuleTaskNum(PdmEngineStatus.INSTANCE.getRuleTaskNum());
		return stat;
	}

	@RequestMapping(value = "/engine/actorStatus", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody EngineActorStatus queryEngineActorStatus(
			@RequestParam(value = "ip", required = true) String ip) {
		Timeout timeout = new Timeout(Duration.create(10, TimeUnit.SECONDS));
		Future<Object> future = Patterns.ask(actorSystem.actorSelection("/user/supervisor/d-" + ip),
				new DeviceActor.MonitorTree(), timeout);
		try {
			EngineActorStatus actorStatus = (EngineActorStatus) Await.result(future, timeout.duration());

			List<CompletableFuture<Object>> childFutures = new ArrayList<>();
			for (Map.Entry<String, EngineActorStatus> descendantMap : actorStatus.getAllDescendants().entrySet()) {
				if (descendantMap.getValue().getNodeType() == TreeNodeType.MANAGEDOBJECT) {
					CompletableFuture<Object> childFuture = PatternsCS
							.ask(actorSystem.actorSelection(descendantMap.getValue().getActorPath()),
									new MoActor.GetShowData(), timeout)
							.toCompletableFuture();
					childFutures.add(childFuture);
				}else if (descendantMap.getValue().getNodeType() == TreeNodeType.INDICATOR) {
					CompletableFuture<Object> childFuture = PatternsCS
							.ask(actorSystem.actorSelection(descendantMap.getValue().getActorPath()),
									new IndicatorActor.GetShowData(), timeout)
							.toCompletableFuture();
					childFutures.add(childFuture);
				}else if (descendantMap.getValue().getNodeType() == TreeNodeType.COLLECT) {
					CompletableFuture<Object> childFuture = PatternsCS
							.ask(actorSystem.actorSelection(descendantMap.getValue().getActorPath()),
									new CollectDataActor.GetShowData(), timeout)
							.toCompletableFuture();
					childFutures.add(childFuture);
				}
			}
			System.out.println("size---------------"+childFutures.size());
			CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[childFutures.size()])).join();
			for (CompletableFuture<Object> child : childFutures) {
				ShowData showData = (ShowData) child.get();
				if (actorStatus.getAllDescendants().containsKey(showData.getActorPath())) {
					actorStatus.getAllDescendants().get(showData.getActorPath()).setShowData(showData.getValue());
					System.out.println(child.get().toString());
				}
			}

			return actorStatus;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
