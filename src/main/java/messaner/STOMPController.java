package messaner;

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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
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
    public void sendChat(@Payload ChatDTO chatDTO, Principal principal) {
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
        //DB상에 구독 정보 삭제하기
        //이후 구독 취소 로직 실행
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            if (!repositoryService.RemoveSubscription(userDTO)) {
                return "";
            }
            return "/";
        }
        return "";
    }

    @PostMapping("/createChannel")
    public String createChannel(@Payload UserDTO userDTO) {
        //전송받을 메시지 DTO를 정의하고 매개변수로 받기
        //새 채널 생성 및 초기화 후 DB상에 등록하기
        String uuid = "room" + UUID.randomUUID();
        if(repositoryService.createChannel(userDTO)) {
            return "/chatting/" + userDTO.getRoom();
        }
        return "/";
    }

    @GetMapping("/rooms")
    public String getRooms() {
        List<Room> rooms = repositoryService.getRooms(new RoomDTO(""));
        Gson gson = new Gson();
        gson.toJson(rooms);
        return gson.toString();
    }

    @GetMapping("/rooms/{room}")
    public String searchRooms(@RequestParam String room) {
        //채널 목록 중 room 문자열을 포함하는 채널들을 반환할 것
        List<Room> rooms = repositoryService.getRooms(new RoomDTO(room));
        Gson gson = new Gson();
        gson.toJson(rooms);
        return gson.toString();
    }

    //구독 이후 접속
    @GetMapping("/chatting/{room}")
    public String getChats(
        @CookieValue(value = "userId") Optional<String> cookie, @PathVariable String room
    ) {
        //구독 여부 확인 후 구독하지 않았다면 위의 SubscribeMapping 경로로 리다이렉트하도록 하기
        //채널 이름에 따라 채팅 목록을 DB에서 읽어오고 Gson을 이용해 문자열 형태로 전송

        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            if (repositoryService.userSubscribed(userDTO)) {
                List<Chat> chats = repositoryService.getChats(userDTO);

                Gson gson = new Gson();
                gson.toJson(chats);
                return gson.toString();
            } else {
                return "/topic/chatting/" + userDTO.getRoom();
            }
        }
        return "/";
    }

    @GetMapping("/")
    public String Connect(@CookieValue(value = "userId") Optional<String> cookie, HttpServletResponse res) {
        if(cookie.isEmpty()) {
            String uuid = "user" + UUID.randomUUID().toString();
            while (repositoryService.userAlreadyExists(uuid)) {
                uuid = "user" + UUID.randomUUID().toString();
            }
            Cookie newCookie = new Cookie("userId", uuid);
            res.addCookie(newCookie);
        }

        return "/rooms";
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String exceptionHandle(Throwable e) {
        return e.getMessage();
    }
}
