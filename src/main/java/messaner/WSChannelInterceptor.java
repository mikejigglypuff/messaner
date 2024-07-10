package messaner;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.UserDTO;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WSChannelInterceptor implements ChannelInterceptor {
    private final RepositoryService repositoryService;
    private final JwtProvider jwtProvider;

    private final Map<String, String> sessions;

    @Autowired
    public WSChannelInterceptor(RepositoryService repositoryService, JwtProvider jwtProvider) {
        this.repositoryService = repositoryService;
        this.jwtProvider = jwtProvider;
        this.sessions = new HashMap<>();
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if(StompCommand.SUBSCRIBE.equals(command)) {
            String token = accessor.getFirstNativeHeader("Authorization");
            log.info("Message token: " + token);
            if(token != null) {
                String dest = accessor.getDestination();

                assert dest != null;
                String[] uri = dest.split("/");
                log.info("Message destination: " + uri[uri.length - 1]);

                String user = jwtProvider.getUserId(token.substring(7));

                log.info("user: " + user);
                repositoryService.addSubscription(new UserDTO(uri[uri.length - 1], user));
                sessions.put(accessor.getSessionId(), user);
                log.info(accessor.getSessionId());
            }
        }
        return message;
    }

    public String getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}
