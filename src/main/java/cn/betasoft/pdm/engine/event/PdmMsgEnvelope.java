package cn.betasoft.pdm.engine.event;

import cn.betasoft.pdm.engine.actor.SingleIndicatorTaskActor;

public class PdmMsgEnvelope {

	// 设备IP-资源moPath-采集指标名称
	public final String topic;

	// 传递的数据
	public final SingleIndicatorTaskActor.Result payload;

	public PdmMsgEnvelope(String topic, SingleIndicatorTaskActor.Result payload) {
		this.topic = topic;
		this.payload = payload;
	}
}
