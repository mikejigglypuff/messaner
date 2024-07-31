package messaner.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.UserDTO;
import messaner.JwtProvider;
import messaner.WSChannelInterceptor;
import messaner.model.Chat;
import messaner.service.RepositoryService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.time.Instant;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class STOMPController {

    private final RepositoryService repositoryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtProvider jwtProvider;
    private final WSChannelInterceptor channelInterceptor;

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
            String user = jwtProvider.getUserId(token);
            Instant date = Instant.now();

            return repositoryService.addChat(chatDTO, user, date);
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
