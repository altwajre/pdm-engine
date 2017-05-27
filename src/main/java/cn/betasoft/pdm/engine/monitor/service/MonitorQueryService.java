package cn.betasoft.pdm.engine.monitor.service;

import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;

import java.util.List;

public interface MonitorQueryService {

	List<HeapInfo> queryHeap(int offsetMinute);

	List<DispatcherInfo> queryDispatcher(String dispatcherName, int offsetMinute);
}
