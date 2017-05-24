package cn.betasoft.pdm.engine.controller;

import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.monitor.HeapMonitorActor;
import cn.betasoft.pdm.engine.monitor.service.MonitorQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("monitor")
public class MonitorController {

	@Autowired
	MonitorQueryService monitorQueryService;

	private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

	@RequestMapping(value = "/heap/query/{offsetMinute}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<HeapInfo> query(@PathVariable int offsetMinute) {
		return monitorQueryService.queryHeap(offsetMinute);
	}
}
