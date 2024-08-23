package messaner.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final ChannelInterceptor channelInterceptor;

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(channelInterceptor);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/pub"); //MessageMapping 컨트롤러 접두사
    registry.enableSimpleBroker("/topic", "/queue");
  }


  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*"); //WebSocket 연결 엔드포인트
    registry.addEndpoint("/sockjs").setAllowedOriginPatterns("*").withSockJS();
  }
}
