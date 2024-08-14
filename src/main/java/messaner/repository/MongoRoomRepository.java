package messaner.repository;

import lombok.extern.slf4j.Slf4j;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@Slf4j
public class MongoRoomRepository implements RoomRepository {
    private final MongoTemplate template;

    @Autowired
    public MongoRoomRepository(ApplicationContext ac) {
        this.template = ac.getBean("mongoTemplate", MongoTemplate.class);
    }

    @Override
    @Transactional
    public List<Room> getRooms(RoomDTO roomDTO) {
        Query query = new Query(Criteria.where("name").regex(roomDTO.getRoom(), null));
        query.with(Sort.by(Sort.Order.asc("_id")));
        List<Room> curRoom = template.find(query, Room.class);

        if(curRoom.isEmpty()) { throw new NoSuchElementException(); }
        return curRoom;
    }

    @Override
    @Transactional
    public boolean insertRoom(UserDTO userDTO) {
        try {
            List<User> users = new ArrayList<>();
            List<Chat> chats = new ArrayList<>();
            users.add(new User(userDTO, chats));
            template.insert(new Room(userDTO, users, chats));
            return true;
        } catch (Exception e) {
            log.error(e.getMessage()); //로깅 방식 변경할 것
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeRoom(RoomDTO roomDTO) {
        Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
        try {
            template.remove(query, Room.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public boolean roomExists(String room) {
        Query query = new Query(Criteria.where("name").is(room));
        return template.exists(query, Room.class);
    }
}
