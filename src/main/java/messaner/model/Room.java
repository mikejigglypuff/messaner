package messaner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import messaner.DTO.RoomDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "room")
@RequiredArgsConstructor
public class Room {
    @Id
    private String name;
    private List<User> subscribers;
    private List<Chat> chats;

    @Autowired
    public Room(RoomDTO roomDTO, List<User> userList, List<Chat> chatList) {
        name = roomDTO.getRoom();
        subscribers = userList;
        chats = chatList;
    }

    public String getName() {
        return name;
    }

    public List<User> getSubscribers() {
        return subscribers;
    }

    public List<Chat> getChats() {
        return chats;
    }
}
