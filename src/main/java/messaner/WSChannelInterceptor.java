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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WSChannelInterceptor implements ChannelInterceptor {
    private final RepositoryService repositoryService;
    private final JwtProvider jwtProvider;
    private final Map<String, String> sessions; //key: userId, val: sessionId

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

        try {
            String token = accessor.getFirstNativeHeader("Authorization");
            log.info("Message token: " + token);
            if (token != null) {
                String dest = accessor.getDestination();
                log.info("Command: " + command + ", dest:" + dest);

                assert dest != null;
                String[] uri = dest.split("/");

                String user = jwtProvider.getUserId(token);
                log.info("user: " + user);

                if (StompCommand.SUBSCRIBE.equals(command)) {
                    repositoryService.addSubscription(new UserDTO(uri[uri.length - 1], user));
                    String sessionId = jwtProvider.getSessionId(token);
                    sessions.put(user, sessionId);

                } else if(StompCommand.UNSUBSCRIBE.equals(command)) {
                    repositoryService.removeSubscription(new UserDTO(uri[uri.length - 1], user));
                    sessions.remove(user);
                }
            } else {
                return MessageBuilder.fromMessage(message)
                        .setHeader("Authorization", jwtProvider.createToken())
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return MessageBuilder.withPayload("Error processing message: " + e.getMessage())
                    .copyHeadersIfAbsent(message.getHeaders())
                    .build();
        }

        return message;
    }

    public String getSession(String token) {
        return sessions.get(jwtProvider.getUserId(token));
    }
}
