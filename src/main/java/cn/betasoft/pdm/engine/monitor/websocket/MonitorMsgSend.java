package cn.betasoft.pdm.engine.monitor.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MonitorMsgSend {

	@Autowired
	private SimpMessagingTemplate messageSender;

	public void sendMessage(String message){
		messageSender.convertAndSend("/topic/monitor",message);
	}
}
