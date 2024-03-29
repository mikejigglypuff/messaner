package messaner.service;

import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    public List<Chat> getChats(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
        Room curRoom = template.findOne(query, Room.class);

        if(curRoom == null) throw new NoSuchElementException();
        return curRoom.getChats();
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
            e.printStackTrace(); //로깅 방식 변경할 것
            return false;
        }
    }

    @Override
    public boolean addSubscription(UserDTO userDTO) {
        try {
            Room room = this.getRoom(userDTO);
            room.getSubscribers().add(new User(userDTO, new ArrayList<>()));
            Query query = new Query(Criteria.where("name").is(userDTO.getRoom()));
            Update update = new Update().set("subscribers", room.getSubscribers());
            template.updateFirst(query, update, Room.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace(); //로깅 방식 변경할 것
            return false;
        }
    }
    
    private Room getRoom(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").is(roomDTO.getRoom()));
        Room room = template.findOne(query, Room.class);

        if(room == null) throw new NoSuchElementException();
        return room;
    }

    @Override
    public boolean addChat(ChatDTO chatDTO) {
        try {
            Room room = this.getRoom(chatDTO);
            List<Chat> chats = room.getChats();
            Chat newChat = new Chat(chatDTO);
            chats.add(newChat);
            for(User user : room.getSubscribers()) {
                if(user.getName().equals(chatDTO.getUser())) {
                    user.addChat(newChat);
                }
            }
            Query query = new Query(Criteria.where("name").is(chatDTO.getRoom()));
            Update update = new Update().set("chats", chats);
            template.updateFirst(query, update, Room.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Room> getRooms(RoomDTO roomDTO) throws NoSuchElementException {
        Query query = new Query(Criteria.where("name").regex(roomDTO.getRoom(), null));
        List<Room> curRoom = template.find(query, Room.class);

        if(curRoom.isEmpty()) { throw new NoSuchElementException(); }
        return curRoom;
    }

    @Override
    public boolean RemoveSubscription(UserDTO userDTO) {
        try {
            Room room = this.getRoom(userDTO);
            List<User> subs = room.getSubscribers();
            subs.removeIf(sub -> sub.getName().equals(userDTO.getUser()));
            Query query = new Query(Criteria.where("name").is(userDTO.getRoom()));
            Update update = new Update().set("subscribers", subs);
            template.updateFirst(query, update, Room.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean userAlreadyExists(String user) {
        Query query = new Query(Criteria.where("name").is(user));
        return template.exists(query, User.class);
    }

    @Override
    public boolean userSubscribed(UserDTO userDTO) {
        Query query = new Query(Criteria.where("name").is(userDTO.getRoom()));
        Room room = template.findOne(query, Room.class);

        if(room != null) {
            List<User> subs = room.getSubscribers();

            for (User user : subs) {
                if(user.getName().equals(userDTO.getUser())) { return true; }
            }
        }

        return false;
    }
}
