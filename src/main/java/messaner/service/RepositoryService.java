package messaner.service;

import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface RepositoryService {
    public List<Chat> getChats(RoomDTO roomDTO);
    public boolean createChannel(UserDTO userDTO);
    public boolean addSubscription(UserDTO userDTO);
    public Chat addChat(ChatDTO chatDTO, String user, Instant dateTime);
    public List<Room> getRooms(RoomDTO roomDTO);
    public boolean removeSubscription(UserDTO userDTO);
    public boolean userAlreadyExists(String user);
    public boolean userSubscribed(UserDTO userDTO);
    public boolean removeChannel(RoomDTO roomDTO);
    public String createUser();
}
