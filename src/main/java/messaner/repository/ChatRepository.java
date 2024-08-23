package messaner.repository;

import java.time.Instant;
import java.util.List;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.model.Chat;

public interface ChatRepository {

  public List<Chat> getChats(RoomDTO roomDTO);

  public boolean insertChat(ChatDTO chatDTO, String user, Instant dateTime);
}
