package cn.betasoft.pdm.engine.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Await;
import scala.concurrent.Promise;

import akka.pattern.Patterns;

import akka.util.Timeout;
import cn.betasoft.pdm.engine.actor.DeviceActor;
import cn.betasoft.pdm.engine.model.EngineStatusStat;
import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.model.monitor.MailBoxStat;
import cn.betasoft.pdm.engine.monitor.HeapMonitorActor;
import cn.betasoft.pdm.engine.monitor.service.MonitorQueryService;
import cn.betasoft.pdm.engine.stats.EngineActorStatus;
import cn.betasoft.pdm.engine.stats.EngineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.List;
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
    public
    @ResponseBody
    List<HeapInfo> queryHeapInfo(@PathVariable int offsetMinute) {
        return monitorQueryService.queryHeap(offsetMinute);
    }

    /**
     * @param name         "akka.actor.default-dispatcher",
     *                     "pdm-work-dispatcher","pdm-future-dispatcher"
     * @param offsetMinute
     * @return
     */
    @RequestMapping(value = "/dispatcher/{name}/query/{offsetMinute}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    List<DispatcherInfo> queryDispatcherInfo(@PathVariable String name,
                                             @PathVariable int offsetMinute) {
        return monitorQueryService.queryDispatcher(name, offsetMinute);
    }

    @RequestMapping(value = "/collectStat/query/{offsetMinute}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    List<CollectStat> queryCollectStat(@PathVariable int offsetMinute) {
        return monitorQueryService.queryCollectStat(offsetMinute);
    }

    @RequestMapping(value = "/mailboxStat/query/{offsetMinute}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    List<MailBoxStat> queryMailBoxStat(@PathVariable int offsetMinute) {
        return monitorQueryService.queryMailBoxStat(offsetMinute);
    }

    @RequestMapping(value = "/engine/status", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    EngineStatusStat queryEngineStatus() {
        EngineStatusStat stat = new EngineStatusStat();
        stat.setDeviceNum(EngineStatus.INSTANCE.getDeviceNum());
        stat.setMoNum(EngineStatus.INSTANCE.getMoNum());
        stat.setIndicatorNum(EngineStatus.INSTANCE.getIndicatorNum());
        stat.setAlarmTaskNum(EngineStatus.INSTANCE.getAlarmTaskNum());
        stat.setRuleTaskNum(EngineStatus.INSTANCE.getRuleTaskNum());
        return stat;
    }

    @RequestMapping(value = "/engine/actorStatus", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public
    @ResponseBody
    EngineActorStatus queryEngineActorStatus(@RequestParam(value = "ip", required = true) String ip) {
        Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        Future<Object> future =
                Patterns.ask(actorSystem.actorSelection("/user/supervisor/d-" + ip), new DeviceActor.Status(), timeout);
        try {
            EngineActorStatus actorStatus = (EngineActorStatus) Await.result(future, timeout.duration());
            return actorStatus;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
