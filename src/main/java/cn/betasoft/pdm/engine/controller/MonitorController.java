package cn.betasoft.pdm.engine.controller;

import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.model.monitor.MailBoxStat;
import cn.betasoft.pdm.engine.monitor.HeapMonitorActor;
import cn.betasoft.pdm.engine.monitor.service.MonitorQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/monitor")
public class MonitorController {

	@Autowired
	MonitorQueryService monitorQueryService;

	private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

	@RequestMapping(value = "/heap/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<HeapInfo> queryHeapInfo(@PathVariable int offsetMinute) {
		return monitorQueryService.queryHeap(offsetMinute);
	}

	/**
	 *
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

	@RequestMapping(value = "/mailboxStat/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<MailBoxStat> queryMailBoxStat(@PathVariable int offsetMinute) {
		return monitorQueryService.queryMailBoxStat(offsetMinute);
	}
}
