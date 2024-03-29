package messaner.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController
public class STOMPController {

    private final RepositoryService repositoryService;
    private final SimpMessagingTemplate messagingTemplate;

    @SubscribeMapping("/room/chatting/{room}")
    public String subscribeRoom(@CookieValue("userId") Optional<String> cookie, @DestinationVariable String room) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            if (!repositoryService.addSubscription(userDTO)) {
                return "/";
            }
            return "/chatting/" + room;
        }
        return "/";
    }

    @MessageMapping("/chat")
    public void sendChat(@Payload ChatDTO chatDTO) {
        String sendUrl, sendMsg;

        if(repositoryService.addChat(chatDTO)) {
            sendUrl = "/topic/chat/" + chatDTO.getRoom();
            sendMsg = chatDTO.getChat();
        } else {
            sendUrl = "/queue/" + chatDTO.getUser();
            sendMsg = "채팅 입력에 문제가 발생했습니다";
        }

        messagingTemplate.convertAndSendToUser(
            chatDTO.getUser(), sendUrl, sendMsg
        );
    }

    @MessageMapping("/unsubscribe/{room}")
    public String unsubscribeRoom(@CookieValue("userId") Optional<String> cookie, @DestinationVariable String room) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            if (!repositoryService.RemoveSubscription(userDTO)) {
                return "";
            }
            return "/";
        }
        return "";
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String exceptionHandle(Throwable e) {
        return e.getMessage();
    }
}
