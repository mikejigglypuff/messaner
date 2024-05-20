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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/api")
public class RESTController {
    private final RepositoryService repositoryService;

    @Autowired
    public RESTController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PreAuthorize("authenticated()")
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

    @PreAuthorize("permitAll()")
    @GetMapping("rooms")
    @ResponseBody
    public String searchRooms(@RequestParam(value="name", required = false, defaultValue = "") String name) {
        try {
            //List<Room> rooms = repositoryService.getRooms(new RoomDTO(name));
            List<Room> rooms = new ArrayList<>();
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
    @GetMapping("room/chatting/{room}")
    @ResponseBody
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

    @PreAuthorize("authenticated()")
    @GetMapping("")
    public String Main() {
        return "index";
    }
}
