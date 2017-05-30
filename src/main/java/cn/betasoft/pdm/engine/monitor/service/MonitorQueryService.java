package cn.betasoft.pdm.engine.monitor.service;

import cn.betasoft.pdm.engine.model.monitor.CollectStat;
import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.model.monitor.MailBoxStat;

import java.util.List;

public interface MonitorQueryService {

	List<HeapInfo> queryHeap(int offsetMinute);

	List<DispatcherInfo> queryDispatcher(String dispatcherName, int offsetMinute);

	List<CollectStat> queryCollectStat(int offsetMinute);

	List<MailBoxStat> queryMailBoxStat(int offsetMinute);

}
