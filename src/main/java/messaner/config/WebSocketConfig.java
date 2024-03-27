package messaner.config;

import messaner.model.Chat;
import messaner.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSocketSecurity
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/main").withSockJS(); //WebSocket 연결 엔드포인트
        registry.setPreserveReceiveOrder(true);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); //MessageMapping 컨트롤러 접두사
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
            .simpDestMatchers("/rooms", "/rooms/*").permitAll()
            .simpSubscribeDestMatchers("/topic/chatting/*").permitAll()
            .simpMessageDestMatchers("/createChannel", "/chatting/*", "/chat").authenticated()
            .simpTypeMatchers(SimpMessageType.UNSUBSCRIBE).authenticated()
            .anyMessage().denyAll();

        return messages.build();
    }
}
