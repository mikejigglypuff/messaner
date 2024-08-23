package messaner.model;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import messaner.DTO.RoomDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "room")
@Getter
@RequiredArgsConstructor
public class Room {

  @Id
  private String name;
  private List<Chat> chats;
  private List<User> subscribers;


  @Autowired
  public Room(RoomDTO roomDTO, List<User> userList, List<Chat> chatList) {
    name = roomDTO.getRoom();
    subscribers = userList;
    chats = chatList;
  }

}
