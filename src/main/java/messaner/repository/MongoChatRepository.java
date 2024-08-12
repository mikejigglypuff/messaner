package messaner.repository;

import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.model.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;

@Slf4j
public class MongoChatRepository implements ChatRepository{
    private final MongoTemplate template;

    @Autowired
    public MongoChatRepository(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public boolean insertChat(ChatDTO chatDTO, String user, Instant dateTime) {
        try {

        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<Chat> getChats(RoomDTO roomDTO) {
        return List.of();
    }
}
