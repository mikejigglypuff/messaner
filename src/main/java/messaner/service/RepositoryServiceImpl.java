package messaner.service;

import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RepositoryServiceImpl implements RepositoryService {
    private final MongoTemplate template;
    private final ApplicationContext ac;

    @Autowired
    RepositoryServiceImpl(ApplicationContext ac) {
        this.ac = ac;
        template = ac.getBean("mongoTemplate", MongoTemplate.class);
    }

    @Override
    public boolean addChat(ChatDTO chatDTO, String user, LocalDateTime dateTime) {
        UserDTO userDTO = new UserDTO(chatDTO.getRoom(), user);
        try {
            if(!roomExists(chatDTO.getRoom())) throw new NoSuchElementException();

            if(userSubscribed(userDTO)) {
                LocalDateTime date = (dateTime != null) ? dateTime : LocalDateTime.now();
                Chat newChat = new Chat(chatDTO, user, date);

                Query chatQuery = new Query(Criteria.where("name").is(chatDTO.getRoom()));
                Update chatUpdate = new Update().push("chats", newChat);
                template.updateFirst(chatQuery, chatUpdate, Chat.class);

                /*
                Query subQuery = new Query(Criteria.where("subscribers").elemMatch(Criteria.where("name").is(user)));
                List<User> subs = template.find(chatQuery, User.class);

                int index = 0;
                for(int i = 0; i < subs.size(); i++) {
                    if(subs.get(i).getName().equals(user)) {
                        index = i;
                        break;
                    }
                }

                Update subUpdate = new Update().push("subscribers." + index + ".chats", newChat);
                template.updateFirst(subQuery, subUpdate, Chat.class);
                 */
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addSubscription(UserDTO userDTO) {
        try {
            if(!roomExists(userDTO.getRoom())) throw new NoSuchElementException();

            Query query = new Query(Criteria.where("name").is(userDTO.getRoom()));
            Update update = new Update().push("subscribers", new User(userDTO));
            template.updateFirst(query, update, Room.class);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage()); //로깅 방식 변경할 것
            return false;
        }
    }

    @Override
    public boolean createChannel(UserDTO userDTO) {
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
    public String createUser() {
        String uuid = "user_" + UUID.randomUUID().toString();
        while (userAlreadyExists(uuid)) {
            uuid = "user" + UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Override
    public List<Chat> getChats(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
        Room curRoom = template.findOne(query, Room.class);

        if(curRoom == null) throw new NoSuchElementException();
        return curRoom.getChats();
    }
    

    @Override
    public List<Room> getRooms(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").regex(roomDTO.getRoom(), null));
        query.with(Sort.by(Sort.Order.asc("_id")));
        List<Room> curRoom = template.find(query, Room.class);

        if(curRoom.isEmpty()) { throw new NoSuchElementException(); }
        return curRoom;
    }

    @Override
    public boolean removeChannel(RoomDTO roomDTO) {
        Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
        try {
            template.remove(query, Room.class);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeSubscription(UserDTO userDTO) {
        try {
            if(!roomExists(userDTO.getRoom())) throw new NoSuchElementException();

            Query query = new Query(
                Criteria.where("subscribers").elemMatch(Criteria.where("name").is(userDTO.getUser()))
            );
            template.remove(query, User.class);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean userAlreadyExists(String user) {
        Query query = new Query(Criteria.where("_id").is("버터왕국")
                .and("subscribers").elemMatch(Criteria.where("name").is(user)));
        List<User> users = template.find(query, User.class);
        for(User u : users) {
            log.info(u.toString());
        }
        if(users.isEmpty()) log.info("user not found");
        return template.exists(query, User.class);
    }

    @Override
    public boolean userSubscribed(UserDTO userDTO) {
        Query query = new Query(
                Criteria.where("name").is(userDTO.getRoom())
                        .and("subscribers.name").is(userDTO.getUser())
        );
        Room room = template.findOne(query, Room.class);

        return room != null;
    }

    private boolean roomExists(String room) {
        Query query = new Query(Criteria.where("name").is(room));
        return template.exists(query, Room.class);
    }
}
