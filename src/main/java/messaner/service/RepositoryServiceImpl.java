package messaner.service;

import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.AggNameResult;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@Slf4j
@Service
public class RepositoryServiceImpl implements RepositoryService {
    private final MongoTemplate template;

    @Autowired
    RepositoryServiceImpl(ApplicationContext ac) {
        template = ac.getBean("mongoTemplate", MongoTemplate.class);
    }

    @Override
    @Transactional
    public boolean addChat(ChatDTO chatDTO, String user, Instant dateTime) {
        UserDTO userDTO = new UserDTO(chatDTO.getRoom(), user);
        try {
            if(!roomExists(chatDTO.getRoom())) throw new NoSuchElementException();

            if(userSubscribed(userDTO)) {
                Instant date = (dateTime != null) ? dateTime : Instant.now();
                Chat newChat = new Chat(chatDTO, user, date);

                BulkOperations bulkOps = template.bulkOps(BulkOperations.BulkMode.UNORDERED, Room.class);

                Query chatQuery = new Query(Criteria.where("name").is(chatDTO.getRoom()));
                Update chatUpdate = new Update().push("chats", newChat);
                bulkOps.updateOne(chatQuery, chatUpdate);

                Query subQuery = new Query(Criteria.where("subscribers").elemMatch(Criteria.where("name").is(user)));
                Update subUpdate = new Update().push("subscribers.$.chats", newChat);
                bulkOps.updateOne(subQuery, subUpdate);

                bulkOps.execute();
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean addSubscription(UserDTO userDTO) {
        try {
            if(!roomExists(userDTO.getRoom())) throw new NoSuchElementException();
            if(userSubscribed(userDTO)) throw new Exception("username " + userDTO.getUser() + " already subscribed");

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
    @Transactional
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
    @Transactional
    public String createUser() {
        String uuid = "user_" + UUID.randomUUID().toString();
        while (userAlreadyExists(uuid)) {
            uuid = "user" + UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Override
    @Transactional
    public List<Chat> getChats(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
        Room curRoom = template.findOne(query, Room.class);

        if(curRoom == null) throw new NoSuchElementException();
        return curRoom.getChats();
    }
    

    @Override
    @Transactional
    public List<Room> getRooms(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").regex(roomDTO.getRoom(), null));
        query.with(Sort.by(Sort.Order.asc("_id")));
        List<Room> curRoom = template.find(query, Room.class);

        if(curRoom.isEmpty()) { throw new NoSuchElementException(); }
        return curRoom;
    }

    @Override
    @Transactional
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
    @Transactional
    public boolean removeSubscription(UserDTO userDTO) {
        try {
            if(!roomExists(userDTO.getRoom())) throw new NoSuchElementException();

            List<User> userList = getUserList(userDTO.getRoom());
            if(userList.isEmpty()) throw new NoSuchElementException();

            User removeUser = new User(userDTO);
            userList.remove(removeUser);

            Query removeQuery = new Query(
                Criteria.where("subscribers").elemMatch(Criteria.where("name").is(userDTO.getUser()))
            );
            Update update = new Update().set("subscribers", userList);
            template.updateFirst(removeQuery, update, Room.class);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean userAlreadyExists(String user) {
        Query query = new Query(Criteria.where("subscribers").elemMatch(Criteria.where("name").is(user)));
        query.fields().include("name");
        return template.exists(query, Room.class);
    }

    @Override
    public boolean userSubscribed(UserDTO userDTO) {
        Aggregation agg = Aggregation.newAggregation(
            match(Criteria.where("_id").is(userDTO.getRoom())),
            unwind("subscribers"),
            match(Criteria.where("subscribers.name").is(userDTO.getUser())),
            project().and("subscribers.name").as("name")
        );

        AggregationResults<AggNameResult> result = template.aggregate(agg, "room", AggNameResult.class);
        return !result.getMappedResults().isEmpty();
    }

    private boolean roomExists(String room) {
        Query query = new Query(Criteria.where("name").is(room));
        return template.exists(query, Room.class);
    }

    private List<User> getUserList(String room) {
        Query query = new Query(
                Criteria.where("_id").is(room)
        );
        query.fields().include("subscribers");

        if(roomExists(room)) {
            return template.findOne(query, Room.class).getSubscribers();
        }
        return new ArrayList<>();
    }
}
