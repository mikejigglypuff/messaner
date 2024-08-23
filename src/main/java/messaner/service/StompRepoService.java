package messaner.service;

import java.time.Instant;
import messaner.DTO.ChatDTO;
import messaner.model.Chat;

public interface StompRepoService {

  public Chat addChat(ChatDTO chatDTO, String user, Instant dateTime);
}
