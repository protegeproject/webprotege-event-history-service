package edu.stanford.protege.webprotegeeventshistory.config;

import edu.stanford.protege.webprotegeeventshistory.AccessManager;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements  WebSocketMessageBrokerConfigurer {
    private static final int MAX_TEXT_MESSAGE_BUFFER_SIZE = 1024 * 1024;


    @Autowired
    private AccessManager accessManager;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wsapps").setAllowedOrigins("http://webprotege-local.edu");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ProjectEventsInterceptor(accessManager));
    }

    @Bean
    public WebSocketStompClient stompClient() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_BUFFER_SIZE);
        return new WebSocketStompClient(new StandardWebSocketClient(container));
    }
}
