package messaner.service;

import messaner.DTO.ChatDTO;
import messaner.model.Chat;

import java.time.Instant;

public interface StompRepoService {
    public Chat addChat(ChatDTO chatDTO, String user, Instant dateTime);
}
