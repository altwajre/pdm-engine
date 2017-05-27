package cn.betasoft.pdm.engine.monitor.service;

import cn.betasoft.pdm.engine.model.monitor.DispatcherInfo;
import cn.betasoft.pdm.engine.model.monitor.HeapInfo;
import cn.betasoft.pdm.engine.monitor.query.DispatcherInfoQuery;
import cn.betasoft.pdm.engine.monitor.query.HeapInfoQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class MonitorQueryServiceImpl implements MonitorQueryService{

	@Autowired
	private Properties kafkaConsumerProperties;

	@Override public List<HeapInfo> queryHeap(int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		HeapInfoQuery heapQuery = new HeapInfoQuery("heapQuery","heap",offsetTime,kafkaConsumerProperties);
		return heapQuery.query();
	}

	@Override public List<DispatcherInfo> queryDispatcher(String dispatcherName, int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		DispatcherInfoQuery dispatcherInfoQuery = new DispatcherInfoQuery(dispatcherName+"Query",dispatcherName,offsetTime,kafkaConsumerProperties);
		return dispatcherInfoQuery.query();
	}
}
