package messaner.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.Jwt.JwtParser;
import messaner.service.RepositoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RESTController {

    private final JwtParser jwtParser;
    private final RepositoryService repositoryService;

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

        if(session != null) {
            UserDTO userDTO = new UserDTO(room, jwtParser.getUserId(session));
            log.info("room: " + userDTO.getRoom() + " user: " + userDTO.getUser());

            if (repositoryService.userSubscribed(userDTO)) {
                return repositoryService.getChatsGson(userDTO);
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
            return repositoryService.getRoomsGson(new RoomDTO(decodeName));
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
