package messaner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.UserDTO;
import messaner.Jwt.JwtParser;
import messaner.Jwt.JwtProvider;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WSChannelInterceptor implements ChannelInterceptor {

  private final RepositoryService repositoryService;
  private final JwtProvider jwtProvider;
  private final JwtParser jwtParser;
  private final Map<String, String> sessions; //key: userId, val: sessionId

  @Autowired
  public WSChannelInterceptor(
      RepositoryService repositoryService, JwtProvider jwtProvider, JwtParser jwtParser
  ) {
    this.repositoryService = repositoryService;
    this.jwtProvider = jwtProvider;
    this.jwtParser = jwtParser;
    this.sessions = new ConcurrentHashMap<>();
  }

  public String getSession(String token) {
    return sessions.get(jwtParser.getUserId(token));
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

        if (dest != null) {
          String[] uri = dest.split("/");

          String user = jwtParser.getUserId(token);
          log.info("user: " + user);

          if (StompCommand.SUBSCRIBE.equals(command)) {
            repositoryService.addSubscription(new UserDTO(uri[uri.length - 1], user));
            String sessionId = jwtParser.getSessionId(token);
            sessions.put(user, sessionId);

          } else if (StompCommand.UNSUBSCRIBE.equals(command)) {
            repositoryService.removeSubscription(new UserDTO(uri[uri.length - 1], user));
            sessions.remove(user);
          }
        }
      } else if (StompCommand.SUBSCRIBE.equals(command)) {
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

}
