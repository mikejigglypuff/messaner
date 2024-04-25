package messaner.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.JwtProvider;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class RESTController {
    private final JwtProvider jwtProvider;
    private final RepositoryService repositoryService;

    @Autowired
    public RESTController(RepositoryService repositoryService, JwtProvider jwtProvider) {
        this.repositoryService = repositoryService;
        this.jwtProvider = jwtProvider;
    }

    @PreAuthorize("authenticated()")
    @PostMapping("/room/create")
    public String createChannel(@Payload RoomDTO roomDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication.isAuthenticated()) {
            UserDTO userDTO = new UserDTO(roomDTO.getRoom(), authentication.getCredentials().toString());
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
    @PreAuthorize("authenticated()")
    @GetMapping("/room/chatting/{room}")
    public String getChats(@PathVariable String room) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication.isAuthenticated()) {
            UserDTO userDTO = new UserDTO(room, authentication.getCredentials().toString());
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

    @PreAuthorize("perMitAll()")
    @GetMapping("/connect")
    public String Connect(@CookieValue(value = "userId") Optional<String> cookie, HttpServletResponse res) {

        return "/rooms";
    }
}
