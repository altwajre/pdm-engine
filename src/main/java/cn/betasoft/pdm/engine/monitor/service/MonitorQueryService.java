package cn.betasoft.pdm.engine.monitor.service;

import cn.betasoft.pdm.engine.model.monitor.*;

import java.util.List;

public interface MonitorQueryService {

	List<HeapInfo> queryHeap(int offsetMinute);

	List<DispatcherInfo> queryDispatcher(String dispatcherName, int offsetMinute);

	List<CollectStat> queryCollectStat(int offsetMinute);

	List<CollectStat> queryIndicatorHandleStat(int offsetMinute);

	List<MailBoxStat> queryMailBoxStat(int offsetMinute);

}
