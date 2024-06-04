package messaner.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.GsonInstantAdapter;
import messaner.JwtProvider;
import messaner.factory.GsonFactory;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@Slf4j
@Controller
public class RESTController {
    private final RepositoryService repositoryService;
    private final GsonFactory gsonFactory;

    @Autowired
    public RESTController(RepositoryService repositoryService, GsonFactory gsonFactory) {
        this.gsonFactory = gsonFactory;
        this.repositoryService = repositoryService;
    }

    @PostMapping("/room/create")
    @ResponseBody
    public String createRoom(@Payload RoomDTO roomDTO) {
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
    @ResponseBody
    public String searchRooms(@RequestParam(value="name", required = false, defaultValue = "") String name) {
        try {
            List<Room> rooms = repositoryService.getRooms(new RoomDTO(name));
            Gson gson = gsonFactory.instantGson();
            System.out.println(gson.toJson(rooms));
            return gson.toJson(rooms);
        } catch (NoSuchElementException ne) {
            log.info(ne.getMessage());
            return "no room matches";
        }
    }

    //구독 이후 접속
    @GetMapping("/room/chatting/{room}")
    @ResponseBody
    public String getChats(@PathVariable String room) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication.isAuthenticated()) {
            UserDTO userDTO = new UserDTO(room, authentication.getCredentials().toString());
            if (repositoryService.userSubscribed(userDTO)) {
                List<Chat> chats = repositoryService.getChats(userDTO);

                Gson gson = gsonFactory.instantGson();
                return gson.toJson(chats);
            } else {
                return "/topic/chatting/" + userDTO.getRoom();
            }
        }
        return "/";
    }

    @GetMapping("/")
    public String mainPage() {
        return "/static/index.html";
    }
}
