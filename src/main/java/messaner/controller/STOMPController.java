package messaner.controller;

import lombok.RequiredArgsConstructor;
import messaner.DTO.ChatDTO;
import messaner.DTO.UserDTO;
import messaner.service.RepositoryService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class STOMPController {

    private final RepositoryService repositoryService;
    private final SimpMessagingTemplate messagingTemplate;

    @SubscribeMapping("/room/chatting/{room}")
    public void subscribeRoom(@CookieValue("userId") Optional<String> cookie, @DestinationVariable String room) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            repositoryService.addSubscription(userDTO);
        }
    }

    @MessageMapping("/chat")
    public void sendChat(@CookieValue("userId") Optional<String> cookie, @Payload ChatDTO chatDTO) {
        if(cookie.isPresent()) {

            String sendUrl, sendMsg;
            String user = cookie.get();

            if (repositoryService.addChat(chatDTO, user)) {
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
    public void unsubscribeRoom(@CookieValue("userId") Optional<String> cookie, @DestinationVariable String room) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            repositoryService.RemoveSubscription(userDTO);
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String exceptionHandle(Throwable e) {
        return e.getMessage();
    }
}
