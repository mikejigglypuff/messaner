package messaner.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.Jwt.JwtParser;
import messaner.WSChannelInterceptor;
import messaner.model.Chat;
import messaner.service.StompRepoService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class STOMPController {

    private final WSChannelInterceptor channelInterceptor;
    private final JwtParser jwtParser;
    private final StompRepoService repositoryService;

    @MessageMapping("/{room}")
    @SendTo("/topic/{room}")
    public Chat sendChat(
            @DestinationVariable("room") String room,
            @Header("Authorization") String token,
            @Payload ChatDTO chatDTO
    ) {
        String session = channelInterceptor.getSession(token);
        log.info("token: " + token + ", sessionID: " + session);

        if (session != null) {
            String user = jwtParser.getUserId(token);
            return repositoryService.addChat(chatDTO, user, null);
        }

        return null;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String exceptionHandle(Throwable e) {
        String msg = e.getMessage();

        log.error(msg);
        return msg;
    }

}
