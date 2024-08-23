package messaner.repository;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MongoChatRepository implements ChatRepository {

  private final MongoTemplate template;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  @Autowired
  public MongoChatRepository(
      ApplicationContext ac,
      RoomRepository roomRepository,
      UserRepository userRepository
  ) {
    this.template = ac.getBean("mongoTemplate", MongoTemplate.class);
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public List<Chat> getChats(RoomDTO roomDTO) throws NoSuchElementException {
    Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
    Room curRoom = template.findOne(query, Room.class);

    if (curRoom == null) {
      throw new NoSuchElementException();
    }
    return curRoom.getChats();
  }

  @Override
  @Transactional
  public boolean insertChat(ChatDTO chatDTO, String user, Instant dateTime) {
    UserDTO userDTO = new UserDTO(chatDTO.getRoom(), user);

    try {
      if (!roomRepository.roomExists(chatDTO.getRoom())) {
        throw new NoSuchElementException();
      }

      if (userRepository.isSubscribed(userDTO)) {
        Instant date = (dateTime != null) ? dateTime : Instant.now();
        Chat newChat = new Chat(chatDTO, user, date);

        BulkOperations bulkOps = template.bulkOps(BulkOperations.BulkMode.UNORDERED, Room.class);

        Query chatQuery = new Query(Criteria.where("name").is(chatDTO.getRoom()));
        Update chatUpdate = new Update().push("chats", newChat);
        bulkOps.updateOne(chatQuery, chatUpdate);

        Query subQuery = new Query(
            Criteria.where("subscribers").elemMatch(Criteria.where("name").is(user)));
        Update subUpdate = new Update().push("subscribers.$.chats", newChat);
        bulkOps.updateOne(subQuery, subUpdate);

        bulkOps.execute();
      }
    } catch (Exception e) {
      log.error(e.toString());
      return false;
    }
    return true;
  }
}
