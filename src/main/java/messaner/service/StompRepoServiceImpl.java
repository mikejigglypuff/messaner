package messaner.service;

import lombok.RequiredArgsConstructor;
import messaner.DTO.ChatDTO;
import messaner.model.Chat;
import messaner.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class StompRepoServiceImpl implements StompRepoService{
    private final ChatRepository chatRepository;

    @Override
    public Chat addChat(ChatDTO chatDTO, String user, Instant dateTime) {
        return (chatRepository.insertChat(chatDTO, user, dateTime))
                ? new Chat(chatDTO, user)
                : new Chat();
    }
}
