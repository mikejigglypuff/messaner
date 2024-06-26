package messaner;

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

@Component
@Slf4j
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
        StompCommand command = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(command)) {
            String token = accessor.getFirstNativeHeader("Authorization");
            log.info("Message token: " + token);
            if(token != null) {
                log.info("Message destination: " + accessor.getDestination());
                repositoryService.addSubscription(
                    new UserDTO(accessor.getDestination(), jwtProvider.getUserId(token))
                );
                accessor.getSessionAttributes().put("authToken", token);
            }

        }
        return message;
    }


}
