package cn.betasoft.pdm.engine.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 添加一个/socket端点，客户端就可以通过这个端点来进行连接
		registry.addEndpoint("/socket").setAllowedOrigins("*");
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 客户端订阅地址的前缀信息
		registry.enableSimpleBroker("/topic");
		// 客户端给服务端发消息地址的前缀
		registry.setApplicationDestinationPrefixes("/app");
	}

}