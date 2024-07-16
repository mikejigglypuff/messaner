package messaner.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.GsonInstantAdapter;
import messaner.JwtProvider;
import messaner.WSChannelInterceptor;
import messaner.factory.GsonFactory;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.time.Instant;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RESTController {
    private final RepositoryService repositoryService;
    private final GsonFactory gsonFactory;
    private final WSChannelInterceptor channelInterceptor;
    private final JwtProvider jwtProvider;

    @PostMapping("/room/create")
    @ResponseBody
    public String createRoom(@RequestBody String room) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication.isAuthenticated()) {
            UserDTO userDTO = new UserDTO(room, authentication.getCredentials().toString());
            if (repositoryService.createChannel(userDTO)) {
                return "/chatting/" + userDTO.getRoom();
            }
        }
        return "채널 생성 실패";
    }

    //구독 이후 접속
    @GetMapping("/chats")
    @ResponseBody
    public String getChats(
            @RequestParam(value="room", required = false, defaultValue = "") String room,
            HttpServletRequest req
    ) {
        String session = req.getHeader("Authorization");
        String decodeRoom = UriUtils.decode(room, "UTF-8");
        log.info("session: " + session);

        if(session != null) {
            UserDTO userDTO = new UserDTO(decodeRoom, jwtProvider.getUserId(session));
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

    @GetMapping("/rooms")
    @ResponseBody
    public String searchRooms(@RequestParam(value="name", required = false, defaultValue = "") String name) {
        try {
            String decodeName = UriUtils.decode(name, "UTF-8");
            List<Room> rooms = repositoryService.getRooms(new RoomDTO(decodeName));
            Gson gson = gsonFactory.instantGson();
            System.out.println(gson.toJson(rooms));
            return gson.toJson(rooms);
        } catch (NoSuchElementException ne) {
            log.info(ne.getMessage());
            return "no room matches";
        }
    }

    @GetMapping("/")
    public String mainPage() {
        return "/index.html";
    }
}
