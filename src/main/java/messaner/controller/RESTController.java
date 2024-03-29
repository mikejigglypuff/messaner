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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class RESTController {
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

    @GetMapping("/rooms/{room}")
    public String searchRooms(@RequestParam String room) {
        try {
            List<Room> rooms = repositoryService.getRooms(new RoomDTO(room));
            Gson gson = new Gson();
            gson.toJson(rooms);
            return gson.toString();
        } catch (NoSuchElementException ne) {
            ne.printStackTrace();
            return "no room matches";
        }
    }

    @GetMapping("/rooms")
    public String getRooms() {
        try {
            List<Room> rooms = repositoryService.getRooms(new RoomDTO(""));
            Gson gson = new Gson();
            gson.toJson(rooms);
            System.out.println(gson.toString());
            return gson.toString();
        } catch (NoSuchElementException ne) {
            ne.printStackTrace();
            return "no room matches";
        }
    }

    //구독 이후 접속
    @GetMapping("/chatting/{room}")
    public String getChats(
            @CookieValue(value = "userId") Optional<String> cookie, @PathVariable String room
    ) {
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

    @GetMapping("/connect")
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
}
