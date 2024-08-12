package messaner.repository;

import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.model.Chat;

import java.time.Instant;
import java.util.List;

public interface ChatRepository {
    public boolean insertChat(ChatDTO chatDTO, String user, Instant dateTime);

    public List<Chat> getChats(RoomDTO roomDTO);
}
