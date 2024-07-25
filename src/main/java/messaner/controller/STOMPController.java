package messaner.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.UserDTO;
import messaner.JwtProvider;
import messaner.WSChannelInterceptor;
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
    public void sendChat(
            @DestinationVariable String room,
            @Header("Authorization") String name,
            @Payload ChatDTO chatDTO
    ) {
        String sendUrl, sendMsg, user;
        String session = channelInterceptor.getSession(name);
        log.info("sessionID: " + session);

        if (session != null) {
            user = jwtProvider.getUserId(session);

            if (repositoryService.addChat(chatDTO, user, null)) {
                sendUrl = "/topic/chat/" + chatDTO.getRoom();
                sendMsg = chatDTO.getChat();
            } else {
                sendUrl = "/queue/" + user;
                sendMsg = "채팅 입력에 문제가 발생했습니다";
            }

            messagingTemplate.convertAndSendToUser(
                    user, sendUrl, sendMsg
            );
        }
    }

    @MessageMapping("/unsubscribe")
    public void unsubscribeRoom(
            @PathVariable(value = "room") String room,
            StompHeaderAccessor accessor
    ) {
        String session = channelInterceptor.getSession(accessor.getFirstNativeHeader("Authorization"));
        if(session != null) {
            String decodeRoom = UriUtils.decode(room, "UTF-8");
            UserDTO userDTO = new UserDTO(decodeRoom, jwtProvider.getUserId(session));
            repositoryService.removeSubscription(userDTO);
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String exceptionHandle(Throwable e) {
        String msg = e.getMessage();

        log.error(msg);
        return msg;
    }

}
