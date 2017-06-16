package cn.betasoft.pdm.engine.monitor.service;

import akka.actor.ActorSystem;
import cn.betasoft.pdm.engine.model.monitor.*;
import cn.betasoft.pdm.engine.monitor.query.CollectStatQuery;
import cn.betasoft.pdm.engine.monitor.query.DispatcherInfoQuery;
import cn.betasoft.pdm.engine.monitor.query.HeapInfoQuery;
import cn.betasoft.pdm.engine.monitor.query.MailBoxStatQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class MonitorQueryServiceImpl implements MonitorQueryService {

	@Autowired private Properties kafkaConsumerProperties;

	@Autowired private ActorSystem actorSystem;

	@Override public List<HeapInfo> queryHeap(int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		HeapInfoQuery heapQuery = new HeapInfoQuery("heapQuery", "heap", offsetTime, kafkaConsumerProperties);
		return heapQuery.query();
	}

	@Override public List<DispatcherInfo> queryDispatcher(String dispatcherName, int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		DispatcherInfoQuery dispatcherInfoQuery = new DispatcherInfoQuery(dispatcherName + "Query", dispatcherName,
				offsetTime, kafkaConsumerProperties);
		return dispatcherInfoQuery.query();
	}

	@Override public List<CollectStat> queryCollectStat(int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		CollectStatQuery collectStatQuery = new CollectStatQuery("collectStatQuery", "collectStat", offsetTime,
				kafkaConsumerProperties);
		return collectStatQuery.query();
	}

	@Override public List<CollectStat> queryIndicatorHandleStat(int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		CollectStatQuery collectStatQuery = new CollectStatQuery("indicatorHandleStatQuery", "indicatorHandleStat", offsetTime,
				kafkaConsumerProperties);
		return collectStatQuery.query();
	}

	@Override public List<MailBoxStat> queryMailBoxStat(int offsetMinute) {
		long offsetTime = Instant.now().minus(offsetMinute, MINUTES).toEpochMilli();
		MailBoxStatQuery statQuery = new MailBoxStatQuery("mailboxStatQuery", "mailboxStat", offsetTime,
				kafkaConsumerProperties);
		return statQuery.query();
	}

}
