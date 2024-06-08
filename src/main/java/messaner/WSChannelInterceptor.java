package messaner;

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

@Component
public class WSChannelInterceptor implements ChannelInterceptor {
    private final RepositoryService repositoryService;
    private final JwtProvider jwtProvider;

    @Autowired
    public WSChannelInterceptor(RepositoryService repositoryService, JwtProvider jwtProvider) {
        this.repositoryService = repositoryService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if(token != null) {
                repositoryService.addSubscription(
                    new UserDTO(accessor.getDestination(), jwtProvider.getUserId(token))
                );
                accessor.getSessionAttributes().put("authToken", token);
            }

        }
        return message;
    }
}
