package messaner.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.service.RepositoryService;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
public class RESTController {
    static int COOKIE_EXPIRES = 60 * 60;
    private final RepositoryService repositoryService;

    @PostMapping("/room/create")
    public String createChannel(@CookieValue("userId") Optional<String> cookie, @Payload RoomDTO roomDTO) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(roomDTO.getRoom(), cookie.get());
            if (repositoryService.createChannel(userDTO)) {
                return "/chatting/" + userDTO.getRoom();
            }
        }
        return "채널 생성 실패";
    }

    @GetMapping("/rooms")
    public String searchRooms(@RequestParam(value="name", required = false, defaultValue = "") String name) {
        try {
            List<Room> rooms;
            rooms = repositoryService.getRooms(new RoomDTO(name));
            Gson gson = new Gson();
            System.out.println(gson.toJson(rooms));
            return gson.toJson(rooms);
        } catch (NoSuchElementException ne) {
            ne.printStackTrace();
            return "no room matches";
        }
    }

    //구독 이후 접속
    @GetMapping("/room/chatting/{room}")
    public String getChats(
            @CookieValue(value = "userId") Optional<String> cookie, @PathVariable String room
    ) {
        if(cookie.isPresent()) {
            UserDTO userDTO = new UserDTO(room, cookie.get());
            if (repositoryService.userSubscribed(userDTO)) {
                List<Chat> chats = repositoryService.getChats(userDTO);

                Gson gson = new Gson();
                return gson.toJson(chats);
            } else {
                return "/topic/chatting/" + userDTO.getRoom();
            }
        }
        return "/";
    }

    @GetMapping("/connect")
    public String Connect(@CookieValue(value = "userId") Optional<String> cookie, HttpServletResponse res) {
        if(cookie.isEmpty()) {
            String uuid = "user" + UUID.randomUUID().toString();
            while (repositoryService.userAlreadyExists(uuid)) {
                uuid = "user" + UUID.randomUUID().toString();
            }
            Cookie newCookie = new Cookie("userId", uuid);
            newCookie.setMaxAge(COOKIE_EXPIRES);
            newCookie.setHttpOnly(true);
            res.addCookie(newCookie);
        }

        return "/rooms";
    }
}
