package messaner.controller;

import lombok.RequiredArgsConstructor;
import messaner.DTO.ChatDTO;
import messaner.DTO.UserDTO;
import messaner.JwtProvider;
import messaner.service.RepositoryService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@Controller
public class STOMPController {

    private final RepositoryService repositoryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtProvider jwtProvider;

    @MessageMapping("/chat/{room}")
    public void sendChat(
            @DestinationVariable String room,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes,
            @Payload ChatDTO chatDTO
    ) {
        String sendUrl, sendMsg;
        String token = (String) sessionAttributes.get("authToken");

        if(token != null) {
            String user = jwtProvider.getUserId(token);

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

    @MessageMapping("/unsubscribe/{room}")
    public void unsubscribeRoom(@CookieValue("userId") Optional<String> cookie, @DestinationVariable("room") String room) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            repositoryService.removeSubscription(userDTO);
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String exceptionHandle(Throwable e) {
        return e.getMessage();
    }
}
