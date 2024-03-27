package messaner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import messaner.DTO.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class User {
    private String name;
    private List<Chat> chats;

    @Autowired
    public User(UserDTO userDTO, List<Chat> chatList) {
        name = userDTO.getUser();
        chats = chatList;
    }

    public void addChat(Chat chat) { chats.add(chat); }
}
