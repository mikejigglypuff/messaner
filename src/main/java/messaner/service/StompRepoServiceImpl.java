package messaner.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import messaner.DTO.ChatDTO;
import messaner.model.Chat;
import messaner.repository.ChatRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StompRepoServiceImpl implements StompRepoService {

  private final ChatRepository chatRepository;

  @Override
  public Chat addChat(ChatDTO chatDTO, String user, Instant dateTime) {
    return (chatRepository.insertChat(chatDTO, user, dateTime))
        ? new Chat(chatDTO, user, dateTime)
        : new Chat();
  }
}
